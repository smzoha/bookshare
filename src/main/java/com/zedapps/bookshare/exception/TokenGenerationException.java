package com.zedapps.bookshare.exception;

/**
 * @author smzoha
 * @since 27/3/26
 **/
public class TokenGenerationException extends RuntimeException {

    public TokenGenerationException(String message) {
        super(message);
    }

    public TokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
