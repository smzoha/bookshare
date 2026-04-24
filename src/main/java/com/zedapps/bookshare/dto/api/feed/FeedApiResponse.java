package com.zedapps.bookshare.dto.api.feed;

import java.util.List;

/**
 * @author smzoha
 * @since 24/4/26
 **/
public record FeedApiResponse(List<FeedApiDto> feedEntries, int page, int totalPages) {
}
