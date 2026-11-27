package org.plovdev.pvva.models;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.utils.IOUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public record PVVAHost(@NonNull PVVAHeader header,
                       @NonNull EntriesOffsetTable offsetTable,
                       @NonNull PluginJson pluginJson,
                       @NonNull ResourceConfig resourceConfig,
                       @Nullable HttpConfig httpConfig,
                       @NonNull MainParser mainParser,
                       @NonNull Map<String, Chunk> chunkMap,
                       byte @Nullable [] signature) {

    public String getSystemPluginId() {
        String rawSystemId = header.getPluginId() + '\0' + pluginJson.developerId();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return IOUtils.formatHex(digest.digest(rawSystemId.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchElementException("Algorithm SHA-256 not found", e);
        }
    }

    public @NonNull Optional<HttpConfig> optHttpConfig() {
        return Optional.ofNullable(httpConfig);
    }
}