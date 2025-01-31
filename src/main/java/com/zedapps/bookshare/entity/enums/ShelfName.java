package com.zedapps.bookshare.entity.enums;

/**
 * @author smzoha
 * @since 31/1/25
 * Generated via ChatGPT
 **/
public enum ShelfName {
    TO_READ("To Read"),
    CURRENTLY_READING("Currently Reading"),
    READ("Read");

    private final String displayName;

    ShelfName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
