package org.plovdev.pvva.url;

import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.plugins.PVVAHostProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Возвращает читалку для чанков плагина.
 * Схема: pvva://${systemPluginId}/${chunkId}
 */
public class PVVAURLConnection extends URLConnection {
    private static final Logger log = LoggerFactory.getLogger(PVVAURLConnection.class);
    private Map<String, Chunk> chunkMap;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected PVVAURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() {
        String host = url.getHost();
        PVVAHost openedHost = PVVAHostProviderManager.getPluginFor(host);
        chunkMap = openedHost.chunkMap();

        connected = true;
        log.info("Connected to {}", host);
    }

    @Override
    public InputStream getInputStream() {
        if (!connected) {
            connect();
        }

        Chunk chunk = chunkMap.get(url.getPath().substring(1));
        if (chunk == null) {
            return InputStream.nullInputStream();
        }

        return new ByteArrayInputStream(chunk.getChunkContent());
    }
}