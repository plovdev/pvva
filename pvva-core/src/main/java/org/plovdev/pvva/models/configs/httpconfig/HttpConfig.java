package org.plovdev.pvva.models.configs.httpconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class HttpConfig {
    private final HttpClientType httpClient;
    private final HeadersConfig headersConfig;
    private final RetryPolicy retryPolicy;

    private final long connectTimeout;
    private final long readTimeout;
    private final long writeTimeout;
    private final long retryDelay;
    private final int retryCount;

    public HttpConfig(
            @Nullable HttpClientType httpClient,
            @Nullable HeadersConfig headersConfig,
            @Nullable RetryPolicy retryPolicy,
            long connectTimeout,
            long readTimeout,
            long writeTimeout,
            long retryDelay,
            int retryCount) {

        this.httpClient = httpClient;
        this.headersConfig = headersConfig;
        this.retryPolicy = retryPolicy == null ? RetryPolicy.NO_RETRY : retryPolicy;

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.retryDelay = retryDelay;
        this.retryCount = retryCount;
    }

    public @NonNull HttpClientType httpClient() {
        return httpClient == null ? HttpClientType.OK_HTTP_CLIENT : httpClient;
    }

    public @NonNull Optional<HeadersConfig> headersConfig() {
        return Optional.ofNullable(headersConfig);
    }

    public @NonNull RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    public long connectTimeout() {
        return connectTimeout != 0 ? connectTimeout : 30000;
    }

    public long readTimeout() {
        return readTimeout != 0 ? readTimeout : 30000;
    }

    public long writeTimeout() {
        return writeTimeout != 0 ? writeTimeout : 30000;
    }

    public long retryDelay() {
        return retryDelay != 0 ? retryDelay : 1000;
    }

    public int retryCount() {
        return retryCount;
    }

    @Override
    public @NonNull String toString() {
        return "HttpConfig{" +
                "httpClient='" + httpClient + '\'' +
                ", headersConfig=" + headersConfig +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                '}';
    }
}