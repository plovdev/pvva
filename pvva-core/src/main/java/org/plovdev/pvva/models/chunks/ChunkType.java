package org.plovdev.pvva.models.chunks;

import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;

public enum ChunkType {
    SYSTEM("SYST"), CONFIG("CONF"), SCRIPT("SRPT"), RESOURCE("RESC");

    public static @NonNull ChunkType ofString(String str) {
        for (ChunkType type : values()) {
            if (type.getType().equals(str)) {
                return type;
            }
        }
        throw new NoSuchElementException("Unkonown chunk type: " + str);
    }

    private final String type;

    ChunkType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}