package org.plovdev.pvva.models.configs.resourceconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class MainResources {
    private final boolean supports;
    private final String endpoint;
    private final boolean supportSearch;
    private final String searchUrl;

    public MainResources(
            boolean supports,
            @Nullable String endpoint,
            boolean supportSearch,
            @Nullable String searchUrl) {
        this.supports = supports;
        this.endpoint = endpoint;
        this.supportSearch = supportSearch;
        this.searchUrl = searchUrl;
    }

    public boolean supports() {
        return supports;
    }

    public @NonNull Optional<String> endpoint() {
        return Optional.ofNullable(endpoint);
    }

    public boolean supportSearch() {
        return supportSearch;
    }

    public @NonNull Optional<String> searchUrl() {
        return Optional.ofNullable(searchUrl);
    }

    @Override
    public @NonNull String toString() {
        return "MainResources{" +
                "supports=" + supports +
                ", endpoint='" + endpoint + '\'' +
                ", supportSearch=" + supportSearch +
                ", searchUrl='" + searchUrl + '\'' +
                '}';
    }
}