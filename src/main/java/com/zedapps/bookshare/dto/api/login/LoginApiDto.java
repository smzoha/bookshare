package com.zedapps.bookshare.dto.api.login;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zedapps.bookshare.dto.api.book.ReadingProgressDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;

import java.util.List;

/**
 * @author smzoha
 * @since 22/4/26
 **/
public record LoginApiDto(String firstName, String lastName, String handle, String email,
                          String bio, String profilePictureUrl, String role, boolean active,
                          @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ShelfDto> shelfList,
                          @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ReadingProgressDto> readingProgress,
                          @JsonInclude(JsonInclude.Include.NON_EMPTY) List<LoginApiDto> connectionList) {
}
