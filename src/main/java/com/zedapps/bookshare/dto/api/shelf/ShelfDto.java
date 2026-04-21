package com.zedapps.bookshare.dto.api.shelf;

/**
 * @author smzoha
 * @since 20/4/26
 **/
public record ShelfDto(String name, String login, Integer bookCount, boolean defaultShelf) {
}
