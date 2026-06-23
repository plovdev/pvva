package org.plovdev.pvva.read;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.plovdev.pvva.utils.DataCompressor.decompress;

public class PVVAReader implements AutoCloseable {
    private final FileChannel readChannel;
    private final Path readSource;

    public PVVAReader(Path path) throws IOException {
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

        List<Chunk> chunks = readAllChunks(buffer);
        ResourceConfig resourceConfig = null;
        HttpConfig httpConfig = null;
        MainParser mainParser = null;

        for (Chunk chunk : chunks) {
            String chunkContent = new String(decompress(chunk.getChunkContent()), StandardCharsets.UTF_8);
            chunk.setChunkSize(chunkContent.length());
            switch (chunk.getChunkId()) {
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
        return new PVVAHost(header, PluginJsonTransformer.ofJson(pluginJson), Objects.requireNonNull(resourceConfig), httpConfig, Objects.requireNonNull(mainParser), signature);
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

        byte[] plugId = new byte[idlength];
        buffer.get(plugId);
        String pluginId = new String(plugId);

        return new PVVAHeader(version, flag, hasSign, buildId, idlength, pluginId, minAppVersion, maxAppVersion, pluginJsonSize);
    }

    private @NonNull String readPluginJson(@NonNull ByteBuffer buffer, int jsonSize) {
        byte[] jsonBytes = new byte[jsonSize];
        buffer.get(jsonBytes);

        return new String(decompress(jsonBytes), StandardCharsets.UTF_8);
    }

    private @NonNull List<Chunk> readAllChunks(@NonNull ByteBuffer buffer) {
        List<Chunk> chunks = new ArrayList<>();

        while (buffer.hasRemaining()) {
            byte chunkIdLength = buffer.get();
            int chunkSize = buffer.getInt();

            byte[] chunkId = new byte[chunkIdLength];
            buffer.get(chunkId);
            String chunkIdStr = new String(chunkId, StandardCharsets.US_ASCII);

            byte[] content = new byte[chunkSize];
            buffer.get(content);
            chunks.add(new Chunk(chunkIdLength, chunkIdStr, chunkSize, content) {
            });
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