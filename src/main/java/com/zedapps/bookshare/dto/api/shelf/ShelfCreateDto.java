package com.zedapps.bookshare.dto.api.shelf;

import jakarta.validation.constraints.NotBlank;

/**
 * @author smzoha
 * @since 20/4/26
 **/
public record ShelfCreateDto(@NotBlank String name) {
}
