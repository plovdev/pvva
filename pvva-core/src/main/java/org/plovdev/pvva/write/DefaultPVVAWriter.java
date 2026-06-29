package org.plovdev.pvva.write;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.WritablePVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardOpenOption.*;

public class DefaultPVVAWriter implements PVVAWriter {
    private static final Logger log = LoggerFactory.getLogger(DefaultPVVAWriter.class);
    private final FileChannel pvvaWriter;

    private final AtomicBoolean isAdapterWrited = new AtomicBoolean(false);
    private final AtomicBoolean signatureAppened = new AtomicBoolean(false);

    public DefaultPVVAWriter(Path outputPath) throws IOException {
        this.pvvaWriter = FileChannel.open(outputPath, CREATE_NEW, READ, WRITE, TRUNCATE_EXISTING);
    }

    @Override
    public synchronized void writeVideoAdapter(@NonNull WritablePVVAHost host) throws IOException {
        if (isAdapterWrited.compareAndSet(false, true)) {
            log.debug("Writing adapter.");
            PVVAHeader header = host.header();

            writeMagic();
            int headerWritten = pvvaWriter.write(PVVAWriterHelper.preparePVVAHeaderBuffer(header));
            log.debug("Header bytes written: {}", headerWritten);

            writeOffsetTable(header, host);
            writeChunks(host.chunkMap());
        } else {
            log.warn("Adapter already writed");
        }
    }

    private void writeMagic() throws IOException {
        int written = pvvaWriter.write(ByteBuffer.wrap(PVVAHeader.MAGIC_NUMBER_BYTES));
        if (written != PVVAHeader.MAGIC_NUMBER.length()) {
            throw new IllegalStateException("Magic written is incorrect. Excepted 4 but written " + written);
        }
    }

    @Contract(pure = true)
    private void writeOffsetTable(@NonNull PVVAHeader header, @NonNull WritablePVVAHost host) throws IOException {
        int tableOffset = PVVAHeader.ABS_HEADER_SIZE + header.getIdlength();
        int tableBytesWritten = pvvaWriter.write(PVVAWriterHelper.prepareOffsetTableBuffer(tableOffset, host.chunkMap()));
        log.debug("Offset table bytes written: {}", tableBytesWritten);
    }

    private void writeChunks(@NonNull Map<String, Chunk> chunkMap) throws IOException {
        for (Chunk chunk : chunkMap.values()) {
            int chunkBytesWritten = pvvaWriter.write(PVVAWriterHelper.prepareChunkBuffer(chunk));
            log.debug("Chunk {} bytes written: {}", chunk.getChunkId(), chunkBytesWritten);
        }
    }

    @Override
    public synchronized ByteBuffer getWrittenData() throws IOException {
        long position = pvvaWriter.position();
        pvvaWriter.position(0);
        ByteBuffer buffer = ByteBuffer.allocate((int) position);
        pvvaWriter.read(buffer);
        pvvaWriter.position(position);
        return buffer.flip();
    }

    @Override
    public synchronized void appendSignature(byte[] signature) throws IOException {
        if (signatureAppened.compareAndSet(false, true)) {
            if (signature == null || signature.length != 64) {
                throw new IllegalArgumentException("Signature must be 64 bytes");
            }
            int written = pvvaWriter.write(ByteBuffer.wrap(signature));
            if (written != signature.length) {
                throw new IllegalStateException("Failed to write signature: " + written + "/" + signature.length + " bytes has been written");
            }
            log.debug("Signature appended, bytes: {}", written);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        isAdapterWrited.set(false);
        signatureAppened.set(false);

        pvvaWriter.force(true);
        pvvaWriter.close();
    }
}