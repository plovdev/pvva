package org.plovdev.pvvacli.transforms;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvvacli.exceptions.PvvaCliException;
import org.plovdev.pvvacli.models.BuildXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BuildXmlParser {
    private static final Logger log = LoggerFactory.getLogger(BuildXmlParser.class);
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    public static @NonNull BuildXml parse(@NonNull Path xmlPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlPath.toFile());

            BuildXml buildXml = new BuildXml();
            buildXml.setPluginId(doc.getDocumentElement().getAttribute("id"));

            buildXml.setMinAppVersion(getElementText(doc, "min-app-version"));
            buildXml.setMaxAppVersion(getElementText(doc, "max-app-version"));
            buildXml.setCreateSignature(Boolean.parseBoolean(getElementText(doc, "create-sign")));

            String cl = getElementText(doc, "compress-level");
            buildXml.setCompressLevel(cl == null || cl.isEmpty() ? 9 : Integer.parseInt(cl.trim()));
            buildXml.setFinalName(getElementText(doc, "final-name"));
            buildXml.setCreateInfo(Boolean.parseBoolean(getElementText(doc, "create-info")));
            buildXml.setUrl(getElementText(doc, "url"));
            buildXml.putExcludes(parsePathsList(doc, "excludes"));
            return buildXml;
        } catch (Exception e) {
            throw new PvvaCliException(e);
        }
    }

    private static @Nullable String getElementText(@NonNull Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private static @NonNull List<Path> parsePathsList(Document doc, String part) {
        try {
            List<Path> files = new ArrayList<>();
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "/plugin/build/files/" + part + "/file";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                files.add(Path.of(nodeList.item(i).getTextContent().trim()));
            }

            return files;
        } catch (XPathExpressionException e) {
            log.error("XPath expression error: ", e);
            throw new IllegalArgumentException(e);
        }
    }
}