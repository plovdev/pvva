package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.read.PVVAReader;
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

            preparePaths(outputDir);
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }

    private void preparePaths(Path output) throws IOException {
        Files.createDirectory(output);
        Path src = output.resolve(Path.of("src"));
        Files.createDirectory(src);

        Path configs = src.resolve(Path.of("configs"));
        Path parsers = src.resolve(Path.of("parsers"));
        Files.createDirectory(configs);
        Files.createDirectory(parsers);
    }
}