package com.zedapps.bookshare.dto.api.book;

/**
 * @author smzoha
 * @since 18/4/26
 **/
public record ReviewDto(String reviewedBy, String comment, String reviewDate, Integer rating) {
}
