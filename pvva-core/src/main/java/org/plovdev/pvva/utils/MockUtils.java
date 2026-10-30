package org.plovdev.pvva.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.configs.httpconfig.HttpClientType;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;

import java.util.HashMap;
import java.util.Map;

import static org.plovdev.pvva.transforms.HttpConfigTransformer.*;
import static org.plovdev.pvva.transforms.PluginJsonTransformer.*;
import static org.plovdev.pvva.transforms.ResourceConfigTransformer.*;

public class MockUtils {
    @Contract(pure = true)
    public static @NonNull String mockPluginJson() {
        JsonObject pluginJson = new JsonObject();

        JsonObject commons = new JsonObject();
        commons.addProperty(TITLE, "--no title--");
        commons.addProperty(VERSION, "--no version--");
        commons.addProperty(DESCRIPTION, "--no description--");
        pluginJson.add(COMMONS, commons);

        JsonObject legal = new JsonObject();
        legal.addProperty(AUTHOR, "--no author--");
        legal.addProperty(DEVELOPER_ID, "--no eveloper id--");
        legal.addProperty(LICENSE, "--no license--");
        pluginJson.add(LEGAL, legal);

        return PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(pluginJson);
    }

    public static @NonNull String mockResourceConfig() {
        JsonObject root = new JsonObject();
        root.addProperty(BASE_URL, "https://example.com");

        JsonObject main = new JsonObject();
        main.addProperty(SUPPORTS, false);
        main.addProperty(ENDPOINT, "/");
        JsonObject mainSearch = new JsonObject();
        mainSearch.addProperty(SUPPORTS, false);
        mainSearch.addProperty(ENDPOINT, "/");
        main.add(MAIN_SEARCH, mainSearch);
        root.add(MAIN, main);

        JsonObject models = new JsonObject();
        models.addProperty(SUPPORTS, false);
        models.addProperty(ENDPOINT, "/");
        JsonObject model = new JsonObject();
        model.addProperty(SUPPORTS, false);
        model.addProperty(ENDPOINT, "/");
        models.add(MODEL, model);
        JsonObject modelSearch = new JsonObject();
        modelSearch.addProperty(SUPPORTS, false);
        modelSearch.addProperty(ENDPOINT, "/");
        models.add(MODEL_SEARCH, modelSearch);
        root.add(MODELS, models);

        JsonObject categories = new JsonObject();
        categories.addProperty(SUPPORTS, false);
        categories.addProperty(ENDPOINT, "/");
        JsonObject category = new JsonObject();
        category.addProperty(SUPPORTS, false);
        category.addProperty(ENDPOINT, "/");
        categories.add(CATEGORY, category);
        root.add(CATEGORIES, categories);

        JsonObject video = new JsonObject();
        video.addProperty(SUPPORTS, false);
        video.addProperty(ENDPOINT, "/");
        root.add(VIDEO, video);

        JsonObject mirrors = new JsonObject();
        mirrors.addProperty(SUPPORTS, false);
        root.add(MIRRORS, mirrors);

        return PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(root);
    }

    public static @NonNull String mockHttpConfig() {
        JsonObject root = new JsonObject();
        root.addProperty(HTTP_CLIENT, HttpClientType.OK_HTTP_CLIENT.name());

        JsonObject headersObj = new JsonObject();
        headersObj.addProperty(RANDOM, false);
        JsonArray headersSet = new JsonArray();
        JsonObject headerObj = new JsonObject();
        mockHeaders().forEach(headerObj::addProperty);
        headersSet.add(headerObj);
        headersObj.add(SET, headersSet);
        root.add(HEADERS, headersObj);

        JsonObject timeouts = new JsonObject();
        timeouts.addProperty(CONNECT_TIMEOUT, 60000);
        root.add(TIMEOUTS, timeouts);

        JsonObject retry = new JsonObject();
        retry.addProperty(RETRY_COUNT, 3);
        retry.addProperty(RETRY_POLICY, RetryPolicy.ON_FAILED.name());
        retry.addProperty(RETRY_DELAY, 1000);
        root.add(RETRY, retry);

        return PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(root);
    }

    @Contract(pure = true)
    private static @NonNull Map<String, String> mockHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.put("Accept", "text/html,application/xml,application/json,text/plain-text");
        headers.put("Referer", "https://example.com");
        return headers;
    }

    public static @NonNull String mockMainParser() {
        return """
                function parseVideos(htmlstr)
                    local table = {}
                    return json.toJson(table)
                end
                
                function parseCategories(htmlstr)
                    local table = {}
                    return json.toJson(table)
                end
                
                function parseModels(htmlstr)
                    local table = {}
                    return json.toJson(table)
                end
                
                function parseVideoPage(htmlstr)
                    local table = {}
                    return json.toJson(table)
                end
                """;
    }

    @Contract(" -> new")
    public static @NonNull PVVAHost mockPVVAHost() {
        return new PVVAHost(new PVVAHeader((byte) 0, (byte) 0, 0, (byte) 0, -1, -1, 0, ""),
                new EntriesOffsetTable((short) 0, (byte) 0, Map.of()),
                PluginJsonTransformer.ofJson(mockPluginJson()), ResourceConfigTransformer.ofJson(mockResourceConfig()),
                HttpConfigTransformer.ofJson(mockHttpConfig()), new MainParser(mockMainParser()), Map.of(), new byte[64]);
    }
}