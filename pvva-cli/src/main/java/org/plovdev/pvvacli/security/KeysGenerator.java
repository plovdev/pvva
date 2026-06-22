package org.plovdev.pvvacli.security;

import org.jspecify.annotations.NonNull;
import org.plovdev.pvvacli.exceptions.KeyGenerationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;

import static org.plovdev.pvvacli.security.Signer.ALGORITHM;

public final class KeysGenerator {
    private final KeyPair keyPair;

    public KeysGenerator() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
            keyPair = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new KeyGenerationException(e);
        }
    }

    public byte @NonNull [] getPublicKey() {
        EdECPublicKey publicKey = (EdECPublicKey) keyPair.getPublic();
        return publicKey.getPoint().getY().toByteArray();
    }

    public byte @NonNull [] getPrivateKey() {
        EdECPrivateKey privateKey = (EdECPrivateKey) keyPair.getPrivate();
        return privateKey.getBytes().orElseThrow();
    }
}