package com.zedapps.bookshare.dto.book;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author smzoha
 * @since 23/9/25
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookReviewDto {

    @NotNull(message = "{error.required}")
    private Long bookId;

    @NotNull(message = "{error.required}")
    @Min(value = 0, message = "{error.min.value}")
    @Max(value = 5, message = "{error.max.value}")
    private Integer rating;

    @NotBlank(message = "{error.blank}")
    @Size(max = 10000, message = "{error.max.length.exceeded}")
    private String content;
}
