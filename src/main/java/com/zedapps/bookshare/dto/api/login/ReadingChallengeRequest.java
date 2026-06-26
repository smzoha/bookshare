package com.zedapps.bookshare.dto.api.login;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author smzoha
 * @since 25/6/26
 **/
public record ReadingChallengeRequest(int year,

                                      @NotNull(message = "{error.required}")
                                      @Min(value = 1, message = "{error.min.value}")
                                      @Max(value = 1000, message = "{error.max.value}")
                                      Integer bookCount) {
}
