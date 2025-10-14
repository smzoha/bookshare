package com.zedapps.bookshare.validator;

import com.zedapps.bookshare.dto.login.LoginBaseDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Component
public class LoginDtoValidator implements Validator {

    private final LoginRepository loginRepository;

    public LoginDtoValidator(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return LoginBaseDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LoginBaseDto loginDto = (LoginBaseDto) target;

        if (!errors.hasFieldErrors("email") && StringUtils.isNotBlank(loginDto.getEmail())) {
            Optional<Login> login = loginRepository.findByEmail(loginDto.getEmail());

            if (login.isPresent()) {
                errors.rejectValue("email", "error.email.exists");
            }
        }

        if (!errors.hasFieldErrors("handle") && StringUtils.isNotBlank(loginDto.getHandle())) {
            Optional<Login> login = loginRepository.findByHandle(loginDto.getHandle());

            if (login.isPresent()) {
                errors.rejectValue("handle", "error.handle.exists");
            }
        }

        if (!errors.hasFieldErrors("confirmPassword")
                && loginDto instanceof RegistrationRequestDto
                && !Objects.equals(loginDto.getPassword(), ((RegistrationRequestDto) loginDto).getConfirmPassword())) {

            errors.rejectValue("confirmPassword", "error.password.do.not.match");
        }
    }
}
