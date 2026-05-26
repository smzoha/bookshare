package com.zedapps.bookshare.controller;

import com.zedapps.bookshare.config.SecurityConfig;
import com.zedapps.bookshare.service.auth.JwtService;
import com.zedapps.bookshare.service.auth.LoginDetailOidcService;
import com.zedapps.bookshare.service.auth.LoginDetailService;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * @author smzoha
 * @since 26/5/26
 **/
@Import(SecurityConfig.class)
public abstract class AbstractWebMvcTest {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected LoginDetailService loginDetailService;

    @MockitoBean
    protected LoginDetailOidcService loginDetailOidcService;

    @MockitoBean
    protected PasswordEncoder passwordEncoder;
}
