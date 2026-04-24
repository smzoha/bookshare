package com.zedapps.bookshare.dto.api.feed;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.util.Utils;

import java.util.Map;

/**
 * @author smzoha
 * @since 24/4/26
 **/
public record FeedApiDto(String activityType, Map<String, String> actorInfo, String message,
                         String truncDetails, Map<String, Object> target,
                         String timeElapsed) {

    public static FeedApiDto getFeedApiDto(FeedDto feedDto) {
        Login actor = feedDto.actor();

        Map<String, String> actorInfo = Map.of(
                "handle", actor.getHandle(),
                "name", actor.getName(),
                "profilePictureUrl", Utils.getImageUrl(actor.getProfilePicture())
        );

        return new FeedApiDto(feedDto.activityType(), actorInfo, feedDto.message(), feedDto.truncDetails(),
                feedDto.target(), feedDto.timeElapsed());
    }
}
