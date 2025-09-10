package com.zedapps.bookshare.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author smzoha
 * @since 11/9/25
 **/
public class Utils {

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && !(auth instanceof AnonymousAuthenticationToken);
    }
}
