package org.plovdev.pvva.read;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.chunks.ChunkType;
import org.plovdev.pvva.models.table.TableEntry;
import org.plovdev.pvva.utils.DataCompressor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.plovdev.pvva.models.PVVAHeader.HEADER_SIZE;

public final class PVVAReaderHelper {
    private PVVAReaderHelper() {
    }

    public static @NonNull PVVAHeader fillHeaderFromBuffer(@NonNull FileChannel pvvaReader) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        int readBuffer = pvvaReader.read(buffer);
        if (readBuffer != HEADER_SIZE) {
            throw new IllegalStateException("Invalid header bytes read: " + readBuffer + "/" + HEADER_SIZE);
        }

        buffer.flip();
        byte version = buffer.get();
        byte flag = buffer.get();
        int buildId = buffer.getInt();

        byte idLength = buffer.get();
        int minAppVersion = buffer.getInt();
        int maxAppVersion = buffer.getInt();
        int tableOffset = buffer.getInt();

        ByteBuffer idBuffer = ByteBuffer.allocate(idLength);
        int readId = pvvaReader.read(idBuffer);
        if (readId != idLength) {
            throw new IllegalStateException("Invalid plugin id bytes read: " + readId + "/" + idLength);
        }
        idBuffer.flip();
        byte[] plugId = new byte[idLength];
        idBuffer.get(plugId);
        String pluginId = new String(plugId, StandardCharsets.US_ASCII);

        return new PVVAHeader(version, flag, buildId, idLength, minAppVersion, maxAppVersion, tableOffset, pluginId);
    }

    public static @NonNull Map<String, TableEntry> readTableEntries(short tableSize, @NonNull FileChannel pvvaReader) throws IOException {
        ByteBuffer tableBody = ByteBuffer.allocate(tableSize);
        int tableBodyRead = pvvaReader.read(tableBody);
        if (tableBodyRead != tableSize) {
            throw new IllegalStateException("Read not " + tableSize + " bytes in table body");
        }

        tableBody.flip();
        Map<String, TableEntry> entryMap = new HashMap<>();

        while (tableBody.hasRemaining()) {
            byte[] chunkTypeBytes = new byte[4];
            tableBody.get(chunkTypeBytes);
            ChunkType chunkType = ChunkType.ofString(new String(chunkTypeBytes, StandardCharsets.US_ASCII));

            int chunkSize = tableBody.getInt();
            byte idLen = tableBody.get();
            int entryOffset = tableBody.getInt();

            byte[] entryIdBytes = new byte[idLen];
            tableBody.get(entryIdBytes);
            String entryId = new String(entryIdBytes, StandardCharsets.US_ASCII);

            entryMap.put(entryId, new TableEntry(chunkType, chunkSize, idLen, entryOffset, entryId));
        }

        return entryMap;
    }

    @Contract("_, _ -> new")
    public static @NonNull Chunk readChunk(@NonNull TableEntry entry, @NonNull FileChannel pvvaReader) throws IOException {
        pvvaReader.position(entry.entryOffset());

        int chunkSize = entry.chunkSize();
        ByteBuffer chunkBody = ByteBuffer.allocate(chunkSize);
        int bodyRead = pvvaReader.read(chunkBody);
        if (bodyRead != chunkSize) {
            throw new IllegalStateException("Read not " + bodyRead + " bytes in chunk body");
        }
        chunkBody.flip();

        byte[] compressedChunkContent = new byte[chunkSize];
        chunkBody.get(compressedChunkContent);
        byte[] chunkContent = DataCompressor.decompress(compressedChunkContent);

        return new Chunk(entry.chunkType(), chunkSize, entry.entryIdLength(), entry.entryId(), chunkContent);
    }

    public static @NonNull ByteBuffer getChannelData(@NonNull FileChannel channel) throws IOException {
        long position = channel.position();
        channel.position(0);
        ByteBuffer buffer = ByteBuffer.allocate((int) position);
        channel.read(buffer);
        channel.position(position);
        return buffer.flip();
    }
}