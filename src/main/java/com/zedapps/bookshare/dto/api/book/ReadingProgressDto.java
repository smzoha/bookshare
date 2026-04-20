package com.zedapps.bookshare.dto.api.book;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * @author smzoha
 * @since 19/4/26
 **/
public record ReadingProgressDto(String bookTitle, String isbn, String login, Long pagesRead,
                                 @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                 @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                 boolean completed) {
}
