package org.plovdev.pvva.url;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class PVVAURLHandler extends URLStreamHandler {
    private static final Logger log = LoggerFactory.getLogger(PVVAURLHandler.class);

    @Override
    protected URLConnection openConnection(@NonNull URL u) {
        return new PVVAURLConnection(u);
    }
}