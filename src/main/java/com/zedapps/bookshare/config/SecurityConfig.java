package com.zedapps.bookshare.config;

import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.service.auth.LoginDetailOidcService;
import com.zedapps.bookshare.service.auth.LoginDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginDetailService loginDetailService;
    private final LoginDetailOidcService loginDetailOidcService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests((requests) -> {
                    requests.requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
                            .requestMatchers("/manage/**").hasAnyAuthority(Role.ADMIN.name(), Role.MODERATOR.name())
                            .requestMatchers("/manage/book").hasAuthority(Role.AUTHOR.name())
                            .requestMatchers("/profile/**").hasAnyAuthority(Role.getAllRoleNames())
                            .requestMatchers("/book/add*", "/book/remove*", "/book/update*", "/book/like",
                                    "/shelf/add", "/collection/**").authenticated()
                            .requestMatchers("/resetPasswordRequest", "/resetPassword").anonymous()
                            .requestMatchers("/author/apply").hasAuthority(Role.USER.name())
                            .requestMatchers("/author/bookRequest").hasAuthority(Role.AUTHOR.name())
                            .requestMatchers("/actuator/**").hasAuthority(Role.ADMIN.name())
                            .anyRequest().permitAll();
                })
                .formLogin((form) -> form.loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(loginDetailOidcService)))
                .logout((logout) -> logout.logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(loginDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
