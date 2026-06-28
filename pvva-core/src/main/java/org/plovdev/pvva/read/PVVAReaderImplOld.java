package org.plovdev.pvva.read;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.chunks.ChunkType;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.models.table.TableEntry;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.plovdev.pvva.utils.DataCompressor.decompress;

public class PVVAReaderImplOld implements AutoCloseable {
    private final FileChannel readChannel;
    private final Path readSource;

    public PVVAReaderImplOld(Path path) throws IOException {
        Objects.requireNonNull(path);
        readSource = path;
        readChannel = FileChannel.open(path, StandardOpenOption.READ);
    }

    public Path getReadSource() {
        return readSource;
    }

    public PVVAHost parseVideoAdapter() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((int) Files.size(readSource));
        readChannel.read(buffer);
        buffer.flip();
        int totalSize = buffer.limit();

        checkMagic(buffer);
        PVVAHeader header = readHeader(buffer);
        if (header.isHasSign()) {
            buffer.limit(totalSize - 64);
        }

        String pluginJson = readPluginJson(buffer, header.getJsonSize());
        int realPluginJsonSize = pluginJson.length();

        Map<String, Chunk> chunks = readAllChunks(buffer);
        ResourceConfig resourceConfig = null;
        HttpConfig httpConfig = null;
        MainParser mainParser = null;

        for (String chunkId : chunks.keySet()) {
            Chunk chunk = chunks.get(chunkId);
            String chunkContent = new String(decompress(chunk.getChunkContent()), StandardCharsets.UTF_8);
            chunk.setChunkSize(chunkContent.length());
            switch (chunkId) {
                case "resource-config":
                    resourceConfig = ResourceConfigTransformer.ofJson(chunkContent);
                    break;
                case "http-config":
                    httpConfig = HttpConfigTransformer.ofJson(chunkContent);
                    break;
                case "main-parser":
                    mainParser = ParserTransformer.ofParser(chunkContent);
            }
        }

        byte[] signature = null;
        if (header.isHasSign()) {
            buffer.limit(totalSize);
            signature = new byte[64];
            buffer.get(signature);
        }

        header.setJsonSize(realPluginJsonSize);
        return new PVVAHost(header, readOffsetTable(buffer, header.getTableOffset()), PluginJsonTransformer.ofJson(pluginJson), Objects.requireNonNull(resourceConfig), httpConfig, Objects.requireNonNull(mainParser), chunks, signature);
    }

    private @NonNull PVVAHeader readHeader(@NonNull ByteBuffer buffer) {
        byte version = buffer.get();
        byte flag = buffer.get();
        boolean hasSign = buffer.get() != 0;
        int buildId = buffer.getInt();

        byte idlength = buffer.get();
        int minAppVersion = buffer.getInt();
        int maxAppVersion = buffer.getInt();
        int pluginJsonSize = buffer.getInt();
        int tableOffset = buffer.getInt();

        byte[] plugId = new byte[idlength];
        buffer.get(plugId);
        String pluginId = new String(plugId);

        return new PVVAHeader(version, flag, hasSign, buildId, idlength, minAppVersion, maxAppVersion, pluginJsonSize, tableOffset, pluginId);
    }

    @Contract("_, _ -> new")
    private @NonNull EntriesOffsetTable readOffsetTable(ByteBuffer buffer, int tableOffset) throws IOException {
        readChannel.position(tableOffset);
        ByteBuffer sizeBuffer = ByteBuffer.allocate(EntriesOffsetTable.TABLE_HEADER_SIZE);
        readChannel.read(sizeBuffer);
        short tableSize = sizeBuffer.flip().getShort();
        byte entriesCount = sizeBuffer.get();
        ByteBuffer tableBodyBuffer = ByteBuffer.allocate(tableSize);
        readChannel.read(tableBodyBuffer);

        return new EntriesOffsetTable(tableSize, entriesCount, Map.of());
    }

    @Contract(pure = true)
    private @NonNull Map<String, TableEntry> readTableEntries(@NonNull ByteBuffer tableBody) {
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

    private @NonNull String readPluginJson(@NonNull ByteBuffer buffer, int jsonSize) {
        byte[] jsonBytes = new byte[jsonSize];
        buffer.get(jsonBytes);

        return new String(decompress(jsonBytes), StandardCharsets.UTF_8);
    }

    private @NonNull Map<String, Chunk> readAllChunks(@NonNull ByteBuffer buffer) {
        Map<String, Chunk> chunks = new HashMap<>();

        byte[] chunkTypeBytes = new byte[4];
        while (buffer.hasRemaining()) {
            buffer.get(chunkTypeBytes);
            String chunkTypeStr = new String(chunkTypeBytes, StandardCharsets.US_ASCII);

            byte chunkIdLength = buffer.get();
            int chunkSize = buffer.getInt();

            byte[] chunkId = new byte[chunkIdLength];
            buffer.get(chunkId);
            String chunkIdStr = new String(chunkId, StandardCharsets.US_ASCII);

            byte[] content = new byte[chunkSize];
            buffer.get(content);
            chunks.put(chunkIdStr, new Chunk(ChunkType.ofString(chunkTypeStr), chunkSize, chunkIdLength, chunkIdStr, content));
        }
        return chunks;
    }

    private void checkMagic(@NonNull ByteBuffer buffer) throws IOException {
        byte[] magic = new byte[4];
        buffer.get(magic);
        String magicStr = new String(magic, StandardCharsets.US_ASCII);
        if (!magicStr.equals(PVVAHeader.MAGIC_NUMBER)) {
            throw new IOException("Not a PVVA file!");
        }
    }

    @Override
    public void close() throws IOException {
        readChannel.close();
    }
}