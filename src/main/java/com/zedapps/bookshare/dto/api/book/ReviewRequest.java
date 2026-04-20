package com.zedapps.bookshare.dto.api.book;

import jakarta.validation.constraints.*;

/**
 * @author smzoha
 * @since 18/4/26
 **/
public record ReviewRequest(@NotNull(message = "{error.required}")
                            @Min(value = 0, message = "{error.min.value}")
                            @Max(value = 5, message = "{error.max.value}")
                            Integer rating,

                            @NotBlank(message = "{error.blank}")
                            @Size(max = 10000, message = "{error.max.length.exceeded}")
                            String content) {
}
