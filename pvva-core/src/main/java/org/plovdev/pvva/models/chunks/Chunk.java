package org.plovdev.pvva.models.chunks;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Chunk {
    public static final String PLUGIN_JSON = "plugin.json";
    public static final String RESOURCE_CONFIG = "resource-config.json";
    public static final String HTTP_CONFIG = "http-config.json";
    public static final String MAIN_PARSER = "main-parser.lua";

    public static final int CHUNK_ID_LENGTH_LENGTH = 1;
    public static final int CHUNK_ID_LENGTH = 4;
    public static final int CHUNK_SIZE_LENGTH = 4;

    protected ChunkType chunkType;
    protected int compressedChunkSize;
    protected byte chunkIdLength;
    protected String chunkId;
    protected byte[] chunkContent;

    public Chunk(ChunkType chunkType, int compressedChunkSize, byte chunkIdLength, String chunkId, byte[] chunkContent) {
        this.chunkType = chunkType;
        this.compressedChunkSize = compressedChunkSize;
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

    public int getCompressedChunkSize() {
        return compressedChunkSize;
    }

    public void setCompressedChunkSize(int compressedChunkSize) {
        this.compressedChunkSize = compressedChunkSize;
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

    public int getContentSize() {
        return chunkContent.length;
    }

    public String stringifyChunkContent() {
        return new String(chunkContent, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkType=" + chunkType +
                ", compressedChunkSize=" + compressedChunkSize +
                ", chunkIdLength=" + chunkIdLength +
                ", chunkId='" + chunkId + '\'' +
                ", chunkContent=" + Arrays.toString(chunkContent) +
                '}';
    }
}