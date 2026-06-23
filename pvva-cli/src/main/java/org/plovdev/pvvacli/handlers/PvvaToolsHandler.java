package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.read.PVVAReader;
import org.plovdev.pvvacli.PvvaPaths;
import org.plovdev.pvvacli.models.BuildXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;

import static org.plovdev.pvvacli.handlers.utils.StringBuilderAppener.appendString;

public class PvvaToolsHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(PvvaToolsHandler.class);

    @Command("info")
    void info(@NonNull CommandInfo info) {
        if (info.hasFlag("-i")) {
            Path pvva = Path.of(info.getFlag("-i"));
            try (PVVAReader reader = new PVVAReader(pvva)) {
                PVVAHost host = reader.parseVideoAdapter();
                PVVAHeader header = host.header();

                StringBuilder builder = new StringBuilder(pvva.getFileName() + "(" + header.pluginId() + ")" + "\n");
                appendString(builder, "FileSize", PvvaPaths.length(pvva));
                builder.append("\n");

                appendString(builder, "File Version", header.version());
                appendString(builder, "Adapter Flag", header.flag());
                appendString(builder, "Has Signature", header.hasSign());
                appendString(builder, "Build ID", header.buildId());
                appendString(builder, "Min App Version", BuildXml.intToVersion(header.minAppVersion()));
                appendString(builder, "Max App Version", BuildXml.intToVersion(header.maxAppVersion()));

                PluginJson pluginJson = host.pluginJson();
                builder.append("\n");
                appendString(builder, "Plugin Name", pluginJson.title());
                appendString(builder, "Plugin Version", pluginJson.version());
                pluginJson.autoUpdateUrl().ifPresent(url -> {
                    appendString(builder, "Autoupdate URL", url);
                    appendString(builder, "Sign Required", pluginJson.signRequired());
                });
                pluginJson.author().ifPresent(author -> appendString(builder, "Author", author));
                pluginJson.developerId().ifPresent(devId -> appendString(builder, "Developer ID", devId));
                pluginJson.authorPage().ifPresent(authorPage -> appendString(builder, "Author Page", authorPage));
                pluginJson.licenseUrl().ifPresent(license -> appendString(builder, "License", license));
                pluginJson.homepage().ifPresent(url -> appendString(builder, "Homepage", url));

                ResourceConfig config = host.resourceConfig();
                builder.append("\n");
                appendString(builder, "Base-page URL", config.baseUrl());
                appendString(builder, "Support main", config.mainResources().supports());
                appendString(builder, "Support video", config.supportVideo());
                appendString(builder, "Support Mirrors", config.supportMirrors());

                config.modelsResources().ifPresent(res -> {
                    appendString(builder, "Support Models", res.supports());
                    appendString(builder, "    Support Model", res.supportModel());
                    appendString(builder, "    Support Models Search", res.supportModelSearch());
                });
                config.categoriesResources().ifPresent(res -> {
                    appendString(builder, "Support Categories", res.supports());
                    appendString(builder, "    Support Category", res.supportCategory());
                });

                if (header.hasSign() && host.signature() != null) {
                    builder.append("\n");
                    appendString(builder, "Signature", Arrays.toString(host.signature()));
                }

                builder.append("\nFor more info enter 'pvva extract -i=").append(pvva).append(" -e={entry}' ");
                builder.append("or enter 'pvva unpack -i=").append(pvva).append("'");
                System.out.println(builder);
            } catch (Exception e) {
                log.error("Error read pvva file: ", e);
            }
        } else {
            System.out.println("Parameter '-i' not found");
        }
    }
}