package org.plovdev.pvvacli.utils;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.chunks.ChunkType;
import org.plovdev.pvvacli.PvvaPaths;

import java.nio.file.Path;

public final class ChunkTypeUtils {
    private ChunkTypeUtils() {
    }

    public static ChunkType determineChunkType(@NonNull Path relativePath) {
        if (relativePath.getNameCount() < 2) {
            return ChunkType.SYSTEM;
        }

        Path src = relativePath.getName(0);
        if (!src.equals(PvvaPaths.SRC_PATH.getFileName())) {
            return ChunkType.SYSTEM;
        }

        Path second = relativePath.getName(1);

        if (second.equals(PvvaPaths.CONFIGS.getFileName())) {
            return ChunkType.CONFIG;
        }

        if (second.equals(PvvaPaths.RESOURCES.getFileName())) {
            return ChunkType.RESOURCE;
        }

        if (second.equals(PvvaPaths.SCRIPTS.getFileName())) {
            if (relativePath.getNameCount() > 2) {
                Path third = relativePath.getName(2);
                if (third.equals(PvvaPaths.PARSERS.getFileName())) {
                    return ChunkType.SCRIPT;
                }
            }
            return ChunkType.SYSTEM;
        }

        return ChunkType.SYSTEM;
    }
}