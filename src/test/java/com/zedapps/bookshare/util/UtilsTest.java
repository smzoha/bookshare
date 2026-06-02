package com.zedapps.bookshare.util;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.auth.LoginDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 2/6/26
 **/
class UtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void isAuthenticated_nullAuthentication_returnsFalse() {
        assertFalse(Utils.isAuthenticated());
    }

    @Test
    void isAuthenticated_anonymousAuthenticationToken_returnsFalse() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertFalse(Utils.isAuthenticated());
    }

    @Test
    void isAuthenticated_validNonAnonymousAuthentication_returnsTrue() {
        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        assertTrue(Utils.isAuthenticated());
    }

    @Test
    void getImageUrl_nullImage_returnsEmptyString() {
        assertEquals("", Utils.getImageUrl(null));
    }

    @Test
    void getImageUrl_validImage_returnsPathWithId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(6001);
        request.setContextPath("");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Image image = new Image(1L, "image.png", "image/png", null, LocalDateTime.now());
        String imageUrl = Utils.getImageUrl(image);

        assertEquals("http://localhost:6001/image/1", imageUrl);
    }

    @Test
    void getDefaultShelves_withLogin_returnsDefaultShelves() {
        Login login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        List<Shelf> defaultShelves = Utils.getDefaultShelves(login);

        assertEquals(3, defaultShelves.size());

        for (String defaultShelfName : Shelf.DEFAULT_SHELF_NAMES) {
            assertTrue(defaultShelves.stream().anyMatch(shelf -> Objects.equals(shelf.getName(), defaultShelfName)));
        }

        assertEquals(login, defaultShelves.getFirst().getUser());
    }

    @Test
    void cleanHtml_stringWithSingleTag_removesTag() {
        String html = "<p>This is a test string</p>";
        String cleanedHtml = Utils.cleanHtml(html);

        assertEquals("This is a test string", cleanedHtml);
    }

    @Test
    void cleanHtml_stringWithNestedTags_removesAllTags() {
        String html = "<p><strong>This is a test string</strong></p>";
        String cleanedHtml = Utils.cleanHtml(html);

        assertEquals("This is a test string", cleanedHtml);
    }

    @Test
    void cleanHtml_plainTextNoTags_returnsStringUnchanged() {
        String html = "This is a test string";
        String cleanedHtml = Utils.cleanHtml(html);

        assertEquals(html, cleanedHtml);
    }

    @Test
    void cleanHtml_emptyString_returnsEmptyString() {
        assertEquals("", Utils.cleanHtml(""));
    }

    @Test
    void cleanHtml_nullString_returnsNull() {
        assertNull(Utils.cleanHtml(null));
    }

    @Test
    void getErrorResponseDto_globalErrorsOnly_mapsToGlobalErrorList() {
        Errors errors = new MapBindingResult(new HashMap<>(), "test");
        errors.reject("error.required");

        ErrorResponseDto errorResponseDto = Utils.getErrorResponseDto(errors);

        assertFalse(errorResponseDto.getGlobalErrors().isEmpty());
        assertEquals(1, errorResponseDto.getGlobalErrors().size());
        assertEquals("error.required", errorResponseDto.getGlobalErrors().getFirst());
    }

    @Test
    void getErrorResponseDto_fieldErrorsOnly_mapsToFieldErrorMap() {
        Errors errors = new MapBindingResult(new HashMap<>(), "test");
        errors.rejectValue("field", "error.required");

        ErrorResponseDto errorResponseDto = Utils.getErrorResponseDto(errors);

        assertFalse(errorResponseDto.getFieldErrors().isEmpty());
        assertEquals(1, errorResponseDto.getFieldErrors().size());

        assertTrue(errorResponseDto.getFieldErrors().containsKey("field"));
        assertFalse(errorResponseDto.getFieldErrors().get("field").isEmpty());
    }

    @Test
    void getErrorResponseDto_multipleErrorsForSameField_groupedUnderSameKey() {
        Errors errors = new MapBindingResult(new LinkedHashMap<>(), "test");
        errors.rejectValue("field", "error.required");
        errors.rejectValue("field", "error.invalid");

        ErrorResponseDto errorResponseDto = Utils.getErrorResponseDto(errors);

        assertFalse(errorResponseDto.getFieldErrors().isEmpty());
        assertEquals(1, errorResponseDto.getFieldErrors().size());

        assertEquals(2, errorResponseDto.getFieldErrors().get("field").size());

        assertTrue(Arrays.asList(errorResponseDto.getFieldErrors().get("field")
                .getFirst()).contains("error.required"));

        assertTrue(Arrays.asList(errorResponseDto.getFieldErrors().get("field")
                .getLast()).contains("error.invalid"));
    }

    @Test
    void getErrorResponseDto_mixedErrors_includesBothGlobalAndFieldErrors() {
        Errors errors = new MapBindingResult(new LinkedHashMap<>(), "test");
        errors.rejectValue("field", "error.required");
        errors.rejectValue("field", "error.invalid");

        errors.reject("common.error.message");

        ErrorResponseDto errorResponseDto = Utils.getErrorResponseDto(errors);

        assertFalse(errorResponseDto.getFieldErrors().isEmpty());
        assertEquals(1, errorResponseDto.getFieldErrors().size());

        assertFalse(errorResponseDto.getGlobalErrors().isEmpty());
        assertEquals(1, errorResponseDto.getGlobalErrors().size());

        assertEquals(2, errorResponseDto.getFieldErrors().get("field").size());

        assertTrue(Arrays.asList(errorResponseDto.getFieldErrors().get("field")
                .getFirst()).contains("error.required"));

        assertTrue(Arrays.asList(errorResponseDto.getFieldErrors().get("field")
                .getLast()).contains("error.invalid"));

        assertEquals("common.error.message", errorResponseDto.getGlobalErrors().getFirst());
    }
}
