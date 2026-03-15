package com.zedapps.bookshare.dto.activity;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.login.Login;
import lombok.Builder;

/**
 * @author smzoha
 * @since 12/3/26
 **/
@Builder
public record ActivityFeedDto(Activity activity, Login login) {
}
