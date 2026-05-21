package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.feed.FeedEntryRepository;
import com.zedapps.bookshare.repository.login.ReviewRepository;
import com.zedapps.bookshare.service.book.BookService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author smzoha
 * @since 14/3/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedEntryRepository feedEntryRepository;
    private final LoginService loginService;
    private final BookService bookService;

    private final MessageSource messageSource;
    private final ReviewRepository reviewRepository;
    private MessageSourceAccessor msa;

    @PostConstruct
    public void init() {
        msa = new MessageSourceAccessor(messageSource);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "feed", key = "#audience.id + '-' + #page + '-' + #pageSize")
    public Page<FeedEntry> getFeedEntries(Login audience, int pageSize, int page) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return feedEntryRepository.getPagedFeedEntries(audience, cutoffDate, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<FeedDto> mapToFeedDtoList(Page<FeedEntry> feedEntries) {
        return feedEntries.stream()
                .map(this::getFeedDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private FeedDto getFeedDto(FeedEntry feedEntry) {
        Activity activity = feedEntry.getActivity();
        Map<String, Object> activityMetadata = activity.getMetadata();

        Locale locale = LocaleContextHolder.getLocale();

        switch (activity.getEventType()) {
            case BOOK_ADD_REVIEW:
                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.book.add.review", locale));

            case BOOK_LIKE_REVIEW:
                Login reviewer = loginService.getLogin(getPropertyFromMetadata(activityMetadata, "reviewedBy"));

                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.book.like.review",
                        new String[]{reviewer.getName()}, locale));

            case BOOK_UPDATE_READING_PROGRESS:
                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.update.reading.progress",
                        new String[]{getPropertyFromMetadata(activityMetadata, "pagesRead"),
                                getPropertyFromMetadata(activityMetadata, "totalPages")}, locale));

            case ADD_FRIEND:
                Login friend = loginService.getLogin(getPropertyFromMetadata(activityMetadata, "requestFromEmail"));

                return new FeedDto(activity.getEventType().name(),
                        activity.getLogin(),
                        msa.getMessage("feed.activity.add.friend", locale),
                        null,
                        Map.of("id", friend.getHandle(),
                                "value", friend.getName()),
                        getTimeElapsed(activity.getCreatedAt()));

            default:
                log.info("Invalid activity type found in feed list: id:{}, eventType:{}", feedEntry.getId(), activity.getEventType());
        }

        return null;
    }

    private FeedDto getFeedDtoForBook(Activity activity, String message) {
        Map<String, Object> activityMetadata = activity.getMetadata();
        Book book = bookService.getBook(Long.parseLong(getPropertyFromMetadata(activityMetadata, "bookId")));

        String truncDetails = null;

        if (activity.getEventType().isReviewActivity()) {
            long reviewId = Long.parseLong(getPropertyFromMetadata(activityMetadata, "reviewId"));
            Review review = reviewRepository.findById(reviewId).orElseThrow(NoResultException::new);

            truncDetails = StringUtils.abbreviate(review.getContent(), 50) + " (" + review.getRating() + "/5)";
        }

        return new FeedDto(activity.getEventType().name(),
                activity.getLogin(),
                message,
                truncDetails,
                Map.of("id", book.getId(),
                        "value", book.getTitle()),
                getTimeElapsed(activity.getCreatedAt())
        );
    }

    private String getPropertyFromMetadata(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);

        if (value == null) {
            throw new IllegalStateException("Missing required activity metadata key: " + key);
        }

        return value.toString();
    }

    private String getTimeElapsed(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);

        if (minutes < 1) return "now";
        if (minutes < 60) return minutes + "m";
        if (hours < 24) return hours + "h";
        else return days + "d";
    }
}
