package com.zedapps.bookshare.enums;

import lombok.Getter;

import java.util.Set;

/**
 * @author smzoha
 * @since 24/1/26
 **/
@Getter
public enum ActivityType {

    // User Activities
    BOOK_LIST_VIEW("BOOK"),
    BOOK_VIEW("BOOK"),
    BOOK_ADD_REVIEW("REVIEW"),
    BOOK_LIKE_REVIEW("REVIEW"),
    BOOK_REMOVE_LIKE_REVIEW("REVIEW"),
    BOOK_ADD_TO_SHELF("SHELF"),
    BOOK_REMOVE_FROM_SHELF("SHELF"),
    BOOK_UPDATE_READING_PROGRESS("READING_PROGRESS"),

    SHELF_ADD("SHELF"),

    LOGIN("LOGIN"),
    LOGOUT("LOGIN"),
    REGISTER("LOGIN"),
    RESET_PASSWORD_REQUEST("LOGIN"),
    RESET_PASSWORD("LOGIN"),
    AUTHOR_REQUEST("LOGIN"),

    // Admin Activities
    USER_LIST_VIEW("LOGIN"),
    USER_VIEW("LOGIN"),
    USER_ADD("LOGIN"),
    USER_UPDATE("LOGIN"),

    BOOK_LIST_VIEW_ADMIN("BOOK"),
    BOOK_VIEW_ADMIN("BOOK"),
    BOOK_ADD("BOOK"),
    BOOK_UPDATE("BOOK"),
    BOOK_REQUEST_SAVE("BOOK"),

    GENRE_LIST_VIEW("GENRE"),
    GENRE_VIEW("GENRE"),
    GENRE_ADD("GENRE"),
    GENRE_UPDATE("GENRE"),

    TAG_LIST_VIEW("TAG"),
    TAG_VIEW("TAG"),
    TAG_ADD("TAG"),
    TAG_UPDATE("TAG"),

    AUTHOR_LIST_VIEW("AUTHOR"),
    AUTHOR_VIEW("AUTHOR"),
    AUTHOR_ADD("AUTHOR"),
    AUTHOR_UPDATE("AUTHOR"),

    IMAGE_UPLOAD("IMAGE"),

    ACTUATOR_DASHBOARD_VIEW("ACTUATOR"),

    PROFILE_VIEW("LOGIN"),
    FRIEND_REQ_SENT("FRIEND_REQUEST"),
    DECLINE_FRIEND_REQ("FRIEND_REQUEST"),
    REVOKE_FRIEND_REQ("FRIEND_REQUEST"),
    ADD_FRIEND("CONNECTION"),
    REMOVE_FRIEND("CONNECTION");

    public static final Set<ActivityType> FEED_ACTIVITIES = Set.of(BOOK_ADD_REVIEW, BOOK_LIKE_REVIEW,
            BOOK_UPDATE_READING_PROGRESS, ADD_FRIEND);

    private final String referenceEntity;

    ActivityType(String referenceEntity) {
        this.referenceEntity = referenceEntity;
    }

    public boolean isReviewActivity() {
        return this == BOOK_ADD_REVIEW || this == BOOK_LIKE_REVIEW;
    }
}
