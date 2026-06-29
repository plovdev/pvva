package org.plovdev.pvva.models;

import java.nio.charset.StandardCharsets;

public final class PVVAHeader {
    public static final String MAGIC_NUMBER = "PVVA";
    public static final byte[] MAGIC_NUMBER_BYTES = MAGIC_NUMBER.getBytes(StandardCharsets.US_ASCII);
    public static final int HEADER_SIZE = 20;
    public static final int ABS_HEADER_SIZE = MAGIC_NUMBER.length() + HEADER_SIZE;

    private byte version;
    private byte flag;
    private boolean hasSign;
    private int buildId;
    private byte idlength;
    private int minAppVersion;
    private int maxAppVersion;
    public int tableOffset;
    private String pluginId;

    public PVVAHeader(byte version, byte flag, boolean hasSign, int buildId, byte idlength, int minAppVersion, int maxAppVersion, int tableOffset, String pluginId) {
        this.version = version;
        this.flag = flag;
        this.hasSign = hasSign;
        this.buildId = buildId;
        this.idlength = idlength;
        this.minAppVersion = minAppVersion;
        this.maxAppVersion = maxAppVersion;
        this.tableOffset = tableOffset;
        this.pluginId = pluginId;
    }

    public PVVAHeader() {
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public boolean isHasSign() {
        return hasSign;
    }

    public void setHasSign(boolean hasSign) {
        this.hasSign = hasSign;
    }

    public int getBuildId() {
        return buildId;
    }

    public void setBuildId(int buildId) {
        this.buildId = buildId;
    }

    public byte getIdlength() {
        return idlength;
    }

    public void setIdlength(byte idlength) {
        this.idlength = idlength;
    }

    public int getMinAppVersion() {
        return minAppVersion;
    }

    public void setMinAppVersion(int minAppVersion) {
        this.minAppVersion = minAppVersion;
    }

    public int getMaxAppVersion() {
        return maxAppVersion;
    }

    public void setMaxAppVersion(int maxAppVersion) {
        this.maxAppVersion = maxAppVersion;
    }

    public int getTableOffset() {
        return tableOffset;
    }

    public void setTableOffset(int tableOffset) {
        this.tableOffset = tableOffset;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String toString() {
        return "PVVAHeader{" +
                "version=" + version +
                ", flag=" + flag +
                ", hasSign=" + hasSign +
                ", buildId=" + buildId +
                ", idlength=" + idlength +
                ", minAppVersion=" + minAppVersion +
                ", maxAppVersion=" + maxAppVersion +
                ", tableOffset=" + tableOffset +
                ", pluginId='" + pluginId + '\'' +
                '}';
    }
}