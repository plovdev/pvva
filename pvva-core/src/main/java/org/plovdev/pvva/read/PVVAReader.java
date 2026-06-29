package org.plovdev.pvva.read;

import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.table.EntriesOffsetTable;

import java.io.IOException;
import java.util.Set;

public interface PVVAReader extends AutoCloseable {
    boolean checkMagic();

    boolean isCompatibleWithAppVersion(int appVersion);

    PVVAHeader parseHeader() throws IOException;

    EntriesOffsetTable parseOffsetTable() throws IOException;

    PVVAHost readVideoAdapter() throws IOException;

    boolean hasChunk(String chunkId);

    Chunk extractChunk(String chunkId) throws IOException;

    Set<String> getAvailableChunkIds();

    byte[] readSignature() throws IOException;

    byte supportedVersion();

    byte supportedFlag();

    @Override
    void close() throws IOException;
}