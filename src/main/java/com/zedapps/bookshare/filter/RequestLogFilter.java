package com.zedapps.bookshare.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * @author smzoha
 * @since 7/2/26
 **/
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("REQUEST-LOG");
    final Set<String> EXCLUDED_URLS = Set.of("/css", "/fontawesome", "/img", "/js", "/favicon");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/image") && Objects.equals(request.getMethod(), "GET")) {
            filterChain.doFilter(request, response);
            return;
        }

        for (String url : EXCLUDED_URLS) {
            if (requestURI.startsWith(url)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("REQUEST-LOG :: [METHOD] {} [URL] {}{} [STATUS] {} [IP] {} [USER] {}",
                    request.getMethod(),
                    requestURI,
                    request.getQueryString() != null ? "?" + request.getQueryString() : "",
                    response.getStatus(),
                    request.getRemoteAddr(),
                    request.getRemoteUser() != null ? request.getRemoteUser() : "[GUEST]");
        }
    }
}
