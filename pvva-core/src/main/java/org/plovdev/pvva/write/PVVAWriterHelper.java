package org.plovdev.pvva.write;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.chunks.Chunk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class PVVAWriterHelper {
    private PVVAWriterHelper() {
    }

    public static @NonNull ByteBuffer preparePVVAHeaderBuffer(@NonNull PVVAHeader header) {
        ByteBuffer buffer = ByteBuffer.allocate(PVVAHeader.HEADER_SIZE + header.getIdlength());
        buffer.put(header.getVersion());
        buffer.put(header.getFlag());
        buffer.putInt(header.getBuildId());

        buffer.put(header.getIdlength());
        buffer.putInt(header.getMinAppVersion());
        buffer.putInt(header.getMaxAppVersion());
        buffer.putInt(PVVAHeader.ABS_HEADER_SIZE + header.getIdlength());
        buffer.put(header.getPluginId().getBytes(StandardCharsets.US_ASCII));

        return buffer.flip();
    }

    public static @NonNull ByteBuffer prepareOffsetTableBuffer(int tableOffset, @NonNull Map<String, Chunk> chunkMap) {
        int tableSize = 0;
        for (Chunk chunk : chunkMap.values()) {
            tableSize += 13 + chunk.getChunkIdLength(); // chunk type + chunk size + idLen + offset + id
        }
        tableSize = Math.min(tableSize, Short.MAX_VALUE);
        byte entriesCount = (byte) Math.min(chunkMap.size(), 127);

        ByteBuffer tableBuffer = ByteBuffer.allocate(3 + tableSize); // header + body
        tableBuffer.putShort((short) tableSize);
        tableBuffer.put(entriesCount);

        int currentOffset = tableOffset + 3 + tableSize;

        for (Chunk chunk : chunkMap.values()) {
            tableBuffer.put(chunk.getChunkType().getType().getBytes(StandardCharsets.US_ASCII));
            tableBuffer.putInt(chunk.getCompressedChunkSize());
            tableBuffer.put(chunk.getChunkIdLength());
            tableBuffer.putInt(currentOffset);
            tableBuffer.put(chunk.getChunkId().getBytes(StandardCharsets.US_ASCII));

            currentOffset += chunk.getCompressedChunkSize();
        }

        return tableBuffer.flip();
    }

    public static @NonNull ByteBuffer prepareChunkBuffer(@NonNull Chunk chunk) {
        ByteBuffer chunkBuffer = ByteBuffer.allocate(chunk.getCompressedChunkSize());
        chunkBuffer.put(chunk.getChunkContent());

        return chunkBuffer.flip();
    }
}