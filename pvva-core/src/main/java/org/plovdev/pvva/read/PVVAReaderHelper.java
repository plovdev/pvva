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
        boolean hasSign = buffer.get() != 0;
        int buildId = buffer.getInt();

        byte idlength = buffer.get();
        int minAppVersion = buffer.getInt();
        int maxAppVersion = buffer.getInt();
        int tableOffset = buffer.getInt();

        ByteBuffer idBuffer = ByteBuffer.allocate(idlength);
        int readId = pvvaReader.read(idBuffer);
        if (readId != idlength) {
            throw new IllegalStateException("Invalid plugin id bytes read: " + readId + "/" + idlength);
        }
        idBuffer.flip();
        byte[] plugId = new byte[idlength];
        idBuffer.get(plugId);
        String pluginId = new String(plugId, StandardCharsets.US_ASCII);

        return new PVVAHeader(version, flag, hasSign, buildId, idlength, minAppVersion, maxAppVersion, tableOffset, pluginId);
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
            byte idLen = tableBody.get();
            int entryOffset = tableBody.getInt();

            byte[] entryIdBytes = new byte[idLen];
            tableBody.get(entryIdBytes);
            String entryId = new String(entryIdBytes, StandardCharsets.US_ASCII);
            entryMap.put(entryId, new TableEntry(idLen, entryOffset, entryId));
        }

        return entryMap;
    }

    @Contract("_, _ -> new")
    public static @NonNull Chunk readChunk(int chunkOffset, @NonNull FileChannel pvvaReader) throws IOException {
        pvvaReader.position(chunkOffset);
        ByteBuffer chunkHeader = ByteBuffer.allocate(9);
        int headerRead = pvvaReader.read(chunkHeader);
        if (headerRead != 9) {
            throw new IllegalStateException("Read not 9 bytes");
        }
        chunkHeader.flip();

        byte[] chunkTypeBytes = new byte[4];
        chunkHeader.get(chunkTypeBytes);
        int chunkSize = chunkHeader.getInt();
        byte chunkIdLength = chunkHeader.get();

        int bodySize = chunkIdLength + chunkSize;
        ByteBuffer chunkBody = ByteBuffer.allocate(bodySize);
        int bodyRead = pvvaReader.read(chunkBody);
        if (bodyRead != bodySize) {
            throw new IllegalStateException("Read not " + bodyRead + " bytes in chunk body");
        }
        chunkBody.flip();

        byte[] chunkIdBytes = new byte[chunkIdLength];
        chunkBody.get(chunkIdBytes);

        byte[] compressedChunkContent = new byte[chunkSize];
        chunkBody.get(compressedChunkContent);

        String chunkTypeStr = new String(chunkTypeBytes, StandardCharsets.US_ASCII);
        String chunkId = new String(chunkIdBytes, StandardCharsets.US_ASCII);
        byte[] chunkContent = DataCompressor.decompress(compressedChunkContent);

        return new Chunk(ChunkType.ofString(chunkTypeStr), chunkSize, chunkIdLength, chunkId, chunkContent);
    }
}