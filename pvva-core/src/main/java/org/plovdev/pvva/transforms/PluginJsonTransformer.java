package org.plovdev.pvva.transforms;

import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.utils.PVVAJsonSerializer;

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

        String author = null;
        String devId = null;
        String authorPage = null;
        String license = null;
        String homepage = null;

        if (pluginJson.has(LEGAL)) {
            JsonObject legal = pluginJson.get(LEGAL).getAsJsonObject();
            author = extractString(AUTHOR, legal);
            devId = extractString(DEVELOPER_ID, legal);
            authorPage = extractString(AUTHOR_PAGE, legal);
            license = extractString(LICENSE, legal);
            homepage = extractString(HOMEPAGE, legal);
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
        json.developerId().ifPresent(devId -> legal.addProperty(DEVELOPER_ID, devId));
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