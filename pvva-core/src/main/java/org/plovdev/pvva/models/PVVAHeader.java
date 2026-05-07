package org.plovdev.pvva.models;

import org.jspecify.annotations.NonNull;

public record PVVAHeader(byte version, byte flag, int buildId, byte idlength, String pluginId, int minAppVersion,
                         int maxAppVersion, int jsonSize) {
    public static final String MAGIC_NUMBER = "PVVA";
    public static final int HEADER_SIZE = 23;

    @Override
    public @NonNull String toString() {
        return "PVVAHeader{" +
                "version=" + version +
                ", flag=" + flag +
                ", buildId=" + buildId +
                ", idlength=" + idlength +
                ", pluginId='" + pluginId + '\'' +
                ", minAppVersion=" + minAppVersion +
                ", maxAppVersion=" + maxAppVersion +
                ", jsonSize=" + jsonSize +
                '}';
    }
}