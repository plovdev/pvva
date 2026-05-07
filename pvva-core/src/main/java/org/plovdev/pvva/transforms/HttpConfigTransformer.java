package org.plovdev.pvva.transforms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractBoolean;
import static org.plovdev.pvva.utils.PVVAJsonSerializer.extractString;

public final class HttpConfigTransformer {
    private static final Logger log = LoggerFactory.getLogger(HttpConfigTransformer.class);

    private HttpConfigTransformer() {
    }

    public static @NonNull HttpConfig ofJson(String json) {
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        // HTTP client
        String httpClient = extractString("http-client", root);

        // Headers config
        JsonObject headersObj = root.getAsJsonObject("headers");
        boolean random = extractBoolean("random", headersObj);

        List<Map<String, String>> headerSets = new ArrayList<>();

        for (String key : headersObj.keySet()) {
            if (key.startsWith("set")) {
                JsonArray setArray = headersObj.getAsJsonArray(key);
                for (var element : setArray) {
                    JsonObject headerSet = element.getAsJsonObject();
                    Map<String, String> headers = new HashMap<>();
                    headerSet.entrySet().forEach(entry ->
                            headers.put(entry.getKey(), entry.getValue().getAsString())
                    );
                    headerSets.add(headers);
                }
            }
        }

        HeadersConfig headersConfig = new HeadersConfig(random, headerSets);

        // Timeouts
        JsonObject timeouts = root.getAsJsonObject("timeouts");
        long connectTimeout = timeouts.has("connect-timeout") ? timeouts.get("connect-timeout").getAsLong() : 0;
        long readTimeout = timeouts.has("read-timeout") ? timeouts.get("read-timeout").getAsLong() : 0;
        long writeTimeout = timeouts.has("write-timeout") ? timeouts.get("write-timeout").getAsLong() : 0;

        // Retry
        JsonObject retry = root.getAsJsonObject("retry");
        RetryPolicy retryPolicy = RetryPolicy.valueOf(extractString("retry-policy", retry));
        long retryDelay = retry.has("retry-delay") ? retry.get("retry-delay").getAsLong() : 0;
        int retryCount = retry.has("retry-count") ? retry.get("retry-count").getAsInt() : 0;

        return new HttpConfig(httpClient, headersConfig, retryPolicy, connectTimeout, readTimeout, writeTimeout, retryDelay, retryCount);
    }

    public static String toJson(@NonNull HttpConfig config) {
        JsonObject root = new JsonObject();

        // HTTP client
        config.httpClient().ifPresent(client -> root.addProperty("http-client", client));

        // Headers config
        JsonObject headersObj = new JsonObject();
        config.headersConfig().ifPresent(headers -> {
            headersObj.addProperty("random", headers.random());

            // Сохраняем наборы заголовков как set, set2, set3...
            int setCounter = 1;
            if (headers.headerSets().isPresent()) {
                for (Map<String, String> headerSet : headers.headerSets().get()) {
                    JsonArray setArray = new JsonArray();
                    JsonObject headerObj = new JsonObject();
                    headerSet.forEach(headerObj::addProperty);
                    setArray.add(headerObj);

                    String key = setCounter == 1 ? "set" : "set" + setCounter;
                    headersObj.add(key, setArray);
                    setCounter++;
                }
            }
        });
        root.add("headers", headersObj);

        // Timeouts
        JsonObject timeouts = new JsonObject();
        timeouts.addProperty("connect-timeout", config.connectTimeout());
        timeouts.addProperty("connect-timeout", config.connectTimeout());
        timeouts.addProperty("connect-timeout", config.connectTimeout());
        root.add("timeouts", timeouts);

        // Retry
        JsonObject retry = new JsonObject();
        retry.addProperty("retry-count", config.retryCount());
        retry.addProperty("retry-policy", config.retryPolicy().name());
        retry.addProperty("retry-delay", config.retryDelay());
        root.add("retry", retry);

        return org.plovdev.pvva.utils.PVVAJsonSerializer.GLOBAL_JSON.toJson(root);
    }
}