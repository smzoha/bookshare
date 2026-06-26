package com.zedapps.bookshare.service.dashboard;

import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.dashboard.ReadingStatsDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.helper.ReadingStatsHelper;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author smzoha
 * @since 25/6/26
 **/
@Service
@RequiredArgsConstructor
public class ReadingStatsApiService {

    private final ReadingStatsHelper readingStatsHelper;
    private final BookApiService bookApiService;

    @SuppressWarnings("unchecked")
    public ReadingStatsDto getReadingStatsDto(LoginDetails loginDetails, Integer year) {
        Map<String, Object> model = new HashMap<>();
        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, year, model);

        int challengeBookCount = (int) model.get("challengeBookCount");

        int booksReadCount = (int) model.get("booksReadCount");
        int readCount = (int) model.get("readCount");
        long totalPagesRead = (long) model.get("totalPagesRead");

        long averageFinishTime = (long) model.get("averageFinishTime");
        long avgPagesPerDay = (long) model.get("avgPagesPerDay");

        Map<String, Integer> booksReadMap = (Map<String, Integer>) model.get("booksReadMap");

        BookDto longestBookDto = null, shortestBookDto = null, latestBookDto = null;

        Book longestBook = (Book) model.get("longestBook"), shortestBook = (Book) model.get("shortestBook"),
                latestBook = (Book) model.get("latestBook");

        if (Objects.nonNull(longestBook)) {
            longestBookDto = bookApiService.createDto(longestBook, false);
        }

        if (Objects.nonNull(shortestBook)) {
            shortestBookDto = bookApiService.createDto(shortestBook, false);
        }

        if (Objects.nonNull(latestBook)) {
            latestBookDto = bookApiService.createDto(latestBook, false);
        }

        int reviewCount = (int) model.get("reviewCount");
        int totalAvgReview = (int) model.get("totalAvgReview");

        Map<Integer, Integer> ratingMap = (Map<Integer, Integer>) model.get("ratingMap");

        return new ReadingStatsDto(challengeBookCount, booksReadCount, readCount, totalPagesRead,
                averageFinishTime, avgPagesPerDay, booksReadMap, longestBookDto, shortestBookDto, latestBookDto,
                reviewCount, totalAvgReview, ratingMap);
    }
}
