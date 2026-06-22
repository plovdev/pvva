package org.plovdev.pvvacli.models;

import org.jspecify.annotations.NonNull;

public class BuildXml {
    private String pluginId;
    private String minAppVersion;
    private String maxAppVersion;
    private boolean createSignature;
    private String excludePath;
    private String includeSource;
    private int flag;
    private String finalName;
    private String sign;
    private boolean createInfo;
    private String url;

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

    public String getExcludePath() {
        return excludePath;
    }

    public void setExcludePath(String excludePath) {
        this.excludePath = excludePath;
    }

    public String getIncludeSource() {
        return includeSource;
    }

    public void setIncludeSource(String includeSource) {
        this.includeSource = includeSource;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getFinalName() {
        return finalName == null ? getPluginId() : finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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

    public static int versionToInt(@NonNull String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        return major * 10000 + minor * 100 + patch;
    }

    public static @NonNull String intToVersion(int versionCode) {
        int major = versionCode / 10000;
        int minor = (versionCode % 10000) / 100;
        int patch = versionCode % 100;

        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public String toString() {
        return "BuildXml{" +
                "pluginId='" + pluginId + '\'' +
                ", minAppVersion='" + minAppVersion + '\'' +
                ", maxAppVersion='" + maxAppVersion + '\'' +
                ", createSignature=" + createSignature +
                ", excludePath='" + excludePath + '\'' +
                ", includeSource='" + includeSource + '\'' +
                ", flag=" + flag +
                ", finalName='" + finalName + '\'' +
                ", sign='" + sign + '\'' +
                ", createInfo=" + createInfo +
                ", url='" + url + '\'' +
                '}';
    }
}