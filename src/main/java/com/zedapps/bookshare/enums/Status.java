package com.zedapps.bookshare.enums;

import lombok.Getter;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Getter
public enum Status {

    ACTIVE("Active"),
    PENDING("Pending"),
    ARCHIVED("Archived");

    private final String naturalName;

    Status(String naturalName) {
        this.naturalName = naturalName;
    }
}
