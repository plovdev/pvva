package org.plovdev.pvva.utils.vars;

import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;

public enum Variable {
    USER_INPUT("user-input"),
    CATEGORY("category"),
    MODEL_NAME("model-name"),
    VIDEO_ID("video-id"),
    PAGE("page");

    public static @NonNull Variable ofName(String name) {
        for (Variable variable : values()) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        throw new NoSuchElementException("Cann't find variable " + name);
    }

    private final String name;
    Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("${%s}", name);
    }
}