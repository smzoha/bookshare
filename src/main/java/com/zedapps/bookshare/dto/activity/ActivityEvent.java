package com.zedapps.bookshare.dto.activity;

import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import lombok.Builder;

import java.util.Map;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Builder
public record ActivityEvent(
        String loginEmail,
        ActivityType eventType,
        Long referenceId,
        Map<String, Object> metadata,
        boolean internal) {

    // This is done to obtain deep immutability; record can only offer shallow - i.e. Map itself is not immutable.
    public ActivityEvent {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
