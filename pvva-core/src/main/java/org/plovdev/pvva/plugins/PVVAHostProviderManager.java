package org.plovdev.pvva.plugins;

import org.plovdev.pvva.models.PVVAHost;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class PVVAHostProviderManager {
    private static final AtomicReference<PVVAHostProvider> pluginsProvider = new AtomicReference<>(null);

    private PVVAHostProviderManager() {
        throw new UnsupportedOperationException();
    }

    public static void setPluginsProvider(PVVAHostProvider provider) {
        pluginsProvider.set(Objects.requireNonNull(provider));
    }

    public static PVVAHostProvider getPluginsProvider() {
        return pluginsProvider.get();
    }

    public static PVVAHost getPluginFor(String systemId) {
        PVVAHostProvider provider = Objects.requireNonNull(pluginsProvider.get());
        return provider.getPluginFor(systemId);
    }
}