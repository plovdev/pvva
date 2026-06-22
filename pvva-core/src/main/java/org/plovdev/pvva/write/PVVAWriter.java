package org.plovdev.pvva.write;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
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
import java.util.Objects;
import java.util.Optional;

public class PVVAWriter implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PVVAWriter.class);
    private final FileChannel writeChannel;
    private final Path writeSource;

    public PVVAWriter(Path path) throws IOException {
        Objects.requireNonNull(path);
        writeSource = path;
        writeChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
    }

    public Path getWriteSource() {
        return writeSource;
    }

    public void writeVideoAdapter(@NonNull PVVAHost pvvaHost) throws IOException {
        PVVAHeader header = pvvaHost.header();
        ByteBuffer buffer = ByteBuffer.allocate(PVVAHeader.HEADER_SIZE);
        buffer.put(PVVAHeader.MAGIC_NUMBER.getBytes(StandardCharsets.US_ASCII));

        writeHeader(buffer, header);
        int headerWritten = writeChannel.write(buffer);
        int pluginIdWritten = writeChannel.write(ByteBuffer.wrap(header.pluginId().getBytes()));
        log.debug("Headers bytes written: {}, plugin id written: {}", headerWritten, pluginIdWritten);

        byte[] jsonBytes = PluginJsonTransformer.toJson(pvvaHost.pluginJson()).getBytes(StandardCharsets.UTF_8);
        int jsonWriten = writeChannel.write(ByteBuffer.wrap(jsonBytes));
        log.debug("PluginJson bytes writen: {}", jsonWriten);

        writeChunks(pvvaHost);

        if (header.hasSign() && pvvaHost.signature() != null) {
            int signWriten = writeChannel.write(ByteBuffer.wrap(pvvaHost.signature()));
            log.trace("Signature bytes writen: {}", signWriten);
        }
    }

    private void writeHeader(@NonNull ByteBuffer buffer, @NonNull PVVAHeader header) {
        buffer.put(header.version());
        buffer.put(header.flag());
        buffer.put((byte) (header.hasSign() ? 1 : 0));
        buffer.putInt(header.buildId());

        buffer.put(header.idlength());
        buffer.putInt(header.minAppVersion());
        buffer.putInt(header.maxAppVersion());
        buffer.putInt(header.jsonSize());
        buffer.flip();
    }

    private void writeChunks(@NonNull PVVAHost pvvaHost) throws IOException {
        writeResourceConfig(pvvaHost.resourceConfig());
        writeMainParser(pvvaHost.mainParser());
        Optional<HttpConfig> httpConfigOpt = pvvaHost.optHttpConfig();
        if (httpConfigOpt.isPresent()) {
            writeHttpConfig(httpConfigOpt.get());
        }
    }

    private void writeResourceConfig(@NonNull ResourceConfig config) throws IOException {
        String rcname = "resource-config";
        byte[] rcJsonBytes = ResourceConfigTransformer.toJson(config).getBytes(StandardCharsets.UTF_8);
        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) rcname.length());
        configBuffer.putInt(rcJsonBytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("ResourceConfig Buffer bytes written: {}", bufferWritten);

        int resourceNameWriten = writeChannel.write(ByteBuffer.wrap(rcname.getBytes(StandardCharsets.US_ASCII)));
        int resourceWriten = writeChannel.write(ByteBuffer.wrap(rcJsonBytes));
        log.debug("Resource Config bytes writen: {}, rc name bytes written: {}", resourceWriten, resourceNameWriten);
    }

    private void writeMainParser(MainParser parser) throws IOException {
        String name = "main-parser";
        byte[] bytes = ParserTransformer.toParser(parser).getBytes(StandardCharsets.UTF_8);
        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) name.length());
        configBuffer.putInt(bytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("MainParser Buffer bytes written: {}", bufferWritten);

        int nameWriten = writeChannel.write(ByteBuffer.wrap(name.getBytes(StandardCharsets.US_ASCII)));
        int parserWriten = writeChannel.write(ByteBuffer.wrap(bytes));
        log.debug("Main Parser bytes writen: {}, name bytes written: {}", parserWriten, nameWriten);
    }

    private void writeHttpConfig(HttpConfig config) throws IOException {
        String hcname = "http-config";
        byte[] hcBytes = HttpConfigTransformer.toJson(config).getBytes(StandardCharsets.UTF_8);
        ByteBuffer configBuffer = ByteBuffer.allocate(5);
        configBuffer.put((byte) hcname.length());
        configBuffer.putInt(hcBytes.length);
        int bufferWritten = writeChannel.write(configBuffer.flip());
        log.debug("HttpConfig Buffer bytes written: {}", bufferWritten);

        int httpNameWriten = writeChannel.write(ByteBuffer.wrap(hcname.getBytes(StandardCharsets.US_ASCII)));
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
        writeChannel.close();
    }
}