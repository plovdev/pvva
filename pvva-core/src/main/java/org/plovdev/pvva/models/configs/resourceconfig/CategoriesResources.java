package org.plovdev.pvva.models.configs.resourceconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class CategoriesResources {
    private final boolean supports;
    private final String endpoint;
    private final boolean supportCategory;
    private final String categoryEndpoint;

    public CategoriesResources(
            boolean supports,
            @Nullable String endpoint,
            boolean supportCategory,
            @Nullable String categoryEndpoint) {
        this.supports = supports;
        this.endpoint = endpoint;
        this.supportCategory = supportCategory;
        this.categoryEndpoint = categoryEndpoint;
    }

    public boolean supports() {
        return supports;
    }

    public @NonNull Optional<String> endpoint() {
        return Optional.ofNullable(endpoint);
    }

    public boolean supportCategory() {
        return supportCategory;
    }

    public @NonNull Optional<String> categoryEndpoint() {
        return Optional.ofNullable(categoryEndpoint);
    }

    @Override
    public @NonNull String toString() {
        return "CategoriesResources{" +
                "supports=" + supports +
                ", endpoint='" + endpoint + '\'' +
                ", supportCategory=" + supportCategory +
                ", categoryEndpoint='" + categoryEndpoint + '\'' +
                '}';
    }
}