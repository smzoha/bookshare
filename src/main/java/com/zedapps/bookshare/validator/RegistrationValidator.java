package com.zedapps.bookshare.validator;

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
public class RegistrationValidator implements Validator {

    private LoginRepository loginRepository;

    public RegistrationValidator(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        RegistrationRequestDto registrationRequestDto = (RegistrationRequestDto) target;

        if (StringUtils.isNotBlank(registrationRequestDto.getEmail())) {
            Optional<Login> login = loginRepository.findByEmail(registrationRequestDto.getEmail());

            if (login.isPresent()) {
                errors.rejectValue("email", "error.email.exists");
            }
        }

        if (StringUtils.isNotBlank(registrationRequestDto.getHandle())) {
            Optional<Login> login = loginRepository.findByHandle(registrationRequestDto.getHandle());

            if (login.isPresent()) {
                errors.rejectValue("handle", "error.handle.exists");
            }
        }

        if (Objects.equals(registrationRequestDto.getPassword(), registrationRequestDto.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "error.password.do.not.match");
        }
    }
}
