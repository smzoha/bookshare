package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.repository.login.ReadingProgressRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author smzoha
 * @since 22/6/26
 **/
@Component
@RequiredArgsConstructor
public class ReadingStatsHelper {

    private final ReadingChallengeRepository readingChallengeRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReviewRepository reviewRepository;

    public void setupReadingStatsReferenceData(LoginDetails loginDetails, Integer year, Map<String, Object> model) {
        year = Objects.isNull(year) ? LocalDate.now().getYear() : year;

        Optional<ReadingChallenge> readingChallenge = readingChallengeRepository.getReadingChallengeByLogin_EmailAndYear(
                loginDetails.getEmail(), year);

        List<Integer> readingProgressYears = readingProgressRepository.findReadingProgressYearsByUser_Email(loginDetails.getEmail());

        int challengeBookCount = 0;
        if (readingChallenge.isPresent()) challengeBookCount = readingChallenge.get().getBookCount();

        model.put("challengeBookCount", challengeBookCount);
        model.put("readingProgressYears", readingProgressYears);

        setupReadingProgressProperties(loginDetails, year, model);
        setupReviewProperties(loginDetails, year, model);
    }

    private void setupReadingProgressProperties(LoginDetails loginDetails, Integer year, Map<String, Object> model) {
        List<ReadingProgress> readingProgresses = readingProgressRepository.
                findReadingProgressesByUser_EmailAndEndDateYear(loginDetails.getEmail(), year);

        Set<Book> books = new HashSet<>();
        long totalPagesRead = 0, averageFinishTime = 0;

        Map<String, Integer> booksReadMap = new LinkedHashMap<>();

        Book longestBook = null, shortestBook = null;
        ReadingProgress latestReadingProgress = null;

        Arrays.stream(Month.values()).forEach(month -> booksReadMap.put(month.name(), 0));

        for (ReadingProgress readingProgress : readingProgresses) {
            totalPagesRead += readingProgress.getPagesRead();

            String month = Month.of(readingProgress.getEndDate().getMonthValue()).name();
            booksReadMap.put(month, booksReadMap.get(month) + 1);

            Book book = readingProgress.getBook();
            books.add(book);

            if (longestBook == null) longestBook = book;
            else if (book.getPages() > longestBook.getPages()) longestBook = book;

            if (shortestBook == null) shortestBook = book;
            else if (book.getPages() < shortestBook.getPages()) shortestBook = book;

            if (latestReadingProgress == null) {
                latestReadingProgress = readingProgress;

            } else if (latestReadingProgress.getEndDate().isBefore(readingProgress.getEndDate())) {
                latestReadingProgress = readingProgress;
            }

            averageFinishTime += ChronoUnit.DAYS.between(readingProgress.getStartDate(), readingProgress.getEndDate());
        }

        boolean currentYear = Objects.equals(year, LocalDate.now().getYear());

        model.put("booksReadCount", books.size());
        model.put("readCount", readingProgresses.size());
        model.put("totalPagesRead", totalPagesRead);

        model.put("averageFinishTime", readingProgresses.isEmpty() ? 0 : Math.ceilDiv(averageFinishTime, readingProgresses.size()));
        model.put("avgPagesPerDay", Math.ceilDiv(totalPagesRead, currentYear ? LocalDateTime.now().getDayOfYear()
                : Year.of(year).length()));

        model.put("booksReadMap", booksReadMap);

        model.put("longestBook", longestBook);
        model.put("shortestBook", shortestBook);

        model.put("latestBook", Objects.requireNonNull(latestReadingProgress).getBook());
    }

    private void setupReviewProperties(LoginDetails loginDetails, int year, Map<String, Object> model) {
        List<Review> reviewList = reviewRepository.findReviewsByUser_EmailAndReviewDateYear(
                loginDetails.getEmail(), year);

        int totalReviews = 0;
        Map<Integer, Integer> ratingMap = new LinkedHashMap<>();
        IntStream.iterate(5, i -> i >= 1, i -> i - 1)
                .forEach(index -> ratingMap.put(index, 0));

        for (Review review : reviewList) {
            totalReviews += review.getRating();
            ratingMap.put(review.getRating(), ratingMap.get(review.getRating()) + 1);
        }

        model.put("reviewCount", reviewList.size());
        model.put("totalAvgReview", reviewList.isEmpty() ? 0 : Math.ceilDiv(totalReviews, reviewList.size()));
        model.put("ratingMap", ratingMap);
    }
}
