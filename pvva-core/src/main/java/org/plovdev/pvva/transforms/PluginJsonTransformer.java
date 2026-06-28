package org.plovdev.pvva.transforms;

import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.utils.PVVAJsonSerializer;

import java.util.NoSuchElementException;
import java.util.Objects;

import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractString;

public final class PluginJsonTransformer {
    public static final String TITLE = "title";
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String COMMONS = "commons";

    public static final String LEGAL = "legal";
    public static final String AUTHOR = "author";
    public static final String DEVELOPER_ID = "developer-id";
    public static final String AUTHOR_PAGE = "author-page";
    public static final String LICENSE = "license";
    public static final String HOMEPAGE = "homepage";

    public static final String AUTO_UPDATE = "auto-update";
    public static final String AUTO_UPDATE_URL = "url";

    private PluginJsonTransformer() {
    }

    public static @NonNull PluginJson ofJson(String json) {
        JsonObject pluginJson = PVVAJsonSerializer.GLOBAL_JSON.fromJson(json, JsonObject.class);
        JsonObject commons = pluginJson.get(COMMONS).getAsJsonObject();

        String title = extractString(TITLE, commons);
        String version = extractString(VERSION, commons);
        String description = extractString(DESCRIPTION, commons);

        String updateUrl = null;
        if (pluginJson.has(AUTO_UPDATE)) {
            JsonObject autoUpdate = pluginJson.get(AUTO_UPDATE).getAsJsonObject();
            updateUrl = extractString(AUTO_UPDATE_URL, commons);
        }

        String author;
        String devId;
        String authorPage;
        String license;
        String homepage;

        if (pluginJson.has(LEGAL)) {
            JsonObject legal = pluginJson.get(LEGAL).getAsJsonObject();
            author = extractString(AUTHOR, legal);
            devId = Objects.requireNonNull(extractString(DEVELOPER_ID, legal));
            authorPage = extractString(AUTHOR_PAGE, legal);
            license = extractString(LICENSE, legal);
            homepage = extractString(HOMEPAGE, legal);
        } else {
            throw new NoSuchElementException("Section legal not found.");
        }

        return new PluginJson(title, version, description, updateUrl, author, devId, authorPage, license, homepage);
    }

    public static String toJson(@NonNull PluginJson json) {
        return toJson(json, false);
    }

    public static String toJson(@NonNull PluginJson json, boolean prettyPrint) {
        JsonObject pluginJson = new JsonObject();

        JsonObject commons = new JsonObject();
        commons.addProperty(TITLE, json.title());
        commons.addProperty(VERSION, json.version());
        json.description().ifPresent(description -> commons.addProperty(DESCRIPTION, description));
        pluginJson.add(COMMONS, commons);

        JsonObject autoUpdate = new JsonObject();
        json.autoUpdateUrl().ifPresent(url -> autoUpdate.addProperty(AUTO_UPDATE_URL, url));
        pluginJson.add(AUTO_UPDATE, autoUpdate);

        JsonObject legal = new JsonObject();
        json.author().ifPresent(author -> legal.addProperty(AUTHOR, author));
        legal.addProperty(DEVELOPER_ID, json.developerId());
        json.authorPage().ifPresent(page -> legal.addProperty(AUTHOR_PAGE, page));
        json.licenseUrl().ifPresent(url -> legal.addProperty(LICENSE, url));
        json.homepage().ifPresent(homepage -> legal.addProperty(HOMEPAGE, homepage));
        pluginJson.add(LEGAL, legal);

        if (prettyPrint) {
            return PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(pluginJson);
        } else {
            return PVVAJsonSerializer.GLOBAL_JSON.toJson(pluginJson);
        }
    }
}