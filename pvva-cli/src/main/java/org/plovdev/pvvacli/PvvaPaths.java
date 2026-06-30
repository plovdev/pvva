package org.plovdev.pvvacli;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.lang.System.getProperty;

public final class PvvaPaths {
    private static final Logger log = LoggerFactory.getLogger(PvvaPaths.class);

    public static final Path BUILD_XML = Path.of("build.xml");
    public static final Path PLUGIN_JSON = Path.of("plugin.json");
    public static final Path PLUGINS_HOME = Path.of(getProperty("pv.home", getProperty("user.home") + "/.PornViewer"), "plugins");

    public static final Path PROJECT_HOME = Path.of(".");
    public static final Path SRC_PATH = Path.of("src");
    public static final Path CONFIGS = SRC_PATH.resolve(Path.of("configs"));
    public static final Path SCRIPTS = SRC_PATH.resolve(Path.of("scripts"));
    public static final Path PARSERS = SCRIPTS.resolve(Path.of("parsers"));
    public static final Path RESOURCES = SRC_PATH.resolve(Path.of("resources"));

    public static final Path HTTP_CONFIG = CONFIGS.resolve(Path.of("http-config.json"));
    public static final Path RESOURCE_CONFIG = CONFIGS.resolve(Path.of("resource-config.json"));
    public static final Path MAIN_PARSER = PARSERS.resolve(Path.of("main-parser.lua"));
    public static final Path BUILDS_OUT = Path.of("builds");

    public enum Paths {
        BUILD_XML, PLUGIN_JSON, HTTP_CONFIG, RESOURCE_CONFIG, MAIN_PARSER
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

        Files.createDirectories(output.resolve(SRC_PATH));
        Files.createDirectories(output.resolve(CONFIGS));
        Files.createDirectories(output.resolve(PARSERS));
        Files.createDirectories(output.resolve(RESOURCES));
    }
}