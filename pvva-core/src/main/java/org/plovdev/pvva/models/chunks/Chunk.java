package org.plovdev.pvva.models.chunks;

import java.util.Arrays;

public abstract class Chunk {
    public static final int CHUNK_ID_LENGTH_LENGTH = 1;
    public static final int CHUNK_ID_LENGTH = 4;
    public static final int CHUNK_SIZE_LENGTH = 4;

    private byte chunkIdLength;
    private String chunkId;
    private int chunkSize;
    private byte[] chunkContent;

    public Chunk(byte chunkIdLength, String chunkId, int chunkSize, byte[] chunkContent) {
        this.chunkIdLength = chunkIdLength;
        this.chunkId = chunkId;
        this.chunkSize = chunkSize;
        this.chunkContent = chunkContent;
    }

    public Chunk() {
    }

    public byte getChunkIdLength() {
        return chunkIdLength;
    }

    public void setChunkIdLength(byte chunkIdLength) {
        this.chunkIdLength = chunkIdLength;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public byte[] getChunkContent() {
        return chunkContent;
    }

    public void setChunkContent(byte[] chunkContent) {
        this.chunkContent = chunkContent;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkIdLength=" + chunkIdLength +
                ", chunkId='" + chunkId + '\'' +
                ", chunkSize=" + chunkSize +
                ", chunkContent=" + Arrays.toString(chunkContent) +
                '}';
    }
}