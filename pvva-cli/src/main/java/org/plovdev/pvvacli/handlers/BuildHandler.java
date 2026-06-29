package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.WritablePVVAHost;
import org.plovdev.pvva.write.DefaultPVVAWriter;
import org.plovdev.pvva.write.PVVAWriter;
import org.plovdev.pvvacli.PvvaPaths;
import org.plovdev.pvvacli.exceptions.PvvaCliException;
import org.plovdev.pvvacli.handlers.utils.BuildHandlerHelper;
import org.plovdev.pvvacli.models.BuildXml;
import org.plovdev.pvvacli.security.Signer;
import org.plovdev.pvvacli.transforms.BuildXmlParser;
import org.plovdev.pvvacli.utils.InfoCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class BuildHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(BuildHandler.class);

    @Command("build")
    void build(@NonNull CommandInfo info) {
        boolean isSuccessful = false;
        BuildXml buildXml = BuildXmlParser.parse(PvvaPaths.BUILD_XML);
        String finalName = buildXml.getFinalName() + ".pvva";

        Path pvvaOut = PvvaPaths.BUILDS_OUT.resolve(finalName);
        if (info.hasFlag("re")) {
            PvvaPaths.delete(pvvaOut);
        } else {
            if (Files.exists(pvvaOut)) {
                log.info("Adapter {} already exists.", pvvaOut);
                return;
            }
        }

        String pluginId = buildXml.getPluginId();
        byte pluginIdLength = (byte) pluginId.length();
        int tableOffset = PVVAHeader.ABS_HEADER_SIZE + pluginIdLength;

        PVVAHeader header = new PVVAHeader((byte) 1, (byte) 0, buildXml.isCreateSignature(), BuildXml.generateBuildId(), pluginIdLength, BuildXml.versionToInt(buildXml.getMinAppVersion()), BuildXml.versionToInt(buildXml.getMaxAppVersion()), tableOffset, pluginId);
        WritablePVVAHost host = new WritablePVVAHost(Objects.requireNonNull(header), BuildHandlerHelper.findProjectChunks(buildXml.getCompressLevel()));

        prepareBuildsOut();
        try (PVVAWriter writer = new DefaultPVVAWriter(pvvaOut)) {
            writer.writeVideoAdapter(host);
            if (buildXml.isCreateSignature()) {
                writer.appendSignature(Signer.getSignature(writer.getWrittenData().array()));
            }
            isSuccessful = true;
        } catch (Exception e) {
            log.error("Error to write pvva addapter: ", e);
        }

        if (isSuccessful) {
            if (buildXml.needCreateInfo()) {
                InfoCreator.createPluginInfo(finalName, buildXml.getUrl(), header);
                log.info("Info created");
            }
            log.info("Adapter packed successful");
        } else {
            log.warn("Adapter was not packed success");
            try {
                Files.deleteIfExists(pvvaOut);
            } catch (IOException e) {
                log.error("Error cleanup builded data: ", e);
            }
            return;
        }

        info.getSubCommand("install").ifPresent(installInfo -> {
            Path from = PvvaPaths.BUILDS_OUT.resolve(finalName);
            Path to = PvvaPaths.PLUGINS_HOME.resolve(finalName);
            if (installInfo.hasFlag("re") || info.hasFlag("re")) {
                PvvaPaths.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                log.info("Re Installed");
            } else {
                try {
                    Files.copy(from, to);
                    log.info("Installed");
                } catch (Exception e) {
                    log.error("Error install plugin: ", e);
                }
            }
        });
    }

    private void prepareBuildsOut() {
        if (Files.notExists(PvvaPaths.BUILDS_OUT)) {
            try {
                Files.createDirectory(PvvaPaths.BUILDS_OUT);
            } catch (Exception e) {
                throw new PvvaCliException("Error to prepare file struct", e);
            }
        }
    }

    @Command("install")
    void install(@NonNull CommandInfo info) {
        if (!info.hasFlag("-i")) {
            log.error("Parameter -i not found");
            return;
        }
        Path from = Path.of(info.getFlag("-i"));
        if (Files.notExists(from)) {
            System.out.println("Input .pvva not found.");
            return;
        }

        Path pvvaName = from.getFileName();
        Path to = PvvaPaths.PLUGINS_HOME.resolve(pvvaName);
        if (info.hasFlag("re")) {
            PvvaPaths.delete(to);
        } else {
            if (Files.exists(to)) {
                log.info("Adapter {} already installed.", to);
                return;
            }
        }

        if (info.hasFlag("re")) {
            PvvaPaths.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            log.info("{} Re-installed to app.", pvvaName);
        } else {
            try {
                Files.copy(from, to);
                log.info("{} Installed to app.", pvvaName);
            } catch (Exception e) {
                log.error("Error install plugin: ", e);
            }
        }
    }
}