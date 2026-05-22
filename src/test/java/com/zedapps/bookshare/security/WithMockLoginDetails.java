package com.zedapps.bookshare.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Populates the Spring Security context with a {@link com.zedapps.bookshare.service.auth.LoginDetails}
 * principal before each annotated test, eliminating the need to chain
 * {@code .with(user(loginDetails))} on every {@code mockMvc.perform()} call.
 *
 * <p>Apply at the class level to cover all tests, or at method level to override.</p>
 *
 * @author smzoha
 * @since 22/5/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = WithMockLoginDetailsSecurityContextFactory.class)
public @interface WithMockLoginDetails {

    String email() default "user@test.com";

    String handle() default "test";

    String role() default "USER";

    boolean active() default true;
}
