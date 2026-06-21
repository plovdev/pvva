package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;
import org.plovdev.pvva.write.PVVAWriter;
import org.plovdev.pvvacli.PvvaPaths;
import org.plovdev.pvvacli.models.BuildXml;
import org.plovdev.pvvacli.transforms.BuildXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class BaseHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseHandler.class);

    @Command("build")
    void build(@NonNull CommandInfo info) {
        boolean isSuccessful = false;

        String pluginJsonRaw = PvvaPaths.allString(PvvaPaths.PLUGIN_JSON);
        byte[] pluginJsonBytes = PluginJsonTransformer.toJson(PluginJsonTransformer.ofJson(pluginJsonRaw)).getBytes(StandardCharsets.UTF_8);

        int buildId = (int) (System.currentTimeMillis() / 1000);
        BuildXml buildXml = BuildXmlParser.parse(PvvaPaths.BUILD_XML);
        String pluginId = buildXml.getPluginId();
        String finalName = buildXml.getFinalName() + ".pvva";

        if (info.hasFlag("re")) {
            PvvaPaths.delete(PvvaPaths.BUILDS_OUT.resolve(finalName));
        }

        PVVAHeader header = new PVVAHeader((byte) 1, (byte) buildXml.getFlag(), buildId, (byte) pluginId.length(), pluginId, BuildXml.versionToInt(buildXml.getMinAppVersion()), BuildXml.versionToInt(buildXml.getMaxAppVersion()), pluginJsonBytes.length);

        PluginJson pluginJson = null;
        ResourceConfig resourceConfig = null;
        HttpConfig httpConfig = null;
        MainParser mainParser = null;

        for (PvvaPaths.Paths path : PvvaPaths.paths()) {
            switch (path) {
                case PvvaPaths.Paths.PLUGIN_JSON:
                    pluginJson = PluginJsonTransformer.ofJson(PvvaPaths.allString(PvvaPaths.PLUGIN_JSON));
                    break;
                case PvvaPaths.Paths.HTTP_CONFIG:
                    if (Files.exists(PvvaPaths.HTTP_CONFIG)) {
                        httpConfig = HttpConfigTransformer.ofJson(PvvaPaths.allString(PvvaPaths.HTTP_CONFIG));
                    }
                    break;
                case PvvaPaths.Paths.RESOURCE_CONFIG:
                    resourceConfig = ResourceConfigTransformer.ofJson(PvvaPaths.allString(PvvaPaths.RESOURCE_CONFIG));
                    break;
                case PvvaPaths.Paths.MAIN_PARSER:
                    mainParser = ParserTransformer.ofParser(PvvaPaths.allString(PvvaPaths.MAIN_PARSER));
                    break;
            }
        }

        PVVAHost host = new PVVAHost(Objects.requireNonNull(header), Objects.requireNonNull(pluginJson), Objects.requireNonNull(resourceConfig), Objects.requireNonNull(httpConfig), Objects.requireNonNull(mainParser));
        if (Files.notExists(PvvaPaths.BUILDS_OUT)) {
            try {
                Files.createDirectory(PvvaPaths.BUILDS_OUT);
            } catch (Exception e) {
                throw new RuntimeException("Error to prepare file struct", e);
            }
        }

        try (PVVAWriter writer = new PVVAWriter(PvvaPaths.BUILDS_OUT.resolve(finalName))) {
            writer.writeVideoAdapter(host);
            isSuccessful = true;
        } catch (Exception e) {
            log.error("Error to write pvva addapter: ", e);
        }

        if (buildXml.needCreateInfo()) {
            InfoCreator.createPluginInfo(finalName, buildXml.getUrl(), header);
            log.info("Info created");
        }

        if (isSuccessful) {
            log.info("Adapter packed successful");
        } else {
            log.warn("Adapter was not packed success");
            return;
        }

        info.getSubCommand("install").ifPresent(installInfo -> {
            Path from = PvvaPaths.BUILDS_OUT.resolve(finalName);
            Path to = PvvaPaths.PLUGINS_HOME.resolve(finalName);
            if (installInfo.hasFlag("re") || info.hasFlag("re")) {
                PvvaPaths.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                log.info("Re Installed");
            } else {
                PvvaPaths.copy(from, to);
                log.info("Installed");
            }
        });
    }
}