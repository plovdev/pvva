package org.plovdev.pvva.transforms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.configs.httpconfig.HeadersConfig;
import org.plovdev.pvva.models.configs.httpconfig.HttpClientType;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.httpconfig.RetryPolicy;
import org.plovdev.pvva.utils.PVVAJsonSerializer;
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

    public static final String HTTP_CLIENT = "http-client";
    public static final String HEADERS = "headers";
    public static final String RANDOM = "random";
    public static final String SET = "set";

    public static final String TIMEOUTS = "timeouts";
    public static final String CONNECT_TIMEOUT = "connect-timeout";
    public static final String READ_TIMEOUT = "read-timeout";
    public static final String WRITE_TIMEOUT = "write-timeout";

    public static final String RETRY = "retry";
    public static final String RETRY_COUNT = "retry-count";
    public static final String RETRY_POLICY = "retry-policy";
    public static final String RETRY_DELAY = "retry-delay";

    private HttpConfigTransformer() {
    }

    public static @NonNull HttpConfig ofJson(String json) {
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        // HTTP client
        String httpClient = extractString(HTTP_CLIENT, root);

        // Headers config
        JsonObject headersObj = root.getAsJsonObject(HEADERS);
        boolean random = extractBoolean(RANDOM, headersObj);

        List<Map<String, String>> headerSets = new ArrayList<>();
        JsonArray headersSet = headersObj.getAsJsonArray(SET);
        headersSet.forEach(headersElement -> {
            JsonObject headerSet = headersElement.getAsJsonObject();
            Map<String, String> headers = new HashMap<>();
            headerSet.entrySet().forEach(entry -> headers.put(entry.getKey(), entry.getValue().getAsString()));
            headerSets.add(headers);
        });
        HeadersConfig headersConfig = new HeadersConfig(random, headerSets);

        // Timeouts
        JsonObject timeouts = root.getAsJsonObject(TIMEOUTS);
        long connectTimeout = timeouts.has(CONNECT_TIMEOUT) ? timeouts.get(CONNECT_TIMEOUT).getAsLong() : 0;
        long readTimeout = timeouts.has(READ_TIMEOUT) ? timeouts.get(READ_TIMEOUT).getAsLong() : 0;
        long writeTimeout = timeouts.has(WRITE_TIMEOUT) ? timeouts.get(WRITE_TIMEOUT).getAsLong() : 0;

        // Retry
        JsonObject retry = root.getAsJsonObject(RETRY);
        RetryPolicy retryPolicy = RetryPolicy.valueOf(extractString(RETRY_POLICY, retry));
        long retryDelay = retry.has(RETRY_DELAY) ? retry.get(RETRY_DELAY).getAsLong() : 0;
        int retryCount = retry.has(RETRY_COUNT) ? retry.get(RETRY_COUNT).getAsInt() : 0;

        return new HttpConfig(HttpClientType.safeValueOf(httpClient), headersConfig, retryPolicy, connectTimeout, readTimeout, writeTimeout, retryDelay, retryCount);
    }

    public static String toJson(@NonNull HttpConfig config) {
        return toJson(config, false);
    }

    public static String toJson(@NonNull HttpConfig config, boolean prettyPrint) {
        JsonObject root = new JsonObject();

        // HTTP client
        root.addProperty(HTTP_CLIENT, config.httpClient().name());

        // Headers config
        JsonObject headersObj = new JsonObject();
        config.headersConfig().ifPresent(headers -> {
            headersObj.addProperty(RANDOM, headers.random());
            if (headers.headerSets().isPresent()) {
                JsonArray headersSet = new JsonArray();
                for (Map<String, String> headerSet : headers.headerSets().get()) {
                    JsonObject headerObj = new JsonObject();
                    headerSet.forEach(headerObj::addProperty);
                    headersSet.add(headerObj);
                }
                headersObj.add(SET, headersSet);
            }
        });
        root.add(HEADERS, headersObj);

        // Timeouts
        JsonObject timeouts = new JsonObject();
        timeouts.addProperty(CONNECT_TIMEOUT, config.connectTimeout());
        timeouts.addProperty(READ_TIMEOUT, config.readTimeout());
        timeouts.addProperty(WRITE_TIMEOUT, config.writeTimeout());
        root.add(TIMEOUTS, timeouts);

        // Retry
        JsonObject retry = new JsonObject();
        retry.addProperty(RETRY_COUNT, config.retryCount());
        retry.addProperty(RETRY_POLICY, config.retryPolicy().name());
        retry.addProperty(RETRY_DELAY, config.retryDelay());
        root.add(RETRY, retry);

        if (prettyPrint) {
            return PVVAJsonSerializer.GLOBAL_JSON_PP.toJson(root);
        } else {
            return PVVAJsonSerializer.GLOBAL_JSON.toJson(root);
        }
    }
}