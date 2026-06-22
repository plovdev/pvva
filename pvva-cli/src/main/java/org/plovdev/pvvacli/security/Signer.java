package org.plovdev.pvvacli.security;

import org.jspecify.annotations.NonNull;
import org.plovdev.keyer.Keychain;
import org.plovdev.pvvacli.exceptions.CreateSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public final class Signer {
    private static final Keychain KEYCHAIN = Keychain.getKeychain(Signer.class);
    public static final String ALGORITHM = "Ed25519";
    public static final String PRIVATE = "private-key";
    public static final String PUBLIC = "public-key";
    private static final Logger log = LoggerFactory.getLogger(Signer.class);

    public static byte @NonNull [] getSignature(byte[] data) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(createPrivateKey());
            signature.update(data);

            return signature.sign();
        } catch (Exception e) {
            throw new CreateSignatureException(e);
        }
    }

    private static PrivateKey createPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] rawPrivateKey = KEYCHAIN.getRawPassword(PRIVATE);
        if (rawPrivateKey != null) {
            try {
                NamedParameterSpec params = new NamedParameterSpec(ALGORITHM);
                EdECPrivateKeySpec keySpec = new EdECPrivateKeySpec(params, rawPrivateKey);
                KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
                return factory.generatePrivate(keySpec);
            } finally {
                Arrays.fill(rawPrivateKey, (byte) 0);
            }
        } else {
            log.error("Keys not inited");
            throw new CreateSignatureException("Private key not created");
        }
    }
}