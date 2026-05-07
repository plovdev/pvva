package org.plovdev.pvva.models.configs.resourceconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ResourceConfig {
    private final String baseUrl;
    private final boolean supportVideo;
    private final String videoEndpoint;
    private final boolean supportMirrors;
    private final List<String> mirrors;
    private final MainResources mainResources;
    private final ModelsResources modelsResources;
    private final CategoriesResources categoriesResources;

    public ResourceConfig(
            @NonNull String baseUrl,
            boolean supportVideo,
            @NonNull String videoEndpoint,
            boolean supportMirrors,
            @Nullable List<String> mirrors,
            @NonNull MainResources mainResources,
            @Nullable ModelsResources modelsResources,
            @Nullable CategoriesResources categoriesResources) {

        this.baseUrl = Objects.requireNonNull(baseUrl);
        this.supportVideo = supportVideo;
        this.videoEndpoint = Objects.requireNonNull(videoEndpoint);
        this.supportMirrors = supportMirrors;
        this.mirrors = mirrors;
        this.mainResources = Objects.requireNonNull(mainResources);
        this.modelsResources = modelsResources;
        this.categoriesResources = categoriesResources;
    }

    public @NonNull String baseUrl() {
        return baseUrl;
    }

    public boolean supportVideo() {
        return supportVideo;
    }

    public @NonNull String videoEndpoint() {
        return videoEndpoint;
    }

    public boolean supportMirrors() {
        return supportMirrors;
    }

    public @NonNull Optional<List<String>> mirrors() {
        return Optional.ofNullable(mirrors);
    }

    public @NonNull MainResources mainResources() {
        return mainResources;
    }

    public @NonNull Optional<ModelsResources> modelsResources() {
        return Optional.ofNullable(modelsResources);
    }

    public @NonNull Optional<CategoriesResources> categoriesResources() {
        return Optional.ofNullable(categoriesResources);
    }

    @Override
    public @NonNull String toString() {
        return "ResourceConfig{" + "baseUrl='" + baseUrl + '\'' + ", supportVideo=" + supportVideo + ", videoEndpoint='" + videoEndpoint + '\'' + ", supportMirrors=" + supportMirrors + ", mirrors=" + mirrors + '}';
    }
}