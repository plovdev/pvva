package org.plovdev.pvva.url;

import org.jspecify.annotations.NonNull;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class PVVAProtocolProvider extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(@NonNull String protocol) {
        if (protocol.equalsIgnoreCase("pvva")) {
            return new PVVAURLHandler();
        }
        return null;
    }
}