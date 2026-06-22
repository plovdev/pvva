package org.plovdev.pvvacli.handlers;

import org.jspecify.annotations.NonNull;
import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;
import org.plovdev.keyer.Keychain;
import org.plovdev.pvvacli.security.KeysGenerator;
import org.plovdev.pvvacli.security.Signer;

import java.util.Arrays;
import java.util.HexFormat;

import static org.plovdev.pvvacli.security.Signer.PRIVATE;
import static org.plovdev.pvvacli.security.Signer.PUBLIC;

public class KeysHandler extends CommandHandler {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(Signer.class);
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    @Command("keys")
    void keys(@NonNull CommandInfo info) {
        if (info.hasFlag("init")) {
            byte[] priv = KEYCHAIN.getRawPassword(PRIVATE);
            if (priv == null || info.hasFlag("re")) {
                KeysGenerator generator = new KeysGenerator();
                byte[] privateKey = generator.getPrivateKey();
                byte[] publicKey = generator.getPublicKey();

                try {
                    KEYCHAIN.setPasswordRaw(PRIVATE, privateKey);
                    KEYCHAIN.setPasswordRaw(PUBLIC, publicKey);
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
        }
    }
}