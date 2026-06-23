package org.plovdev.pvvacli.transforms;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvvacli.models.BuildXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Path;

public class BuildXmlOutUtils {
    public static void restoreBuildXml(@NonNull PVVAHeader header, @NonNull Path buildXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlStandalone(true);

        Element element = document.createElement("plugin");
        element.setAttribute("id", header.getPluginId());
        element.appendChild(createProperties(document, header));
        document.appendChild(element);

        document.normalizeDocument();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "no");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(buildXml.toFile());
        transformer.transform(source, result);
    }

    private static @NonNull Element createProperties(@NonNull Document document, @NonNull PVVAHeader header) {
        Element properties = document.createElement("properties");
        Element createSign = document.createElement("create-sign");
        createSign.setTextContent(String.valueOf(header.isHasSign()));
        properties.appendChild(createSign);

        Element minAppVersion = document.createElement("min-app-version");
        minAppVersion.setTextContent(BuildXml.intToVersion(header.getMinAppVersion()));
        properties.appendChild(minAppVersion);

        Element maxAppVersion = document.createElement("max-app-version");
        maxAppVersion.setTextContent(BuildXml.intToVersion(header.getMaxAppVersion()));
        properties.appendChild(maxAppVersion);
        return properties;
    }
}