package com.zedapps.bookshare.util;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;

import java.util.ArrayList;

/**
 * @author smzoha
 * @since 11/9/25
 **/
public class Utils {

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && !(auth instanceof AnonymousAuthenticationToken);
    }

    public static String cleanHtml(String htmlText) {
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
