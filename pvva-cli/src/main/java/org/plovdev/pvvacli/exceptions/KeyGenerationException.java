package org.plovdev.pvvacli.exceptions;

public class KeyGenerationException extends PvvaCliException {
    public KeyGenerationException() {
    }

    public KeyGenerationException(String message) {
        super(message);
    }

    public KeyGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyGenerationException(Throwable cause) {
        super(cause);
    }

    public KeyGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}