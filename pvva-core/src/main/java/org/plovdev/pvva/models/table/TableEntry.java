package org.plovdev.pvva.models.table;

import org.plovdev.pvva.models.chunks.ChunkType;

public record TableEntry(ChunkType chunkType, int chunkSize, byte entryIdLength, int entryOffset, String entryId) {
}