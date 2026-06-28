package org.plovdev.pvva.write;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.plovdev.pvva.models.chunks.Chunk.*;
import static org.plovdev.pvva.utils.DataCompressor.compress;

public class PVVAWriter implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PVVAWriter.class);
    private final FileChannel writeChannel;
    private final Path writeSource;
    private final int compressLevel;
    private int tableOffset = 0;

    public PVVAWriter(Path path, int compressLevel) throws IOException {
        Objects.requireNonNull(path);
        writeSource = path;
        writeChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        this.compressLevel = compressLevel;
    }

    public Path getWriteSource() {
        return writeSource;
    }

    public void writeVideoAdapter(@NonNull PVVAHost pvvaHost) throws IOException {
        PVVAHeader header = pvvaHost.header();

        int magicWriten = writeChannel.write(ByteBuffer.wrap(PVVAHeader.MAGIC_NUMBER.getBytes(StandardCharsets.US_ASCII)));
        if (magicWriten != 4) throw new IOException("Magic number has been incorrect writen");

        writeHeader(pvvaHost, header);
        writeChunks(pvvaHost);
        writeOffsetTable(pvvaHost);

        if (header.isHasSign() && pvvaHost.signature() != null) {
            int signWriten = writeChannel.write(ByteBuffer.wrap(pvvaHost.signature()));
            log.trace("Signature bytes writen: {}", signWriten);
        }
    }

    private void writeHeader(@NonNull PVVAHost host, @NonNull PVVAHeader header) throws IOException {
        byte[] compressedJsonBytes = compress(PluginJsonTransformer.toJson(host.pluginJson()).getBytes(StandardCharsets.UTF_8), compressLevel);

        ByteBuffer buffer = ByteBuffer.allocate(PVVAHeader.HEADER_SIZE);
        buffer.put(header.getVersion());
        buffer.put(header.getFlag());
        buffer.put((byte) (header.isHasSign() ? 1 : 0));
        buffer.putInt(header.getBuildId());

        buffer.put(header.getIdlength());
        buffer.putInt(header.getMinAppVersion());
        buffer.putInt(header.getMaxAppVersion());
        buffer.putInt(compressedJsonBytes.length);

        tableOffset = PVVAHeader.ABS_HEADER_SIZE + header.getIdlength();
        buffer.putInt(tableOffset);

        int headerWriten = writeChannel.write(buffer.flip());
        log.debug("Header bytes writen: {}", headerWriten);
        int pluginIdWritten = writeChannel.write(ByteBuffer.wrap(header.getPluginId().getBytes()));

        int jsonWriten = writeChannel.write(ByteBuffer.wrap(compressedJsonBytes));
        log.debug("PluginJson bytes writen: {}", jsonWriten);
    }

    private void writeOffsetTable(@NonNull PVVAHost host) throws IOException {
        long currentPosition = writeChannel.position();
        Map<String, Chunk> chunksMap = host.chunkMap();
        short tableSize = (short) Math.min(chunksMap.values().stream().map(Chunk::getChunkIdLength).reduce((byte) 0, (a, b) -> (byte) (a + b)) * 5, Short.MAX_VALUE);
        byte entriesCount = (byte) Math.min(chunksMap.size(), 127);
        int tableChunkSize = 3 + tableSize;
        ByteBuffer tableBuffer = ByteBuffer.allocate(tableChunkSize);

        tableBuffer.putShort(tableSize);
        tableBuffer.put(entriesCount);

        int accumulatedOffset = tableOffset + tableChunkSize;

        for (String chunkId : chunksMap.keySet()) {
            Chunk chunk = chunksMap.get(chunkId);
            ByteBuffer entryHeader = ByteBuffer.allocate(5);
            entryHeader.put(chunk.getChunkIdLength());
            entryHeader.putInt(accumulatedOffset);
            int ehWriten = writeChannel.write(entryHeader.flip());
            ByteBuffer entryIdBuffer = ByteBuffer.allocate(chunk.getChunkIdLength());
            entryIdBuffer.put(chunk.getChunkId().getBytes(StandardCharsets.US_ASCII));
            int idWriten = writeChannel.write(entryIdBuffer.flip());
            accumulatedOffset += (9 + chunk.getChunkSize() + chunk.getChunkIdLength());
        }

        writeChannel.position(currentPosition);
    }

    private void writeChunks(@NonNull PVVAHost pvvaHost) throws IOException {
        for (Chunk chunk : pvvaHost.chunkMap().values()) {
            byte[] rcJsonBytes = compress(ResourceConfigTransformer.toJson(config).getBytes(StandardCharsets.UTF_8), compressLevel);
            ByteBuffer configBuffer = ByteBuffer.allocate(5);
            configBuffer.put((byte) RESOURCE_CONFIG.length());
            configBuffer.putInt(rcJsonBytes.length);
            int bufferWritten = writeChannel.write(configBuffer.flip());
            log.debug("ResourceConfig Buffer bytes written: {}", bufferWritten);
            int resourceNameWriten = writeChannel.write(ByteBuffer.wrap(RESOURCE_CONFIG.getBytes(StandardCharsets.US_ASCII)));
            int resourceWriten = writeChannel.write(ByteBuffer.wrap(rcJsonBytes));
            log.debug("Resource Config bytes writen: {}, rc name bytes written: {}", resourceWriten, resourceNameWriten);
        }

        writeResourceConfig(pvvaHost.resourceConfig());
        writeMainParser(pvvaHost.mainParser());
        Optional<HttpConfig> httpConfigOpt = pvvaHost.optHttpConfig();
        if (httpConfigOpt.isPresent()) {
            writeHttpConfig(httpConfigOpt.get());
        }
    }

    private void writeResourceConfig(@NonNull ResourceConfig config) throws IOException {
        byte[] rcJsonBytes = compress(ResourceConfigTransformer.toJson(config).getBytes(StandardCharsets.UTF_8), compressLevel);

        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) RESOURCE_CONFIG.length());
        configBuffer.putInt(rcJsonBytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("ResourceConfig Buffer bytes written: {}", bufferWritten);
        int resourceNameWriten = writeChannel.write(ByteBuffer.wrap(RESOURCE_CONFIG.getBytes(StandardCharsets.US_ASCII)));
        int resourceWriten = writeChannel.write(ByteBuffer.wrap(rcJsonBytes));
        log.debug("Resource Config bytes writen: {}, rc name bytes written: {}", resourceWriten, resourceNameWriten);
    }

    private void writeMainParser(MainParser parser) throws IOException {
        byte[] bytes = compress(ParserTransformer.toParser(parser).getBytes(StandardCharsets.UTF_8), compressLevel);

        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) MAIN_PARSER.length());
        configBuffer.putInt(bytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("MainParser Buffer bytes written: {}", bufferWritten);

        int nameWriten = writeChannel.write(ByteBuffer.wrap(MAIN_PARSER.getBytes(StandardCharsets.US_ASCII)));
        int parserWriten = writeChannel.write(ByteBuffer.wrap(bytes));
        log.debug("Main Parser bytes writen: {}, name bytes written: {}", parserWriten, nameWriten);
    }

    private void writeHttpConfig(HttpConfig config) throws IOException {
        byte[] hcBytes = compress(HttpConfigTransformer.toJson(config).getBytes(StandardCharsets.UTF_8), compressLevel);

        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) HTTP_CONFIG.length());
        configBuffer.putInt(hcBytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("HttpConfig Buffer bytes written: {}", bufferWritten);

        int httpNameWriten = writeChannel.write(ByteBuffer.wrap(HTTP_CONFIG.getBytes(StandardCharsets.US_ASCII)));
        int httpWriten = writeChannel.write(ByteBuffer.wrap(hcBytes));
        log.debug("HttpConfig bytes writen: {}, hc name bytes written: {}", httpWriten, httpNameWriten);
    }

    public synchronized ByteBuffer getWritedData() throws IOException {
        long position = writeChannel.position();
        writeChannel.position(0);
        ByteBuffer buffer = ByteBuffer.allocate((int) position);
        writeChannel.read(buffer);
        return buffer.flip();
    }

    public void appendSignature(byte[] sign) throws IOException {
        int writen = writeChannel.write(ByteBuffer.wrap(sign));
        if (writen != sign.length) {
            throw new IOException("Error to write sign.");
        }
    }

    @Override
    public void close() throws IOException {
        writeChannel.force(true);
        writeChannel.close();
    }
}