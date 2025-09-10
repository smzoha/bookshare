package com.zedapps.bookshare.entity.login;

import java.util.Arrays;

/**
 * @author smzoha
 * @since 6/9/25
 **/
public enum Role {

    ADMIN,
    MODERATOR,
    AUTHOR,
    USER;

    public static String[] getAllRoleNames() {
        return Arrays.stream(Role.values())
                .map(Role::name)
                .toArray(String[]::new);
    }
}
