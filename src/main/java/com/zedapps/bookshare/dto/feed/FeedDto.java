package com.zedapps.bookshare.dto.feed;

import com.zedapps.bookshare.entity.login.Login;

import java.util.Map;

/**
 * @author smzoha
 * @since 15/3/26
 **/
public record FeedDto(String activityType, Login actor, String message, Map<String, Object> target,
                      String timeElapsed) {
}
