package com.zedapps.bookshare.controller.api.auth;

import com.zedapps.bookshare.dto.api.auth.AuthToken;
import com.zedapps.bookshare.dto.api.auth.TokenRequest;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/token")
    public ResponseEntity<AuthToken> getAuthToken(@RequestBody TokenRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            LoginDetails loginDetails = (LoginDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(loginDetails);

            return ResponseEntity.ok(new AuthToken(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
