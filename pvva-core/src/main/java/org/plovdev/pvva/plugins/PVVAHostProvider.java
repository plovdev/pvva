package org.plovdev.pvva.plugins;

import org.plovdev.pvva.models.PVVAHost;

@FunctionalInterface
public interface PVVAHostProvider {
    PVVAHost getPluginFor(String systemPluginId);
}