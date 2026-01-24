package com.zedapps.bookshare.config;

import com.zedapps.bookshare.entity.login.enums.Role;
import com.zedapps.bookshare.service.LoginDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
public class SecurityConfig {

    private final LoginDetailService loginDetailService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(LoginDetailService loginDetailService, PasswordEncoder passwordEncoder) {
        this.loginDetailService = loginDetailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> {
                    requests.requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
                            .requestMatchers("/manage/**").hasAnyAuthority(Role.ADMIN.name(), Role.MODERATOR.name())
                            .requestMatchers("/manage/book").hasAuthority(Role.AUTHOR.name())
                            .requestMatchers("/profile/**").hasAnyAuthority(Role.getAllRoleNames())
                            .requestMatchers("/book/add*", "/book/remove*", "/book/update*", "/book/like",
                                    "/shelf/add").authenticated()
                            .anyRequest().permitAll();
                })
                .formLogin((form) -> form.loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout((logout) -> logout.logoutUrl("/logout")
                        .logoutUrl("/logout")
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
}
