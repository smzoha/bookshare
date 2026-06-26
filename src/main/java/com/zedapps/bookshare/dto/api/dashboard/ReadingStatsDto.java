package com.zedapps.bookshare.dto.api.dashboard;

import com.zedapps.bookshare.dto.api.book.BookDto;

import java.util.Map;

/**
 * @author smzoha
 * @since 25/6/26
 **/
public record ReadingStatsDto(int challengeBookCount, int booksReadCount,
                              int readCount, long totalPagesRead, long averageFinishTime,
                              long avgPagesPerDay, Map<String, Integer> booksReadMap,
                              BookDto longestBook, BookDto shortestBook, BookDto latestBook,
                              int reviewCount, int totalAvgReview, Map<Integer, Integer> ratingMap) {
}
