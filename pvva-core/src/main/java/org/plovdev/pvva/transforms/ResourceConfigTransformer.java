package org.plovdev.pvva.transforms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.resourceconfig.CategoriesResources;
import org.plovdev.pvva.models.configs.resourceconfig.MainResources;
import org.plovdev.pvva.models.configs.resourceconfig.ModelsResources;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;

import java.util.ArrayList;
import java.util.List;

import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractBoolean;
import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractString;

public final class ResourceConfigTransformer {
    private ResourceConfigTransformer() {
    }

    public static @NonNull ResourceConfig ofJson(String json) {
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        String baseUrl = extractString("base-url", root);

        // Main resources
        JsonObject main = root.getAsJsonObject("main");
        boolean mainSupports = extractBoolean("supports", main);
        String mainEndpoint = extractString("endpoint", main);

        JsonObject mainSearch = main.getAsJsonObject("main-search");
        boolean mainSearchSupports = extractBoolean("supports", mainSearch);
        String mainSearchEndpoint = extractString("endpoint", mainSearch);

        MainResources mainResources = new MainResources(mainSupports, mainEndpoint, mainSearchSupports, mainSearchEndpoint);

        // Models resources
        JsonObject models = root.getAsJsonObject("models");
        boolean modelsSupports = extractBoolean("supports", models);
        String modelsEndpoint = extractString("endpoint", models);

        JsonObject model = models.getAsJsonObject("model");
        boolean modelSupports = extractBoolean("supports", model);
        String modelEndpoint = extractString("endpoint", model);

        JsonObject modelSearch = models.getAsJsonObject("model-search");
        boolean modelSearchSupports = extractBoolean("supports", modelSearch);
        String modelSearchEndpoint = extractString("endpoint", modelSearch);
        ModelsResources modelsResources = new ModelsResources(modelsSupports, modelsEndpoint, modelSupports, modelEndpoint, modelSearchSupports, modelSearchEndpoint);

        // Categories resources
        JsonObject categories = root.getAsJsonObject("categories");
        boolean categoriesSupports = extractBoolean("supports", categories);
        String categoriesEndpoint = extractString("endpoint", categories);

        JsonObject category = categories.getAsJsonObject("category");
        boolean categorySupports = extractBoolean("supports", category);
        String categoryEndpoint = extractString("endpoint", category);
        CategoriesResources categoriesResources = new CategoriesResources(categoriesSupports, categoriesEndpoint, categorySupports, categoryEndpoint);

        // Video
        JsonObject video = root.getAsJsonObject("video");
        boolean videoSupports = extractBoolean("supports", video);
        String videoEndpoint = extractString("endpoint", video);

        // Mirrors
        JsonObject mirrors = root.getAsJsonObject("mirrors");
        boolean mirrorsSupports = extractBoolean("supports", mirrors);
        List<String> mirrorsList = new ArrayList<>();
        if (mirrorsSupports && mirrors.has("urls")) {
            JsonArray urls = mirrors.getAsJsonArray("urls");
            urls.forEach(url -> mirrorsList.add(url.getAsString()));
        }

        return new ResourceConfig(baseUrl, videoSupports, videoEndpoint, mirrorsSupports, mirrorsList, mainResources, modelsResources, categoriesResources);
    }

    public static String toJson(@NonNull ResourceConfig config) {
        JsonObject root = new JsonObject();

        root.addProperty("base-url", config.baseUrl());

        // Main resources
        JsonObject main = new JsonObject();
        MainResources mainRes = config.mainResources();
        main.addProperty("supports", mainRes.supports());
        mainRes.endpoint().ifPresent(e -> main.addProperty("endpoint", e));

        JsonObject mainSearch = new JsonObject();
        mainSearch.addProperty("supports", mainRes.supportSearch());
        mainRes.searchUrl().ifPresent(s -> mainSearch.addProperty("endpoint", s));
        main.add("main-search", mainSearch);
        root.add("main", main);

        // Models resources
        JsonObject models = getModelsBlock(config);
        root.add("models", models);

        // Categories resources
        JsonObject categories = new JsonObject();
        config.categoriesResources().ifPresent(catRes -> {
            categories.addProperty("supports", catRes.supports());
            catRes.endpoint().ifPresent(e -> categories.addProperty("endpoint", e));

            JsonObject category = new JsonObject();
            category.addProperty("supports", catRes.supportCategory());
            catRes.categoryEndpoint().ifPresent(e -> category.addProperty("endpoint", e));
            categories.add("category", category);
        });
        root.add("categories", categories);

        // Video
        JsonObject video = new JsonObject();
        video.addProperty("supports", config.supportVideo());
        video.addProperty("endpoint", config.videoEndpoint());
        root.add("video", video);

        // Mirrors
        JsonObject mirrors = new JsonObject();
        mirrors.addProperty("supports", config.supportMirrors());
        if (config.supportMirrors()) {
            JsonArray urls = new JsonArray();
            config.mirrors().ifPresent(mrs -> mrs.forEach(urls::add));
            mirrors.add("urls", urls);
        }
        root.add("mirrors", mirrors);
        return org.plovdev.pvva.utils.PVVAJsonSerializer.GLOBAL_JSON.toJson(root);
    }

    private static @NonNull JsonObject getModelsBlock(@NonNull ResourceConfig config) {
        JsonObject models = new JsonObject();
        config.modelsResources().ifPresent(modelsRes -> {
            models.addProperty("supports", modelsRes.supports());
            modelsRes.endpoint().ifPresent(e -> models.addProperty("endpoint", e));

            JsonObject model = new JsonObject();
            model.addProperty("supports", modelsRes.supportModel());
            modelsRes.modelEndpoint().ifPresent(e -> model.addProperty("endpoint", e));
            models.add("model", model);

            JsonObject modelSearch = new JsonObject();
            modelSearch.addProperty("supports", modelsRes.supportModelSearch());
            modelsRes.modelSearchEndpoint().ifPresent(e -> modelSearch.addProperty("endpoint", e));
            models.add("model-search", modelSearch);
        });

        return models;
    }
}