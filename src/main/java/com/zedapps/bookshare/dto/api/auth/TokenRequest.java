package com.zedapps.bookshare.dto.api.auth;

/**
 * @author smzoha
 * @since 18/4/26
 **/
public record TokenRequest(String email, String password) {
}
