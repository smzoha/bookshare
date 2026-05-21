package com.zedapps.bookshare.util;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 11/9/25
 **/
public class Utils {

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && !(auth instanceof AnonymousAuthenticationToken);
    }

    public static String getImageUrl(Image image) {
        return Objects.nonNull(image)
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                  .path("/image/{id}")
                  .buildAndExpand(image.getId())
                  .toUriString()
                : "";
    }

    public static List<Shelf> getDefaultShelves(Login login) {
        return Shelf.DEFAULT_SHELF_NAMES.stream()
                .map(shelfName -> {
                    Shelf shelf = new Shelf();
                    shelf.setName(shelfName);
                    shelf.setUser(login);
                    shelf.setDefaultShelf(true);

                    return shelf;

                }).toList();
    }

    public static String cleanHtml(String htmlText) {
        if (StringUtils.isEmpty(htmlText)) {
            return htmlText;
        }

        return htmlText.replaceAll("<[^>]*>", "");
    }

    public static ErrorResponseDto getErrorResponseDto(Errors errors) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto();

        if (errors.hasGlobalErrors()) {
            errors.getGlobalErrors().forEach(err -> errorResponseDto.getGlobalErrors().add(err.getCode()));
        }

        if (errors.hasFieldErrors()) {
            errors.getFieldErrors().forEach(err -> {
                errorResponseDto.getFieldErrors().putIfAbsent(err.getField(), new ArrayList<>());
                errorResponseDto.getFieldErrors().get(err.getField()).add(err.getCodes());
            });
        }

        return errorResponseDto;
    }
}
