package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.read.PVVAReader;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;
import org.plovdev.pvvacli.PvvaPaths;
import org.plovdev.pvvacli.exceptions.PvvaCliException;
import org.plovdev.pvvacli.transforms.BuildXmlOutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnpackHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(UnpackHandler.class);

    @Command("unpack")
    void unpack(@NonNull CommandInfo info) {
        if (!info.hasFlag("-i")) {
            log.error("Parameter -i not found");
            return;
        }
        Path pvva = Path.of(info.getFlag("-i"));
        String dirName = info.hasFlag("-o") ? info.getFlag("-o") : pvva.toFile().getName();
        if (dirName.endsWith(".pvva")) dirName = dirName.replace(".pvva", "");
        Path outputDir = Path.of(dirName);

        try (PVVAReader reader = new PVVAReader(pvva)) {
            PVVAHost host = reader.parseVideoAdapter();
            PVVAHeader header = host.header();

            PvvaPaths.preparePaths(outputDir);
            log.info("Paths structure prepared, start writing.");
            Files.writeString(outputDir.resolve(PvvaPaths.PLUGIN_JSON), PluginJsonTransformer.toJson(host.pluginJson(), true));
            Files.writeString(outputDir.resolve(PvvaPaths.RESOURCE_CONFIG), ResourceConfigTransformer.toJson(host.resourceConfig(), true));
            Files.writeString(outputDir.resolve(PvvaPaths.MAIN_PARSER), ParserTransformer.toParser(host.mainParser()));
            log.info("Required data has been writen, writing not required...");
            host.optHttpConfig().ifPresent(config -> {
                try {
                    Files.writeString(outputDir.resolve(PvvaPaths.HTTP_CONFIG), HttpConfigTransformer.toJson(config, true));
                } catch (IOException e) {
                    log.error("Error to unpack http config: {}", e.getMessage());
                    throw new PvvaCliException("Error to unpack http config", e);
                }
            });
            BuildXmlOutUtils.restoreBuildXml(header, outputDir.resolve(PvvaPaths.BUILD_XML));
            log.info("PVVA adapter unpacked successfully");
            log.info("{} unpacked to {}", pvva.getFileName(), outputDir);
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }
}