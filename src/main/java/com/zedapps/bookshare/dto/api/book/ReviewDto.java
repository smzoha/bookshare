package com.zedapps.bookshare.dto.api.book;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 18/4/26
 **/
public record ReviewDto(String reviewedBy, String comment,
                        @JsonFormat(pattern = "yyyy-MM-dd hh:mm a") LocalDateTime reviewDate, Integer rating) {
}
