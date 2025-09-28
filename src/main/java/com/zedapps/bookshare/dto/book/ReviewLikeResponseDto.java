package com.zedapps.bookshare.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author smzoha
 * @since 28/9/25
 **/
@Getter
@Setter
@AllArgsConstructor
public class ReviewLikeResponseDto {

    private Long reviewId;
    private Boolean liked;
    private int likeCount;
}
