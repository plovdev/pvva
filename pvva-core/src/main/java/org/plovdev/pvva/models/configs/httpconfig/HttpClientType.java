package org.plovdev.pvva.models.configs.httpconfig;

public enum HttpClientType {
    JAVA_HTTP_CLIENT, APACHE_HTTP_CLIENT, OK_HTTP_CLIENT, NETTY, OTHER;

    public static HttpClientType safeValueOf(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}