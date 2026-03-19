package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.feed.FeedEntryRepository;
import com.zedapps.bookshare.service.book.BookService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    private MessageSourceAccessor msa;

    @PostConstruct
    public void init() {
        msa = new MessageSourceAccessor(messageSource);
    }

    public void setupFeed(Login audience, int pageSize, int page, ModelMap model) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        Page<FeedEntry> feedEntries = feedEntryRepository.getPagedFeedEntries(audience, cutoffDate, pageRequest);

        List<FeedDto> feedDtoList = feedEntries.stream()
                .map(this::getFeedDto)
                .filter(Objects::nonNull)
                .toList();

        model.put("feedDtoList", feedDtoList);
        model.put("currentPage", page);
        model.put("totalPages",  feedEntries.getTotalPages() - 1);
    }

    public List<FeedDto> getFeedDtoList(Login audience, int pageSize, int page) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        Page<FeedEntry> feedEntries = feedEntryRepository.getPagedFeedEntries(audience, cutoffDate, pageRequest);

        return feedEntries.stream()
                .map(this::getFeedDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private FeedDto getFeedDto(FeedEntry feedEntry) {
        Activity activity = feedEntry.getActivity();
        Map<String, Object> activityMetadata = activity.getMetadata();

        switch (activity.getEventType()) {
            case BOOK_ADD_REVIEW:
                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.book.add.review"));

            case BOOK_LIKE_REVIEW:
                Login reviewer = loginService.getLogin(activityMetadata.get("reviewedBy").toString());

                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.book.like.review",
                        new String[]{reviewer.getName()}));

            case BOOK_UPDATE_READING_PROGRESS:
                return getFeedDtoForBook(activity, msa.getMessage("feed.activity.update.reading.progress",
                        new String[]{activityMetadata.get("pagesRead").toString(),
                                activityMetadata.get("totalPages").toString()}));

            case ADD_FRIEND:
                Login friend = loginService.getLogin(activityMetadata.get("requestFromEmail").toString());

                return new FeedDto(activity.getEventType().name(),
                        activity.getLogin(),
                        msa.getMessage("feed.activity.add.friend"),
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
        Book book = bookService.getBook(Long.parseLong(activityMetadata.get("bookId").toString()));

        return new FeedDto(activity.getEventType().name(),
                activity.getLogin(),
                message,
                Map.of("id", book.getId(),
                        "value", book.getTitle()),
                getTimeElapsed(activity.getCreatedAt())
        );
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
