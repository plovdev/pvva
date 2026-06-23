package org.plovdev.pvva.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import org.jspecify.annotations.NonNull;

public final class PVVAJsonSerializer {
    public static final GsonBuilder GLOBAL_JSON_BUILDER = new GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .disableHtmlEscaping();
    public static final Gson GLOBAL_JSON = GLOBAL_JSON_BUILDER.create();
    public static final Gson GLOBAL_JSON_PP = GLOBAL_JSON_BUILDER.setPrettyPrinting().create();

    public static String serialize(Object object) {
        return GLOBAL_JSON.toJson(object);
    }

    public static <T> T deserialize(String json, Class<T> cls) {
        return GLOBAL_JSON.fromJson(json, cls);
    }

    public static String extractString(String string, @NonNull JsonObject object) {
        String str = null;
        if (object.has(string)) {
            str = object.get(string).getAsString();
        }
        return str;
    }

    public static boolean extractBoolean(String string, @NonNull JsonObject object) {
        boolean bool = false;
        if (object.has(string)) {
            bool = object.get(string).getAsBoolean();
        }
        return bool;
    }
}