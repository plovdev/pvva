package org.plovdev.pvvacli;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.lang.System.getProperty;

public final class PvvaPaths {
    public static final Path BUILD_XML = Path.of("build.xml");
    public static final Path PLUGIN_JSON = Path.of("plugin.json");
    public static final Path PLUGINS_HOME = Path.of(getProperty("pv.home", getProperty("user.home") + "/.PornViewer"), "plugins");
    public static final Path HTTP_CONFIG = Path.of("src/configs/http-config.json");
    public static final Path RESOURCE_CONFIG = Path.of("src/configs/resource-config.json");
    public static final Path MAIN_PARSER = Path.of("src/parsers/main-parser.parser");
    public static final Path BUILDS_OUT = Path.of("builds");

    public enum Paths {
        BUILD_XML, PLUGIN_JSON, HTTP_CONFIG, RESOURCE_CONFIG, MAIN_PARSER
    }

    private static final Logger log = LoggerFactory.getLogger(PvvaPaths.class);

    public static @NonNull @Unmodifiable List<Paths> paths() {
        return List.of(Paths.values());
    }

    public static @NonNull String allString(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (Exception e) {
            log.error("Error read file: ", e);
            return "";
        }
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    public static int length(Path path) {
        if (!exists(path)) return 0;
        try {
            return (int) Files.size(path);
        } catch (Exception e) {
            log.error("Error check file length: ", e);
            return 0;
        }
    }

    public static byte @NonNull [] allBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {
            log.error("Error read file: ", e);
            return new byte[0];
        }
    }

    public static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.error("Error delete file: ", e);
        }
    }

    public static void copy(Path from, Path to, StandardCopyOption... opts) {
        try {
            Files.copy(from, to, opts);
        } catch (Exception e) {
            log.error("Error copy file: ", e);
        }
    }

    public static void preparePaths(Path output) throws IOException {
        Files.createDirectories(output);
        Path src = output.resolve(Path.of("src"));
        Files.createDirectory(src);

        Path configs = src.resolve(Path.of("configs"));
        Path parsers = src.resolve(Path.of("parsers"));
        Files.createDirectory(configs);
        Files.createDirectory(parsers);
    }
}