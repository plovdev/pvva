package org.plovdev.pvva.models;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.chunks.Chunk;

import java.util.Map;

public record WritablePVVAHost(@NonNull PVVAHeader header, @NonNull Map<String, Chunk> chunkMap) {
}