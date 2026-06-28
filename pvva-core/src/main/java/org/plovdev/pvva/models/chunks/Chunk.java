package org.plovdev.pvva.models.chunks;

import java.util.Arrays;

public class Chunk {
    public static final String RESOURCE_CONFIG = "resource-config";
    public static final String HTTP_CONFIG = "http-config";
    public static final String MAIN_PARSER = "main-parser";

    public static final int CHUNK_ID_LENGTH_LENGTH = 1;
    public static final int CHUNK_ID_LENGTH = 4;
    public static final int CHUNK_SIZE_LENGTH = 4;

    protected ChunkType chunkType;
    protected int chunkSize;
    protected byte chunkIdLength;
    protected String chunkId;
    protected byte[] chunkContent;

    public Chunk(ChunkType chunkType, int chunkSize, byte chunkIdLength, String chunkId, byte[] chunkContent) {
        this.chunkType = chunkType;
        this.chunkSize = chunkSize;
        this.chunkIdLength = chunkIdLength;
        this.chunkId = chunkId;
        this.chunkContent = chunkContent;
    }

    public Chunk() {
    }

    public ChunkType getChunkType() {
        return chunkType;
    }

    public void setChunkType(ChunkType chunkType) {
        this.chunkType = chunkType;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
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

    public byte[] getChunkContent() {
        return chunkContent;
    }

    public void setChunkContent(byte[] chunkContent) {
        this.chunkContent = chunkContent;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkType=" + chunkType +
                ", chunkSize=" + chunkSize +
                ", chunkIdLength=" + chunkIdLength +
                ", chunkId='" + chunkId + '\'' +
                ", chunkContent=" + Arrays.toString(chunkContent) +
                '}';
    }
}