package com.zedapps.bookshare.entity.login;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Getter
public enum Role {

    ADMIN("Administrator"),
    MODERATOR("Moderator"),
    AUTHOR("Author"),
    USER("User");

    private final String naturalName;

    Role(String naturalName) {
        this.naturalName = naturalName;
    }

    public static String[] getAllRoleNames() {
        return Arrays.stream(Role.values())
                .map(Role::name)
                .toArray(String[]::new);
    }
}
