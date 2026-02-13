package com.zedapps.bookshare.dto.activity;

import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.login.Login;
import lombok.Builder;

import java.util.Map;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Builder
public record ActivityEvent(
        Login login,
        ActivityType eventType,
        Long referenceId,
        Map<String, Object> metadata,
        boolean internal) {
}
