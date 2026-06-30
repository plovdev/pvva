package org.plovdev.pvvacli.models;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BuildXml {
    private String pluginId;
    private String minAppVersion;
    private String maxAppVersion;
    private boolean createSignature;
    private String finalName;
    private boolean createInfo;
    private String url;
    private int compressLevel = 9;
    private final List<Path> excludes = new ArrayList<>();

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getMinAppVersion() {
        return minAppVersion;
    }

    public void setMinAppVersion(String minAppVersion) {
        this.minAppVersion = minAppVersion;
    }

    public String getMaxAppVersion() {
        return maxAppVersion;
    }

    public void setMaxAppVersion(String maxAppVersion) {
        this.maxAppVersion = maxAppVersion;
    }

    public String getFinalName() {
        return finalName == null ? getPluginId() : finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public boolean needCreateInfo() {
        return createInfo;
    }

    public void setCreateInfo(boolean createInfo) {
        this.createInfo = createInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCreateSignature() {
        return createSignature;
    }

    public void setCreateSignature(boolean createSignature) {
        this.createSignature = createSignature;
    }

    public int getCompressLevel() {
        return compressLevel;
    }

    public void setCompressLevel(int compressLevel) {
        this.compressLevel = compressLevel;
    }

    public List<Path> getExcludes() {
        return List.copyOf(excludes);
    }

    public void putExcludes(List<Path> excs) {
        excludes.addAll(excs);
    }

    @Override
    public String toString() {
        return "BuildXml{" +
                "pluginId='" + pluginId + '\'' +
                ", minAppVersion='" + minAppVersion + '\'' +
                ", maxAppVersion='" + maxAppVersion + '\'' +
                ", createSignature=" + createSignature +
                ", finalName='" + finalName + '\'' +
                ", createInfo=" + createInfo +
                ", url='" + url + '\'' +
                ", compressLevel=" + compressLevel +
                '}';
    }
}