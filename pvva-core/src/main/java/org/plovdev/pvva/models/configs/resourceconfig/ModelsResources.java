package org.plovdev.pvva.models.configs.resourceconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ModelsResources {
    private final boolean supports;
    private final String endpoint;
    private final boolean supportModel;
    private final String modelEndpoint;
    private final boolean supportModelSearch;
    private final String modelSearchEndpoint;

    public ModelsResources(
            boolean supports,
            @Nullable String endpoint,
            boolean supportModel,
            @Nullable String modelEndpoint,
            boolean supportModelSearch,
            @Nullable String modelSearchEndpoint) {
        this.supports = supports;
        this.endpoint = endpoint;
        this.supportModel = supportModel;
        this.modelEndpoint = modelEndpoint;
        this.supportModelSearch = supportModelSearch;
        this.modelSearchEndpoint = modelSearchEndpoint;
    }

    public boolean supports() {
        return supports;
    }

    public @NonNull Optional<String> endpoint() {
        return Optional.ofNullable(endpoint);
    }

    public boolean supportModel() {
        return supportModel;
    }

    public @NonNull Optional<String> modelEndpoint() {
        return Optional.ofNullable(modelEndpoint);
    }

    public boolean supportModelSearch() {
        return supportModelSearch;
    }

    public @NonNull Optional<String> modelSearchEndpoint() {
        return Optional.ofNullable(modelSearchEndpoint);
    }

    @Override
    public @NonNull String toString() {
        return "ModelsResources{" +
                "supports=" + supports +
                ", endpoint='" + endpoint + '\'' +
                ", supportModel=" + supportModel +
                ", modelEndpoint='" + modelEndpoint + '\'' +
                ", supportModelSearch=" + supportModelSearch +
                ", modelSearchEndpoint='" + modelSearchEndpoint + '\'' +
                '}';
    }
}