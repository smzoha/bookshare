package com.zedapps.bookshare.exception;

/**
 * @author smzoha
 * @since 27/3/26
 **/
public class MailSendException extends RuntimeException {

    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
