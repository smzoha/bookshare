package com.zedapps.bookshare.dto.api.login;

import com.zedapps.bookshare.enums.ConnectionAction;

/**
 * @author smzoha
 * @since 22/4/26
 **/
public record ConnectionApiDto(String handle, ConnectionAction action) {
}
