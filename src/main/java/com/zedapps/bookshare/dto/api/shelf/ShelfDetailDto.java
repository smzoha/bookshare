package com.zedapps.bookshare.dto.api.shelf;

import com.zedapps.bookshare.dto.api.book.BookDto;

import java.util.List;

/**
 * @author smzoha
 * @since 20/4/26
 **/
public record ShelfDetailDto(String name, String login, boolean defaultShelf, List<BookDto> books) {
}
