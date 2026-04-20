package com.zedapps.bookshare.dto.api.shelf;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author smzoha
 * @since 20/4/26
 **/
public record ShelfCreateDto(@NotBlank(message = "{error.blank}")
                             @Size(max = 255, message = "{error.max.length.exceeded}") String name) {
}
