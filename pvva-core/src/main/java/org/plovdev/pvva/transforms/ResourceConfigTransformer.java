package org.plovdev.pvva.transforms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.resourceconfig.CategoriesResources;
import org.plovdev.pvva.models.configs.resourceconfig.MainResources;
import org.plovdev.pvva.models.configs.resourceconfig.ModelsResources;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.utils.PVVAJsonSerializer;

import java.util.ArrayList;
import java.util.List;

import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractBoolean;
import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractString;

public final class ResourceConfigTransformer {
    public static final String BASE_URL = "base-url";
    public static final String MAIN = "main";
    public static final String SUPPORTS = "supports";
    public static final String ENDPOINT = "endpoint";

    public static final String MAIN_SEARCH = "main-search";
    public static final String MODELS = "models";
    public static final String MODEL = "model";
    public static final String MODEL_SEARCH = "model-search";
    public static final String CATEGORIES = "categories";
    public static final String CATEGORY = "category";
    public static final String VIDEO = "video";
    public static final String MIRRORS = "mirrors";
    public static final String URLS = "urls";

    private ResourceConfigTransformer() {
    }

    public static @NonNull ResourceConfig ofJson(String json) {
        JsonObject root = PVVAJsonSerializer.GLOBAL_JSON.fromJson(json, JsonObject.class);

        String baseUrl = extractString(BASE_URL, root);

        // Main resources
        JsonObject main = root.getAsJsonObject(MAIN);
        boolean mainSupports = extractBoolean(SUPPORTS, main);
        String mainEndpoint = extractString(ENDPOINT, main);

        JsonObject mainSearch = main.getAsJsonObject(MAIN_SEARCH);
        boolean mainSearchSupports = extractBoolean(SUPPORTS, mainSearch);
        String mainSearchEndpoint = extractString(ENDPOINT, mainSearch);

        MainResources mainResources = new MainResources(mainSupports, mainEndpoint, mainSearchSupports, mainSearchEndpoint);

        // Models resources
        JsonObject models = root.getAsJsonObject(MODELS);
        boolean modelsSupports = extractBoolean(SUPPORTS, models);
        String modelsEndpoint = extractString(ENDPOINT, models);

        JsonObject model = models.getAsJsonObject(MODEL);
        boolean modelSupports = extractBoolean(SUPPORTS, model);
        String modelEndpoint = extractString(ENDPOINT, model);

        JsonObject modelSearch = models.getAsJsonObject(MODEL_SEARCH);
        boolean modelSearchSupports = extractBoolean(SUPPORTS, modelSearch);
        String modelSearchEndpoint = extractString(ENDPOINT, modelSearch);
        ModelsResources modelsResources = new ModelsResources(modelsSupports, modelsEndpoint, modelSupports, modelEndpoint, modelSearchSupports, modelSearchEndpoint);

        // Categories resources
        JsonObject categories = root.getAsJsonObject(CATEGORIES);
        boolean categoriesSupports = extractBoolean(SUPPORTS, categories);
        String categoriesEndpoint = extractString(ENDPOINT, categories);

        JsonObject category = categories.getAsJsonObject(CATEGORY);
        boolean categorySupports = extractBoolean(SUPPORTS, category);
        String categoryEndpoint = extractString(ENDPOINT, category);
        CategoriesResources categoriesResources = new CategoriesResources(categoriesSupports, categoriesEndpoint, categorySupports, categoryEndpoint);

        // Video
        JsonObject video = root.getAsJsonObject(VIDEO);
        boolean videoSupports = extractBoolean(SUPPORTS, video);
        String videoEndpoint = extractString(ENDPOINT, video);

        // Mirrors
        JsonObject mirrors = root.getAsJsonObject(MIRRORS);
        boolean mirrorsSupports = extractBoolean(SUPPORTS, mirrors);
        List<String> mirrorsList = new ArrayList<>();
        if (mirrorsSupports && mirrors.has(URLS)) {
            JsonArray urls = mirrors.getAsJsonArray(URLS);
            urls.forEach(url -> mirrorsList.add(url.getAsString()));
        }

        return new ResourceConfig(baseUrl, videoSupports, videoEndpoint, mirrorsSupports, mirrorsList, mainResources, modelsResources, categoriesResources);
    }

    public static String toJson(@NonNull ResourceConfig config) {
        return toJson(config, false);
    }

    public static String toJson(@NonNull ResourceConfig config, boolean prettyPrint) {
        JsonObject root = new JsonObject();

        root.addProperty(BASE_URL, config.baseUrl());

        // Main resources
        JsonObject main = new JsonObject();
        MainResources mainRes = config.mainResources();
        main.addProperty(SUPPORTS, mainRes.supports());
        mainRes.endpoint().ifPresent(e -> main.addProperty(ENDPOINT, e));

        JsonObject mainSearch = new JsonObject();
        mainSearch.addProperty(SUPPORTS, mainRes.supportSearch());
        mainRes.searchUrl().ifPresent(s -> mainSearch.addProperty(ENDPOINT, s));
        main.add(MAIN_SEARCH, mainSearch);
        root.add(MAIN, main);

        // Models resources
        JsonObject models = getModelsBlock(config);
        root.add(MODELS, models);

        // Categories resources
        JsonObject categories = new JsonObject();
        config.categoriesResources().ifPresent(catRes -> {
            categories.addProperty(SUPPORTS, catRes.supports());
            catRes.endpoint().ifPresent(e -> categories.addProperty(ENDPOINT, e));

            JsonObject category = new JsonObject();
            category.addProperty(SUPPORTS, catRes.supportCategory());
            catRes.categoryEndpoint().ifPresent(e -> category.addProperty(ENDPOINT, e));
            categories.add(CATEGORY, category);
        });
        root.add(CATEGORIES, categories);

        // Video
        JsonObject video = new JsonObject();
        video.addProperty(SUPPORTS, config.supportVideo());
        video.addProperty(ENDPOINT, config.videoEndpoint());
        root.add(VIDEO, video);

        // Mirrors
        JsonObject mirrors = new JsonObject();
        mirrors.addProperty(SUPPORTS, config.supportMirrors());
        if (config.supportMirrors()) {
            JsonArray urls = new JsonArray();
            config.mirrors().ifPresent(mrs -> mrs.forEach(urls::add));
            mirrors.add(URLS, urls);
        }
        root.add(MIRRORS, mirrors);

        if (prettyPrint) {
            return org.plovdev.pvva.utils.PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(root);
        } else {
            return org.plovdev.pvva.utils.PVVAJsonSerializer.GLOBAL_JSON.toJson(root);
        }
    }

    private static @NonNull JsonObject getModelsBlock(@NonNull ResourceConfig config) {
        JsonObject models = new JsonObject();
        config.modelsResources().ifPresent(modelsRes -> {
            models.addProperty(SUPPORTS, modelsRes.supports());
            modelsRes.endpoint().ifPresent(e -> models.addProperty(ENDPOINT, e));

            JsonObject model = new JsonObject();
            model.addProperty(SUPPORTS, modelsRes.supportModel());
            modelsRes.modelEndpoint().ifPresent(e -> model.addProperty(ENDPOINT, e));
            models.add(MODEL, model);

            JsonObject modelSearch = new JsonObject();
            modelSearch.addProperty(SUPPORTS, modelsRes.supportModelSearch());
            modelsRes.modelSearchEndpoint().ifPresent(e -> modelSearch.addProperty(ENDPOINT, e));
            models.add(MODEL_SEARCH, modelSearch);
        });

        return models;
    }
}