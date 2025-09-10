package com.zedapps.bookshare.config;

import com.zedapps.bookshare.entity.login.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> {
                    requests.requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                            .requestMatchers("/manage/**").hasAnyRole(Role.ADMIN.name(), Role.MODERATOR.name())
                            .requestMatchers("/manage/book").hasRole(Role.AUTHOR.name())
                            .requestMatchers("/profile/**").hasAnyRole(Role.getAllRoleNames())
                            .anyRequest().permitAll();
                })
                .formLogin((form) -> form.loginPage("/login")
                        .permitAll())
                .logout((logout) -> logout.logoutUrl("/logout")
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .build();
    }
}
