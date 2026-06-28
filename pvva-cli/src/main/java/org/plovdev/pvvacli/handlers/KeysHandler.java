package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.keyer.Keychain;
import org.plovdev.pvvacli.security.KeysGenerator;
import org.plovdev.pvvacli.security.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Scanner;

import static org.plovdev.pvvacli.security.Signer.PRIVATE;
import static org.plovdev.pvvacli.security.Signer.PUBLIC;

public class KeysHandler extends CommandHandler {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(Signer.class);
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final Logger log = LoggerFactory.getLogger(KeysHandler.class);

    @Command("keys")
    void keys(@NonNull CommandInfo info) {
        if (info.hasFlag("init")) {
            byte[] priv = KEYCHAIN.getRawPassword(PRIVATE);
            if (priv == null || info.hasFlag("re")) {
                KeysGenerator generator = new KeysGenerator();
                byte[] privateKey = generator.getPrivateKey();
                byte[] publicKey = generator.getPublicKey();

                try {
                    KEYCHAIN.setPassword(PRIVATE, privateKey);
                    KEYCHAIN.setPassword(PUBLIC, publicKey);
                    System.out.println("================PUBLIC KEY================");
                    System.out.println(HEX_FORMAT.formatHex(publicKey));
                } finally {
                    Arrays.fill(privateKey, (byte) 0);
                    Arrays.fill(publicKey, (byte) 0);
                }
            } else {
                Arrays.fill(priv, (byte) 0);
                System.out.println("Keys already inited");
            }
        } else if (info.hasFlag("public")) {
            byte[] publicKey = KEYCHAIN.getRawPassword(PUBLIC);
            if (publicKey != null) {
                System.out.println("================PUBLIC KEY================");
                System.out.println(HEX_FORMAT.formatHex(publicKey));
                Arrays.fill(publicKey, (byte) 0);
            } else {
                System.out.println("Keys are not inited.");
                System.out.println("Enter 'pvva keys --init' to init key pair.");
            }
        } else if (info.hasFlag("drop")) {
            System.out.print("Are you really want to delete your keys?(This action is not cancellable) Y/N: ");
            try (Scanner scanner = new Scanner(System.in)) {
                String enter = scanner.next();
                if (enter.equalsIgnoreCase("Y")) {
                    System.out.println("If you really want to delete your keys, don't forgot remove its from https://pornviewer.foundation");
                    System.out.print("Delete keys? Y/N: ");
                    String finalEnter = scanner.next();
                    processInput(finalEnter);
                } else {
                    System.out.println("Answper is no 'Y', stop deleting keys.");
                }
            } catch (Exception e) {
                log.error("Error process this action: ", e);
            }
        }
    }

    private void processInput(@NonNull String enter) {
        if (enter.equalsIgnoreCase("Y")) {
            KEYCHAIN.deletePassword(PRIVATE);
            KEYCHAIN.deletePassword(PUBLIC);
            System.out.println("Your keys are deleted. Enter 'pvva keys --init' if you need a new keys.");
        } else {
            System.out.println("Answer is no 'Y', stop deleting keys.");
        }
    }
}