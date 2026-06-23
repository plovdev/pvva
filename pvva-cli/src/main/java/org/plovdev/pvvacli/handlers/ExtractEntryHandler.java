package org.plovdev.pvvacli.handlers;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.read.PVVAReader;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

import static java.lang.System.out;

public class ExtractEntryHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(ExtractEntryHandler.class);

    @Command(value = "extract")
    void extract(@NonNull CommandInfo info) {
        if (!info.hasFlag("-i")) {
            log.error("Parameter -i not found");
            return;
        }
        if (!info.hasFlag("-e")) {
            log.error("Extractable entry not specified");
            return;
        }

        Path pvva = Path.of(info.getFlag("-i"));
        try (PVVAReader reader = new PVVAReader(pvva)) {
            PVVAHost host = reader.parseVideoAdapter();
            transformAndPrint(info.getFlag("-e"), host);
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }

    @Contract(pure = true)
    private void transformAndPrint(@NonNull String entry, PVVAHost host) {
        switch (entry) {
            case "plugin.json" -> out.println(PluginJsonTransformer.toJson(host.pluginJson(), true));
            case "resource-config" -> out.println(ResourceConfigTransformer.toJson(host.resourceConfig(), true));
            case "http-config" -> {
                Optional<HttpConfig> httpConfigOptional = host.optHttpConfig();
                if (httpConfigOptional.isPresent()) {
                    out.println(HttpConfigTransformer.toJson(httpConfigOptional.get(), true));
                } else {
                    out.println("No " + entry + " entry in adapter.");
                }
            }
            case "main-parser" -> out.println(ParserTransformer.toParser(host.mainParser()));
        }
    }
}