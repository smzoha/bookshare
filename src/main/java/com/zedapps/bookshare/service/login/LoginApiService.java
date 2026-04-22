package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.Utils;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.List;

/**
 * @author smzoha
 * @since 22/4/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginApiService {

    private final LoginService loginService;
    private final LoginRepository loginRepository;
    private final LoginDtoValidator loginDtoValidator;
    private final PasswordResetService passwordResetService;

    @Transactional
    public LoginApiDto registerLogin(RegistrationRequestDto registrationRequestDto) {
        Login login = loginService.createLogin(registrationRequestDto);

        return new LoginApiDto(login.getFirstName(), login.getLastName(), login.getHandle(), login.getEmail(),
                login.getBio(), Utils.getImageUrl(login.getProfilePicture()), login.getRole().name(),
                login.isActive(), null, null, null);
    }

    @Transactional
    public ResponseEntity<?> saveResetPasswordToken(String email) {
        if (!loginRepository.existsLoginByEmail(email)) {
            return ResponseEntity.ok().body(new ErrorResponseDto(List.of("error.invalid.email")));
        }

        passwordResetService.savePasswordResetToken(email);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> resetPassword(PasswordResetDto passwordResetDto, Errors errors) {
        passwordResetService.validatePasswordResetDto(passwordResetDto, errors);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        passwordResetService.resetPassword(passwordResetDto);

        return ResponseEntity.ok().build();
    }

    public void validateRegistration(RegistrationRequestDto registrationRequestDto, Errors errors) {
        loginDtoValidator.validate(registrationRequestDto, errors);
    }
}
