package org.plovdev.pvvacli;

import org.plovdev.commaidle.commands.CommandParser;
import org.plovdev.commaidle.commands.Commander;
import org.plovdev.pvvacli.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    static void main(String[] args) {
        Commander commander = Commander.getInstance();
        commander.registerCommandHandler(new BaseHandler());
        commander.registerCommandHandler(new PvvaToolsHandler());
        commander.registerCommandHandler(new UtilsHandler());
        commander.registerCommandHandler(new ExtractEntryHandler());
        commander.registerCommandHandler(new KeysHandler());
        commander.registerCommandHandler(new UnpackHandler());
        commander.notifyCommandListeners(CommandParser.parse(args));
    }
}