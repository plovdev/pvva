package org.plovdev.pvva.transforms;

import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.utils.PVVAJsonSerializer;

import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractString;

public final class PluginJsonTransformer {
    private PluginJsonTransformer() {
    }

    public static @NonNull PluginJson ofJson(String json) {
        JsonObject pluginJson = PVVAJsonSerializer.GLOBAL_JSON.fromJson(json, JsonObject.class);
        JsonObject commons = pluginJson.get("commons").getAsJsonObject();

        String title = extractString("title", commons);
        String version = extractString("version", commons);
        String description = extractString("description", commons);

        JsonObject legal = pluginJson.get("legal").getAsJsonObject();
        String author = extractString("author", legal);
        String devId = extractString("developer-id", legal);
        String authorPage = extractString("author-page", legal);
        String license = extractString("license", legal);
        String homepage = extractString("homepage", legal);

        JsonObject autoUpdate = pluginJson.get("auto-update").getAsJsonObject();
        String updateUrl = extractString("url", commons);
        boolean seignRequired = Boolean.getBoolean(extractString("sign-required", commons));

        return new PluginJson(title, version, description, updateUrl, seignRequired, author, devId, authorPage, license, homepage);
    }

    public static String toJson(@NonNull PluginJson json) {
        JsonObject pluginJson = new JsonObject();

        JsonObject commons = new JsonObject();
        commons.addProperty("title", json.title());
        commons.addProperty("version", json.version());
        json.description().ifPresent(description -> commons.addProperty("description", description));
        pluginJson.add("commons", commons);

        JsonObject autoUpdate = new JsonObject();
        json.autoUpdateUrl().ifPresent(url -> autoUpdate.addProperty("url", url));
        autoUpdate.addProperty("sign-required", json.signRequired());
        pluginJson.add("auto-update", autoUpdate);

        JsonObject legal = new JsonObject();
        json.author().ifPresent(author -> legal.addProperty("author", author));
        json.developerId().ifPresent(devId -> legal.addProperty("developer-id", devId));
        json.authorPage().ifPresent(page -> legal.addProperty("author-page", page));
        json.licenseUrl().ifPresent(url -> legal.addProperty("license", url));
        json.homepage().ifPresent(homepage -> legal.addProperty("homepage", homepage));
        pluginJson.add("legal", legal);

        return PVVAJsonSerializer.GLOBAL_JSON.toJson(pluginJson);
    }
}