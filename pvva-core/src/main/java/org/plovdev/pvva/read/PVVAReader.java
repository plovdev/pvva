package org.plovdev.pvva.read;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
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

        checkMagic(buffer);
        PVVAHeader header = readHeader(buffer);
        PluginJson pluginJson = readPluginJson(buffer, header.jsonSize());
        List<Chunk> chunks = readAllChunks(buffer);

        ResourceConfig resourceConfig = null;
        HttpConfig httpConfig = null;
        MainParser mainParser = null;

        for (Chunk chunk : chunks) {
            String chunkContent = new String(chunk.getChunkContent(), StandardCharsets.UTF_8);
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

        return new PVVAHost(header, pluginJson, Objects.requireNonNull(resourceConfig), httpConfig, Objects.requireNonNull(mainParser));
    }

    private @NonNull PVVAHeader readHeader(@NonNull ByteBuffer buffer) {
        byte version = buffer.get();
        byte flag = buffer.get();
        int buildId = buffer.getInt();

        byte idlength = buffer.get();
        int minAppVersion = buffer.getInt();
        int maxAppVersion = buffer.getInt();

        int pluginJsonSize = buffer.getInt();
        byte[] plugId = new byte[idlength];
        buffer.get(plugId);
        String pluginId = new String(plugId);

        return new PVVAHeader(version, flag, buildId, idlength, pluginId, minAppVersion, maxAppVersion, pluginJsonSize);
    }

    private @NonNull PluginJson readPluginJson(@NonNull ByteBuffer buffer, int jsonSize) {
        byte[] jsonBytes = new byte[jsonSize];
        buffer.get(jsonBytes);

        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        return PluginJsonTransformer.ofJson(json);
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