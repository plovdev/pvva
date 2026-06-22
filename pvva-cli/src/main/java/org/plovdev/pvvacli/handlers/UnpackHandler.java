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
import org.plovdev.pvvacli.models.BuildXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
            log.info("Paths structure prepared, start writing.");
            Files.writeString(outputDir.resolve(PvvaPaths.PLUGIN_JSON), PluginJsonTransformer.toJson(host.pluginJson()));
            Files.writeString(outputDir.resolve(PvvaPaths.RESOURCE_CONFIG), ResourceConfigTransformer.toJson(host.resourceConfig()));
            Files.writeString(outputDir.resolve(PvvaPaths.MAIN_PARSER), ParserTransformer.toParser(host.mainParser()));
            log.info("Required data has been writen, writing not required...");
            host.optHttpConfig().ifPresent(config -> {
                try {
                    Files.writeString(outputDir.resolve(PvvaPaths.HTTP_CONFIG), HttpConfigTransformer.toJson(config));
                } catch (IOException e) {
                    log.error("Error to unpack http config: {}", e.getMessage());
                    throw new PvvaCliException("Error to unpack http config", e);
                }
            });
            restoreBuildXml(header, outputDir.resolve(PvvaPaths.BUILD_XML));
            log.info("PVVA adapter unpacked successfully");
            log.info("{} unpacked to {}", pvva.getFileName(), outputDir);
        } catch (Exception e) {
            log.error("Error process {}:", pvva, e);
        }
    }

    private void restoreBuildXml(@NonNull PVVAHeader header, @NonNull Path buildXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlStandalone(true);

        Element element = document.createElement("plugin");
        element.setAttribute("id", header.pluginId());
        element.appendChild(createProperties(document, header));
        document.appendChild(element);

        document.normalizeDocument();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(buildXml.toFile());
        transformer.transform(source, result);
    }

    private @NonNull Element createProperties(@NonNull Document document, @NonNull PVVAHeader header) {
        Element properties = document.createElement("properties");
        Element createSign = document.createElement("create-sign");
        createSign.setTextContent(String.valueOf(header.hasSign()));
        properties.appendChild(createSign);

        Element minAppVersion = document.createElement("min-app-version");
        minAppVersion.setTextContent(BuildXml.intToVersion(header.minAppVersion()));
        properties.appendChild(minAppVersion);

        Element maxAppVersion = document.createElement("max-app-version");
        maxAppVersion.setTextContent(BuildXml.intToVersion(header.maxAppVersion()));
        properties.appendChild(maxAppVersion);
        return properties;
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