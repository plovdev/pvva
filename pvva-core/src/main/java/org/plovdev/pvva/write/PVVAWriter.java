package org.plovdev.pvva.write;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.WritablePVVAHost;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface PVVAWriter extends AutoCloseable {
    void writeVideoAdapter(@NonNull WritablePVVAHost host) throws IOException;

    ByteBuffer getWrittenData() throws IOException;

    void appendSignature(byte[] signature) throws IOException;

    @Override
    void close() throws IOException;
}