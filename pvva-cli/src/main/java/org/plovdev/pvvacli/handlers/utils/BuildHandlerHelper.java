package org.plovdev.pvvacli.handlers.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.chunks.Chunk;
import org.plovdev.pvva.models.chunks.ChunkType;
import org.plovdev.pvva.utils.DataCompressor;
import org.plovdev.pvvacli.PvvaPaths;
import org.plovdev.pvvacli.exceptions.PvvaCliException;
import org.plovdev.pvvacli.models.BuildXml;
import org.plovdev.pvvacli.utils.ChunkTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public final class BuildHandlerHelper {
    private static final Logger log = LoggerFactory.getLogger(BuildHandlerHelper.class);

    @Contract(pure = true)
    public static @NonNull @Unmodifiable Map<String, Chunk> findProjectChunks(@NonNull BuildXml buildXml) {
        int compressLevel = buildXml.getCompressLevel();
        List<Path> excs = buildXml.getExcludes();

        Path pluginJsonPath = PvvaPaths.PLUGIN_JSON;
        if (Files.notExists(pluginJsonPath)) {
            throw new NoSuchElementException("File plugin.json not found in project.");
        }

        Map<String, Chunk> chunkMap = new ConcurrentHashMap<>();
        chunkMap.put(Chunk.PLUGIN_JSON, prepareChunk(pluginJsonPath, compressLevel));

        try (Stream<Path> projectFiles = Files.walk(PvvaPaths.SRC_PATH);
             ExecutorService fileScanner = Executors.newVirtualThreadPerTaskExecutor()) {
            projectFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> !excs.contains(path))
                    .forEach(path -> fileScanner.execute(() -> {
                        Chunk chunk = prepareChunk(path, compressLevel);
                        chunkMap.put(chunk.getChunkId(), chunk);
                    }));
        } catch (Exception e) {
            log.error("Error find project files: ", e);
            throw new IllegalStateException(e);
        }

        return chunkMap;
    }

    private static @NonNull Chunk prepareChunk(@NonNull Path path, int compressLevel) {
        try {
            String chunkId = path.getFileName().toString();
            byte chunkIdLength = (byte) chunkId.getBytes(StandardCharsets.US_ASCII).length;

            Path relativePath = PvvaPaths.PROJECT_HOME.relativize(path);
            ChunkType chunkType = ChunkTypeUtils.determineChunkType(relativePath);

            byte[] content = Files.readAllBytes(path);
            byte[] compressed = DataCompressor.compress(content, compressLevel);

            return new Chunk(chunkType, compressed.length, chunkIdLength, chunkId, compressed);
        } catch (Exception e) {
            log.error("Error scanning file {}: ", path, e);
            throw new PvvaCliException("Error scanning file: ", e);
        }
    }
}