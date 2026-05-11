package org.plovdev.pvvacli;

import org.plovdev.commaidle.commands.CommandParser;
import org.plovdev.commaidle.commands.Commander;
import org.plovdev.pvva.read.PVVAReader;
import org.plovdev.pvvacli.handlers.BaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        startPVVA(args);
    }

    private static void readPlugin() {
        Path path = Path.of("builds/Porn365.pvva");
        try (PVVAReader reader = new PVVAReader(path)) {
            System.out.println(reader.parseVideoAdapter());
            HexFormat format = HexFormat.of();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dgst = digest.digest(PvvaPaths.allBytes(path));
            System.out.println(format.formatHex(dgst));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    private static void startPVVA(String[] args) {
        Commander commander = Commander.getInstance();
        commander.registerCommandHandler(new BaseHandler());
        commander.notifyCommandListeners(CommandParser.parse(args));
    }
}