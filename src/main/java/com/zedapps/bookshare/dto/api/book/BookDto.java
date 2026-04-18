package com.zedapps.bookshare.dto.api.book;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * @author smzoha
 * @since 18/4/26
 **/
public record BookDto(String title, String isbn, String description, String imageUrl, Long pages,
                      @JsonFormat(pattern = "yyyy-MM-dd") LocalDate publicationDate,
                      Double averageRating, List<AuthorDto> authors, List<String> genres,
                      List<String> tags, List<ReviewDto> reviews) {
}
