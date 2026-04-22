package com.zedapps.bookshare.controller.api.login;

import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.service.login.LoginApiService;
import com.zedapps.bookshare.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author smzoha
 * @since 22/4/26
 **/
@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginApiController {

    private final LoginApiService loginApiService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto,
                                      Errors errors) {

        loginApiService.validateRegistration(registrationRequestDto, errors);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        LoginApiDto loginApiDto = loginApiService.registerLogin(registrationRequestDto);

        return ResponseEntity.ok().body(loginApiDto);
    }

    @PostMapping("/resetPassword/request")
    public ResponseEntity<?> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        return loginApiService.saveResetPasswordToken(email);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto,
                                           Errors errors) {

        return loginApiService.resetPassword(passwordResetDto, errors);
    }
}
