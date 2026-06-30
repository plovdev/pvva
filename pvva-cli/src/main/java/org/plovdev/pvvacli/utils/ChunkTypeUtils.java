package org.plovdev.pvvacli.utils;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.chunks.ChunkType;

import java.nio.file.Path;

import static org.plovdev.pvvacli.PvvaPaths.*;

public final class ChunkTypeUtils {
    private ChunkTypeUtils() {
    }

    public static ChunkType determineChunkType(@NonNull Path relativePath) {
        if (relativePath.getNameCount() < 2) {
            return ChunkType.SYSTEM;
        }

        Path src = relativePath.getName(0);
        if (!src.equals(SRC_PATH.getFileName())) {
            return ChunkType.SYSTEM;
        }

        Path second = relativePath.getName(1);

        if (second.equals(CONFIGS.getFileName())) {
            return ChunkType.CONFIG;
        }

        if (second.equals(RESOURCES.getFileName())) {
            return ChunkType.RESOURCE;
        }

        if (second.equals(SCRIPTS.getFileName())) {
            return ChunkType.SCRIPT;
        }

        return ChunkType.SYSTEM;
    }
}