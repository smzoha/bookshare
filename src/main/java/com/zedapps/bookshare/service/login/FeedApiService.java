package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.feed.FeedApiDto;
import com.zedapps.bookshare.dto.api.feed.FeedApiResponse;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author smzoha
 * @since 24/4/26
 **/
@Service
@RequiredArgsConstructor
public class FeedApiService {

    private final FeedService feedService;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "feedApi", key = "#audience.id + '-' + #page + '-' + #pageSize")
    public FeedApiResponse getFeedApiResponse(Login audience, int pageSize, int page) {
        Page<FeedEntry> feedEntries = feedService.getFeedEntries(audience, pageSize, page);

        List<FeedApiDto> feedApiDtoList = feedService.mapToFeedDtoList(feedEntries)
                .stream()
                .map(FeedApiDto::getFeedApiDto)
                .toList();

        return new FeedApiResponse(feedApiDtoList, page, feedEntries.getTotalPages());
    }
}
