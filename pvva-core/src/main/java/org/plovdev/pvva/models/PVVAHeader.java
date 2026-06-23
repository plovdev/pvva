package org.plovdev.pvva.models;

public record PVVAHeader(byte version,
                         byte flag,
                         boolean hasSign,
                         int buildId,
                         byte idlength,
                         String pluginId,
                         int minAppVersion,
                         int maxAppVersion,
                         int jsonSize) {

    public static final String MAGIC_NUMBER = "PVVA";
    public static final int HEADER_SIZE = 24;
}