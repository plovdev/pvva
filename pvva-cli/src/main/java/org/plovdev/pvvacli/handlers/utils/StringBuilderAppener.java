package org.plovdev.pvvacli.handlers.utils;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class StringBuilderAppener {
    private static final int STRING_LENGTH = 50;

    @Contract(pure = true)
    public static void appendString(@NonNull StringBuilder builder, @NonNull String text, String value) {
        int dots = STRING_LENGTH - text.length();
        builder.append(text);
        builder.append(".".repeat(Math.max(0, dots))).append(": ");
        builder.append(value).append(";\n");
    }

    @Contract(pure = true)
    public static void appendString(@NonNull StringBuilder builder, @NonNull String text, int value) {
        appendString(builder, text, String.valueOf(value));
    }

    @Contract(pure = true)
    public static void appendString(@NonNull StringBuilder builder, @NonNull String text, byte value) {
        appendString(builder, text, String.valueOf(value));
    }

    @Contract(pure = true)
    public static void appendString(@NonNull StringBuilder builder, @NonNull String text, boolean value) {
        appendString(builder, text, value ? "Yes" : "No");
    }
}