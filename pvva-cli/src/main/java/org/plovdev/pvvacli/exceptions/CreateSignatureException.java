package org.plovdev.pvvacli.exceptions;

public class CreateSignatureException extends PvvaCliException {
    public CreateSignatureException() {
    }

    public CreateSignatureException(String message) {
        super(message);
    }

    public CreateSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateSignatureException(Throwable cause) {
        super(cause);
    }

    public CreateSignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}