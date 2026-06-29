package org.plovdev.pvva.read;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.models.table.TableEntry;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultPVVAReader implements PVVAReader {
    private static final Logger log = LoggerFactory.getLogger(DefaultPVVAReader.class);
    private static final byte SUPPORTED_VERSION = 1;
    private static final byte SUPPORTED_FLAG = 0;

    private final FileChannel pvvaReader;

    private final AtomicBoolean isValidChecked = new AtomicBoolean(false);
    private final AtomicBoolean isValid = new AtomicBoolean(false);

    private final Map<String, Chunk> readedChunks = new ConcurrentHashMap<>();
    private final AtomicReference<PVVAHost> readedHost = new AtomicReference<>();
    private final AtomicReference<PVVAHeader> readedHeader = new AtomicReference<>();
    private final AtomicReference<EntriesOffsetTable> readedOffsetTable = new AtomicReference<>();
    private final AtomicReference<byte[]> readedSignature = new AtomicReference<>();

    public DefaultPVVAReader(@NonNull Path readSource) throws IOException {
        pvvaReader = FileChannel.open(readSource, StandardOpenOption.READ);
    }

    @Override
    public boolean checkMagic() {
        if (isValidChecked.compareAndSet(false, true)) {
            try {
                synchronized (this) {
                    ByteBuffer magicBuffer = ByteBuffer.allocate(4);
                    byte[] magic = new byte[4];
                    int read = pvvaReader.read(magicBuffer);
                    magicBuffer.flip().get(magic);
                    if (read != 4) {
                        throw new IllegalStateException();
                    }
                    boolean validPvva = Arrays.equals(magic, PVVAHeader.MAGIC_NUMBER_BYTES);
                    isValid.set(validPvva);
                    return validPvva;
                }
            } catch (IOException e) {
                log.error("Error check is pvva valid: ", e);
                return false;
            }
        } else {
            return isValid.get();
        }
    }

    @Override
    public synchronized boolean isCompatibleWithAppVersion(int appVersion) {
        try {
            PVVAHeader header = readedHeader.get();
            if (header == null) {
                header = parseHeader();
            }

            int minAppVersion = header.getMinAppVersion();
            int maxAppVersion = header.getMaxAppVersion();
            return (minAppVersion <= appVersion && appVersion >= maxAppVersion);
        } catch (Exception e) {
            log.error("Error check compatiblee versioins: ", e);
            return false;
        }
    }

    @Override
    public synchronized PVVAHeader parseHeader() throws IOException {
        if (readedHeader.get() != null) {
            return readedHeader.get();
        }

        if (checkMagic()) {
            pvvaReader.position(4);
            PVVAHeader header = PVVAReaderHelper.fillHeaderFromBuffer(pvvaReader);
            if (header.getVersion() != SUPPORTED_VERSION || header.getFlag() != SUPPORTED_FLAG) {
                throw new IllegalArgumentException("This video adapter not supported by this reader.");
            }
            readedHeader.set(header);
            return header;
        } else {
            throw new IllegalArgumentException("Not a pvva file.");
        }
    }

    @Override
    public synchronized EntriesOffsetTable parseOffsetTable() throws IOException {
        log.debug("Parsing offset table");
        if (readedOffsetTable.get() != null) {
            return readedOffsetTable.get();
        }

        int tableOffset;
        if (readedHeader.get() != null) {
            tableOffset = readedHeader.get().getTableOffset();
        } else {
            if (checkMagic()) {
                log.debug("Header not parsed, read table offset from file.");
                long tableOffsetField = 20;
                pvvaReader.position(tableOffsetField);
                ByteBuffer tableOffsetFieldBuffer = ByteBuffer.allocate(4);
                int read = pvvaReader.read(tableOffsetFieldBuffer);
                if (read != 4) {
                    throw new IllegalStateException("Read not 4 bytes");
                }
                tableOffsetFieldBuffer.flip();
                tableOffset = tableOffsetFieldBuffer.getInt();
            } else {
                throw new IllegalArgumentException("Not a pvva file.");
            }
        }

        pvvaReader.position(tableOffset);
        ByteBuffer tableHeader = ByteBuffer.allocate(3);
        int tableHeaderRead = pvvaReader.read(tableHeader);
        if (tableHeaderRead != 3) {
            throw new IllegalStateException("Read not 3 bytes in table header: " + tableHeaderRead);
        }

        tableHeader.flip();
        short tableSize = tableHeader.getShort();
        byte entriesCount = tableHeader.get();

        EntriesOffsetTable table = new EntriesOffsetTable(tableSize, entriesCount, PVVAReaderHelper.readTableEntries(tableSize, pvvaReader));
        readedOffsetTable.set(table);
        return table;
    }

    @Override
    public synchronized PVVAHost readVideoAdapter() throws IOException {
        if (readedHost.get() != null) {
            return readedHost.get();
        }

        PVVAHeader header = readedHeader.get();
        if (header == null) {
            header = parseHeader();
        }
        log.debug("Header parsed");

        EntriesOffsetTable table = readedOffsetTable.get();
        if (table == null) {
            table = parseOffsetTable();
        }
        log.debug("Offset Table parsed");

        Map<String, Chunk> chunks = new HashMap<>();
        Map<String, TableEntry> entryMap = table.entries();

        PluginJson pluginJson = null;
        ResourceConfig resourceConfig = null;
        HttpConfig httpConfig = null;
        MainParser mainParser = null;
        for (String chunkId : entryMap.keySet()) {
            Chunk extracted = extractChunk(chunkId);
            chunks.put(chunkId, extracted);
            String chunkContent = extracted.stringifyChunkContent();
            switch (chunkId) {
                case Chunk.PLUGIN_JSON:
                    pluginJson = PluginJsonTransformer.ofJson(chunkContent);
                    break;
                case Chunk.RESOURCE_CONFIG:
                    resourceConfig = ResourceConfigTransformer.ofJson(chunkContent);
                    break;
                case Chunk.HTTP_CONFIG:
                    httpConfig = HttpConfigTransformer.ofJson(chunkContent);
                    break;
                case Chunk.MAIN_PARSER:
                    mainParser = ParserTransformer.ofParser(chunkContent);
            }
        }

        Objects.requireNonNull(pluginJson);
        Objects.requireNonNull(resourceConfig);
        Objects.requireNonNull(mainParser);
        log.debug("Chunks readed");

        byte[] signature = readedSignature.get();
        if (signature == null && header.isHasSign()) {
            signature = readSignature();
        }
        log.debug("Signature parsed");

        PVVAHost host = new PVVAHost(header, table, pluginJson, resourceConfig, httpConfig, mainParser, chunks, signature);
        readedHost.set(host);
        return host;
    }

    @Override
    public boolean hasChunk(String chunkId) {
        try {
            EntriesOffsetTable table = readedOffsetTable.get();
            if (table == null) {
                table = parseOffsetTable();
            }
            return table.entries().containsKey(chunkId);
        } catch (IOException e) {
            log.error("Error check has chunk: ", e);
            return false;
        }
    }

    @Override
    public Chunk extractChunk(String chunkId) {
        return readedChunks.computeIfAbsent(chunkId, (id) -> {
            try {
                EntriesOffsetTable table = readedOffsetTable.get();
                if (table == null) {
                    table = parseOffsetTable();
                }
                Map<String, TableEntry> entryMap = table.entries();
                if (entryMap.containsKey(id)) {
                    return PVVAReaderHelper.readChunk(entryMap.get(id).entryOffset(), pvvaReader);
                } else {
                    throw new NoSuchElementException("Can't find chunk " + id);
                }
            } catch (Exception e) {
                log.error("Error ectract chunk {}: ", id, e);
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public Set<String> getAvailableChunkIds() {
        try {
            EntriesOffsetTable table = readedOffsetTable.get();
            if (table == null) {
                table = parseOffsetTable();
            }
            return table.entries().keySet();
        } catch (IOException e) {
            log.error("Error get available chunk ids: ", e);
            return Set.of();
        }
    }

    @Override
    public synchronized byte[] readSignature() throws IOException {
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
            long totalSize = pvvaReader.size();
            long signOffset = totalSize - signSize;
            pvvaReader.position(signOffset);
            ByteBuffer signBuffer = ByteBuffer.allocate(signSize);
            int signRead = pvvaReader.read(signBuffer);
            if (signRead != signSize) {
                throw new IllegalStateException("Read not 64 bytes in signature");
            }
            byte[] signature = new byte[64];
            signBuffer.flip().get(signature);
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
    public synchronized void close() throws IOException {
        log.debug("Closing read channel.");

        isValid.set(false);
        isValidChecked.set(false);

        readedHost.set(null);
        readedHeader.set(null);
        readedOffsetTable.set(null);
        readedSignature.set(null);
        readedChunks.clear();

        pvvaReader.close();
    }
}