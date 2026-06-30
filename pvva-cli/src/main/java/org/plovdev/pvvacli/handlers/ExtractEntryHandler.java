package org.plovdev.pvvacli.handlers;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.chunks.ChunkType;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.read.DefaultPVVAReader;
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
        try (PVVAReader reader = new DefaultPVVAReader(pvva)) {
            PVVAHost host = reader.readVideoAdapter();
            transformAndPrint(host, reader.extractChunk(info.getFlag("-e")));
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }

    @Contract(pure = true)
    private void transformAndPrint(PVVAHost host, @NonNull Chunk chunk) {
        printChunkHeader(chunk);
        switch (chunk.getChunkId()) {
            case Chunk.PLUGIN_JSON -> out.println(PluginJsonTransformer.toJson(host.pluginJson(), true));
            case Chunk.RESOURCE_CONFIG -> out.println(ResourceConfigTransformer.toJson(host.resourceConfig(), true));
            case Chunk.HTTP_CONFIG -> {
                Optional<HttpConfig> httpConfigOptional = host.optHttpConfig();
                if (httpConfigOptional.isPresent()) {
                    out.println(HttpConfigTransformer.toJson(httpConfigOptional.get(), true));
                } else {
                    out.println("No http-config entry in adapter.");
                }
            }
            case Chunk.MAIN_PARSER -> out.println(ParserTransformer.toParser(host.mainParser()));
            default -> out.println(chunk.stringifyChunkContent());
        }
    }

    @Command(value = "entries")
    void entries(@NonNull CommandInfo info) {
        if (!info.hasFlag("-i")) {
            log.error("Parameter -i not found; command: entries.");
            return;
        }

        Path pvva = Path.of(info.getFlag("-i"));
        try (PVVAReader reader = new DefaultPVVAReader(pvva)) {
            out.println(info);
            if (info.hasFlag("v")) {
                PVVAHost host = reader.readVideoAdapter();
                host.chunkMap().values().forEach(this::printChunkHeader);
            } else {
                EntriesOffsetTable table = reader.parseOffsetTable();
                table.entries().keySet().forEach(id -> out.println("ID: " + id));
            }
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }

    private void printChunkHeader(@NonNull Chunk chunk) {
        ChunkType chunkType = chunk.getChunkType();
        int compressedSize = chunk.getCompressedChunkSize();
        String chunkId = chunk.getChunkId();

        out.println("Chunk ID: " + chunkId);
        out.println("Chunk Type: " + chunkType.name());
        out.println("Chunk compressed size: " + compressedSize);
        out.println("Chunk decompressed size: " + chunk.getContentSize());
        out.println();
    }
}