package com.zedapps.bookshare.dto.api.book;

/**
 * @author smzoha
 * @since 20/4/26
 **/
public record ShelfResponseDto(String name, String login, Integer bookCount, boolean defaultShelf) {
}
