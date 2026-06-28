package org.plovdev.pvva.models;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;

import java.util.Map;
import java.util.Optional;

public record PVVAHost(@NonNull PVVAHeader header,
                       @NonNull EntriesOffsetTable offsetTable,
                       @NonNull PluginJson pluginJson,
                       @NonNull ResourceConfig resourceConfig,
                       @Nullable HttpConfig httpConfig,
                       @NonNull MainParser mainParser,
                       @NonNull Map<String, Chunk> chunkMap,
                       byte @Nullable [] signature) {

    public @NonNull Optional<HttpConfig> optHttpConfig() {
        return Optional.ofNullable(httpConfig);
    }
}