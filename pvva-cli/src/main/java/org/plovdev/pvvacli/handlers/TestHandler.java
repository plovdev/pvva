package org.plovdev.pvvacli.handlers;

import com.plovdev.pornviewer.http.PornClient;
import com.plovdev.pornviewer.http.PornClientImpl;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.models.PluginJson;
import org.plovdev.pvva.models.configs.httpconfig.HttpConfig;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;
import org.plovdev.pvva.models.parsers.MainParser;
import org.plovdev.pvva.models.table.EntriesOffsetTable;
import org.plovdev.pvva.transforms.HttpConfigTransformer;
import org.plovdev.pvva.transforms.PluginJsonTransformer;
import org.plovdev.pvva.transforms.ResourceConfigTransformer;
import org.plovdev.pvva.transforms.parser.ParserTransformer;
import org.plovdev.pvvacli.PvvaPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TestHandler extends CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

    @Command("test")
    void test(CommandInfo info) {
        PVVAHeader header = new PVVAHeader((byte) 1, (byte) 0, false, 0, (byte) 0, 0, 0, 0, "");

        PluginJson pluginJson = PluginJsonTransformer.ofJson(PvvaPaths.allString(PvvaPaths.PLUGIN_JSON));
        ResourceConfig resourceConfig = ResourceConfigTransformer.ofJson(PvvaPaths.allString(PvvaPaths.RESOURCE_CONFIG));
        HttpConfig httpConfig = HttpConfigTransformer.ofJson(PvvaPaths.allString(PvvaPaths.HTTP_CONFIG));
        MainParser mainParser = ParserTransformer.ofParser(PvvaPaths.allString(PvvaPaths.MAIN_PARSER));
        PVVAHost host = new PVVAHost(header, new EntriesOffsetTable((short) 0, (byte) 0, Map.of()), pluginJson, resourceConfig, httpConfig, mainParser, Map.of(), null);

        try (PornClient client = new PornClientImpl(host)) {
            System.out.println(client.requestMainPage(0));
        } catch (Exception e) {
            log.error("Error test plugin: ", e);
        }
    }
}