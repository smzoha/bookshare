package com.zedapps.bookshare.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 5/6/26
 **/
@ExtendWith(MockitoExtension.class)
class RequestLogFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RequestLogFilter requestLogFilter;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger("REQUEST-LOG");
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger("REQUEST-LOG");
        logger.detachAppender(logAppender);
    }

    @Test
    void doFilterInternal_cssPath_doesNotEmitLog() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/css/test.css");

        requestLogFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(logAppender.list.isEmpty());
    }

    @Test
    void doFilterInternal_imageGetRequest_doesNotEmitLog() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/image/1");
        when(request.getMethod()).thenReturn(RequestMethod.GET.name());

        requestLogFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(logAppender.list.isEmpty());
    }

    @Test
    void doFilterInternal_apiRequest_emitsLogWithMethodUriAndStatus() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/book/1");
        when(request.getMethod()).thenReturn(RequestMethod.GET.name());

        when(response.getStatus()).thenReturn(HttpStatus.OK.value());

        requestLogFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.INFO
                        && logEvent.getFormattedMessage().contains("[METHOD] GET")
                        && logEvent.getFormattedMessage().contains("[STATUS] 200")));
    }

    @Test
    void doFilterInternal_authenticatedRequest_includesUsernameInLog() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/book/1");
        when(request.getRemoteUser()).thenReturn("test");

        requestLogFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.INFO
                        && logEvent.getFormattedMessage().contains("[USER] test")));
    }

    @Test
    void doFilterInternal_anonymousRequest_logsGuestLabel() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/book/1");

        requestLogFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.INFO
                        && logEvent.getFormattedMessage().contains("[USER] [GUEST]")));
    }

    @Test
    void doFilterInternal_chainThrowsException_stillLogsInFinallyBlock() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/book/1");
        when(request.getRemoteUser()).thenReturn("test");

        doThrow(ServletException.class).when(filterChain).doFilter(request, response);

        assertThrows(ServletException.class,
                () -> requestLogFilter.doFilterInternal(request, response, filterChain));

        assertTrue(logAppender.list.stream()
                .anyMatch(logEvent -> logEvent.getLevel() == Level.INFO
                        && logEvent.getFormattedMessage().contains("[USER] test")));
    }
}
