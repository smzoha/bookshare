package com.zedapps.bookshare.dto.api.book;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorDto(String firstName, String lastName, String email) {
}
