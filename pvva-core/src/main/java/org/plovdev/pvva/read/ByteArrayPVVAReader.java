package org.plovdev.pvva.read;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
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
import org.plovdev.pvva.utils.DataCompressor;
import org.plovdev.pvva.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ByteArrayPVVAReader implements PVVAReader {
    private static final Logger log = LoggerFactory.getLogger(ByteArrayPVVAReader.class);
    private static final byte SUPPORTED_VERSION = 2;
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
        if (isValidChecked.compareAndSet(false, true)) {
            synchronized (this) {
                byte[] magic = new byte[4];
                pvvaReader.get(magic);
                boolean validPvva = Arrays.equals(magic, PVVAHeader.MAGIC_NUMBER_BYTES);
                isValid.set(validPvva);
                return validPvva;
            }
        } else {
            return isValid.get();
        }
    }

    @Override
    public synchronized boolean isCompatibleWithAppVersion(int appVersion) {
        PVVAHeader header = readedHeader.get();
        if (header == null) {
            header = parseHeader();
        }

        int minAppVersion = header.getMinAppVersion();
        int maxAppVersion = header.getMaxAppVersion();
        return VersionUtils.isCompatibleWithAppVersion(minAppVersion, maxAppVersion, appVersion);
    }

    @Override
    public synchronized PVVAHeader parseHeader() {
        if (readedHeader.get() != null) {
            return readedHeader.get();
        }

        if (checkMagic()) {
            pvvaReader.position(4);
            PVVAHeader header = readHeader();
            if (header.getVersion() != SUPPORTED_VERSION) {
                throw new IllegalArgumentException("Plugin version is not supported by this reader. Plugin version: " + header.getVersion() + " reader version: " + supportedVersion());
            }
            if (header.getFlag() != SUPPORTED_FLAG) {
                throw new IllegalArgumentException("Plugin flag is not supported by this reader. Plugin flag: " + header.getFlag() + " reader flag: " + supportedFlag());
            }

            readedHeader.set(header);
            return header;
        } else {
            throw new IllegalArgumentException("Not a pvva file.");
        }
    }

    private @NonNull PVVAHeader readHeader() {
        byte version = pvvaReader.get();
        byte flag = pvvaReader.get();
        int buildId = pvvaReader.getInt();

        byte idLength = pvvaReader.get();
        int minAppVersion = pvvaReader.getInt();
        int maxAppVersion = pvvaReader.getInt();
        int tableOffset = pvvaReader.getInt();

        byte[] pluginIdBytes = new byte[idLength];
        pvvaReader.get(pluginIdBytes);
        String pluginId = new String(pluginIdBytes, StandardCharsets.US_ASCII);

        return new PVVAHeader(version, flag, buildId, idLength, minAppVersion, maxAppVersion, tableOffset, pluginId);
    }

    @Override
    public synchronized EntriesOffsetTable parseOffsetTable() {
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
                int tableOffsetField = PVVAHeader.HEADER_SIZE;
                pvvaReader.position(tableOffsetField);
                tableOffset = pvvaReader.getInt();
            } else {
                throw new IllegalArgumentException("Not a pvva file.");
            }
        }

        pvvaReader.position(tableOffset);
        short tableSize = pvvaReader.getShort();
        byte entriesCount = pvvaReader.get();
        EntriesOffsetTable table = new EntriesOffsetTable(tableSize, entriesCount, readTableEntries(tableSize));
        readedOffsetTable.set(table);
        return table;
    }

    private @NonNull Map<String, TableEntry> readTableEntries(short tableSize) {
        ByteBuffer tableEntriesBuffer = pvvaReader.slice(pvvaReader.position(), tableSize);
        Map<String, TableEntry> entryMap = new HashMap<>();

        while (tableEntriesBuffer.hasRemaining()) {
            byte[] chunkTypeBytes = new byte[4];
            tableEntriesBuffer.get(chunkTypeBytes);
            ChunkType chunkType = ChunkType.ofString(new String(chunkTypeBytes, StandardCharsets.US_ASCII));

            int chunkSize = tableEntriesBuffer.getInt();
            byte idLen = tableEntriesBuffer.get();
            int entryOffset = tableEntriesBuffer.getInt();

            byte[] entryIdBytes = new byte[idLen];
            tableEntriesBuffer.get(entryIdBytes);
            String entryId = new String(entryIdBytes, StandardCharsets.US_ASCII);

            entryMap.put(entryId, new TableEntry(chunkType, chunkSize, idLen, entryOffset, entryId));
        }

        return entryMap;
    }

    @Override
    public synchronized PVVAHost readVideoAdapter() {
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
        if (signature == null) {
            signature = readSignature();
        }
        log.debug("Signature parsed");

        PVVAHost host = new PVVAHost(header, table, pluginJson, resourceConfig, httpConfig, mainParser, chunks, signature);
        readedHost.set(host);
        return host;
    }

    @Override
    public boolean hasChunk(String chunkId) {
        return getAvailableChunkIds().contains(chunkId);
    }

    @Override
    public Chunk extractChunk(String chunkId) {
        return readedChunks.computeIfAbsent(chunkId, (id) -> {
            EntriesOffsetTable table = readedOffsetTable.get();
            if (table == null) {
                table = parseOffsetTable();
            }
            Map<String, TableEntry> entryMap = table.entries();
            if (entryMap.containsKey(id)) {
                return readChunk(entryMap.get(id));
            } else {
                throw new NoSuchElementException("Can't find chunk " + id);
            }
        });
    }

    @Contract("_ -> new")
    private @NonNull Chunk readChunk(@NonNull TableEntry entry) {
        int offset = entry.entryOffset();
        pvvaReader.position(offset);

        int chunkSize = entry.chunkSize();
        byte[] compressedChunkContent = new byte[chunkSize];
        pvvaReader.get(compressedChunkContent);
        byte[] chunkContent = DataCompressor.decompress(compressedChunkContent);

        return new Chunk(entry.chunkType(), chunkSize, entry.entryIdLength(), entry.entryId(), chunkContent);
    }

    @Override
    public synchronized Set<String> getAvailableChunkIds() {
        EntriesOffsetTable table = readedOffsetTable.get();
        if (table == null) {
            table = parseOffsetTable();
        }
        return table.entries().keySet();
    }

    @Override
    public synchronized byte[] readSignature() {
        log.debug("Reading adapter signature");
        if (readedSignature.get() != null) {
            return readedSignature.get();
        }

        PVVAHeader header = readedHeader.get();
        if (header == null) {
            header = parseHeader();
        }

        int totalSize = pvvaReader.capacity();
        int signOffset = totalSize - PVVAHeader.SIGN_SIZE;
        pvvaReader.position(signOffset);

        byte[] signature = new byte[PVVAHeader.SIGN_SIZE];
        pvvaReader.get(signature);
        readedSignature.set(signature);
        return signature;
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
    public synchronized ByteBuffer getReadData() {
        return pvvaReader.slice(0, pvvaReader.position()).flip().asReadOnlyBuffer();
    }

    @Override
    public synchronized void close() {
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