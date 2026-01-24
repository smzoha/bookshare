package com.zedapps.bookshare.entity.activity.enums;

import lombok.Getter;

import java.util.Set;

/**
 * @author smzoha
 * @since 24/1/26
 **/
@Getter
public enum ActivityType {

    // User Activities
    BOOK_LIST_VIEW,
    BOOK_VIEW,
    BOOK_ADD_REVIEW,
    BOOK_LIKE_REVIEW,
    BOOK_ADD_TO_SHELF,
    BOOK_REMOVE_FROM_SHELF,
    BOOK_UPDATE_READING_PROGRESS,

    SHELF_ADD,

    LOGIN,
    LOGOUT,
    REGISTER,

    // Admin Activities
    USER_ADD,
    USER_UPDATE,
    BOOK_ADD,
    BOOK_UPDATE,
    GENRE_ADD,
    GENRE_UPDATE,
    TAG_ADD,
    TAG_UPDATE,
    AUTHOR_ADD,
    AUTHOR_UPDATE;

    static final Set<ActivityType> FEED_ACTIVITIES = Set.of(BOOK_ADD_REVIEW, BOOK_LIKE_REVIEW, BOOK_UPDATE_READING_PROGRESS);
}
