package com.zedapps.bookshare.validator;

import com.zedapps.bookshare.dto.login.LoginBaseDto;
import com.zedapps.bookshare.dto.login.LoginManageDto;
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
            validateIfEmailExists(loginDto, errors);
        }

        if (!errors.hasFieldErrors("handle") && StringUtils.isNotBlank(loginDto.getHandle())) {
            validateIfHandleExists(loginDto, errors);
        }

        if (loginDto instanceof LoginManageDto) {
            validatePasswordForAdminPanel(((LoginManageDto) loginDto), errors);
        }

        if (loginDto instanceof RegistrationRequestDto) {
            validateConfirmPassword(((RegistrationRequestDto) loginDto), errors);
        }
    }

    private void validateIfEmailExists(LoginBaseDto loginDto, Errors errors) {
        Optional<Login> login = loginRepository.findByEmail(loginDto.getEmail());

        boolean emailExists = loginDto instanceof LoginManageDto && ((LoginManageDto) loginDto).getId() != null
                ? login.isPresent() && !Objects.equals(loginDto.getEmail(), login.get().getEmail())
                : login.isPresent();

        if (emailExists) {
            errors.rejectValue("email", "error.email.exists");
        }
    }

    private void validateIfHandleExists(LoginBaseDto loginDto, Errors errors) {
        Optional<Login> login = loginRepository.findByHandle(loginDto.getHandle());

        boolean handleExists = loginDto instanceof LoginManageDto && ((LoginManageDto) loginDto).getId() != null
                ? login.isPresent() && !Objects.equals(loginDto.getHandle(), login.get().getHandle())
                : login.isPresent();

        if (handleExists) {
            errors.rejectValue("handle", "error.handle.exists");
        }
    }

    private void validateConfirmPassword(RegistrationRequestDto loginDto, Errors errors) {
        if (!errors.hasFieldErrors("confirmPassword")
                && !Objects.equals(loginDto.getPassword(), loginDto.getConfirmPassword())) {

            errors.rejectValue("confirmPassword", "error.password.do.not.match");
        }
    }

    private void validatePasswordForAdminPanel(LoginManageDto loginDto, Errors errors) {
        if (loginDto.getId() == null) {
            String password = loginDto.getPassword();

            if (StringUtils.isBlank(password)) {
                errors.rejectValue("password", "error.blank");
            } else if (password.length() < 8 || password.length() > 32) {
                errors.rejectValue("password", "error.password.length");
            }
        }
    }
}
