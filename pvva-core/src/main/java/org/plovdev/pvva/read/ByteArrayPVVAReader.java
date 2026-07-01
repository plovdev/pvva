package org.plovdev.pvva.read;

import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.models.table.TableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ByteArrayPVVAReader implements PVVAReader {
    private static final Logger log = LoggerFactory.getLogger(ByteArrayPVVAReader.class);
    private static final byte SUPPORTED_VERSION = 1;
    private static final byte SUPPORTED_FLAG = 0;

    private final ByteBuffer pvvaReader;
    private final AtomicBoolean isValidChecked = new AtomicBoolean(false);
    private final AtomicBoolean isValid = new AtomicBoolean(false);

    private final Map<String, Chunk> readedChunks = new ConcurrentHashMap<>();
    private final AtomicReference<PVVAHost> readedHost = new AtomicReference<>();
    private final AtomicReference<PVVAHeader> readedHeader = new AtomicReference<>();
    private final AtomicReference<EntriesOffsetTable> readedOffsetTable = new AtomicReference<>();
    private final AtomicReference<byte[]> readedSignature = new AtomicReference<>();

    public ByteArrayPVVAReader(byte[] pluginData) {
        pvvaReader = ByteBuffer.wrap(pluginData);
    }

    @Override
    public boolean checkMagic() {
        return false;
    }

    @Override
    public boolean isCompatibleWithAppVersion(int appVersion) {
        return false;
    }

    @Override
    public PVVAHeader parseHeader() throws IOException {
        return null;
    }

    @Override
    public EntriesOffsetTable parseOffsetTable() {
        return null;
    }

    @Override
    public PVVAHost readVideoAdapter() throws IOException {
        return null;
    }

    @Override
    public boolean hasChunk(String chunkId) {
        return getAvailableChunkIds().contains(chunkId);
    }

    @Override
    public Chunk extractChunk(String chunkId) throws IOException {
        return readedChunks.computeIfAbsent(chunkId, (id) -> {
            EntriesOffsetTable table = readedOffsetTable.get();
            if (table == null) {
                table = parseOffsetTable();
            }
            Map<String, TableEntry> entryMap = table.entries();
            if (entryMap.containsKey(id)) {
                //return PVVAReaderHelper.readChunk(entryMap.get(id).entryOffset(), pvvaReader);
                return null;
            } else {
                throw new NoSuchElementException("Can't find chunk " + id);
            }
        });
    }

    @Override
    public Set<String> getAvailableChunkIds() {
        EntriesOffsetTable table = readedOffsetTable.get();
        if (table == null) {
            table = parseOffsetTable();
        }
        return table.entries().keySet();
    }

    @Override
    public byte[] readSignature() throws IOException {
        log.debug("Reading adapter signature");
        if (readedSignature.get() != null) {
            return readedSignature.get();
        }

        PVVAHeader header = readedHeader.get();
        if (header == null) {
            header = parseHeader();
        }

        if (header.isHasSign()) {
            int signSize = 64;
            int totalSize = pvvaReader.capacity();
            int signOffset = totalSize - signSize;
            pvvaReader.position(signOffset);

            byte[] signature = new byte[64];
            pvvaReader.get(signature);
            readedSignature.set(signature);
            return signature;
        } else {
            return null;
        }
    }

    @Override
    public byte supportedVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    public byte supportedFlag() {
        return SUPPORTED_FLAG;
    }

    @Override
    public ByteBuffer getReadData() {
        return pvvaReader.slice(0, pvvaReader.position()).flip();
    }

    @Override
    public void close() {
        log.debug("Closing read channel.");

        isValid.set(false);
        isValidChecked.set(false);

        readedHost.set(null);
        readedHeader.set(null);
        readedOffsetTable.set(null);
        readedSignature.set(null);

        readedChunks.clear();
        pvvaReader.clear();
    }
}