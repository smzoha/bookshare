package com.zedapps.bookshare.dto.api.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author smzoha
 * @since 19/4/26
 **/
public record ReadingProgressRequest(Long progressId,

                                     @NotNull(message = "{error.required}")
                                     @Min(value = 0, message = "{error.min.value}")
                                     Long pagesRead,

                                     @NotNull(message = "{error.required}")
                                     @DateTimeFormat(pattern = "yyyy-MM-dd")
                                     LocalDate startDate,

                                     @DateTimeFormat(pattern = "yyyy-MM-dd")
                                     LocalDate endDate,

                                     boolean completed) {
}
