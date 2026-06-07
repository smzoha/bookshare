package com.zedapps.bookshare.validator;

import com.zedapps.bookshare.dto.login.LoginBaseDto;
import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 2/6/26
 **/
@ExtendWith(MockitoExtension.class)
class LoginDtoValidatorTest {

    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private LoginDtoValidator loginDtoValidator;

    private Login login;
    private Errors errors;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);
    }

    @Test
    void supports_loginBaseDtoSubclass_returnsTrue() {
        assertTrue(loginDtoValidator.supports(LoginBaseDto.class));
    }

    @Test
    void supports_unrelatedClass_returnsFalse() {
        assertFalse(loginDtoValidator.supports(String.class));
    }

    @Test
    void validate_newUserEmailAlreadyTaken_rejectsEmailField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(login));

        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setEmail(login.getEmail());

        errors = new BeanPropertyBindingResult(registrationRequestDto, "registrationRequestDto");
        loginDtoValidator.validate(registrationRequestDto, errors);

        verify(loginRepository).findByEmail(registrationRequestDto.getEmail());

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("email")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("email")).getCode()).isEqualTo("error.email.exists");
    }

    @Test
    void validate_updateUserWithOwnEmail_doesNotRejectEmailField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(login));

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(1L);
        loginManageDto.setEmail(login.getEmail());

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        verify(loginRepository).findByEmail(login.getEmail());

        assertThat(errors.hasFieldErrors()).isFalse();
        assertThat(errors.hasFieldErrors("email")).isFalse();
    }

    @Test
    void validate_updateUserEmailTakenByAnotherUser_rejectsEmailField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(login));

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(2L);
        loginManageDto.setEmail(login.getEmail());

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        verify(loginRepository).findByEmail(login.getEmail());

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("email")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("email")).getCode()).isEqualTo("error.email.exists");
    }

    @Test
    void validate_newUserHandleAlreadyTaken_rejectsHandleField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.of(login));

        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setEmail(login.getEmail());
        registrationRequestDto.setHandle(login.getHandle());

        errors = new BeanPropertyBindingResult(registrationRequestDto, "registrationRequestDto");
        loginDtoValidator.validate(registrationRequestDto, errors);

        verify(loginRepository).findByHandle(registrationRequestDto.getHandle());

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("handle")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("handle")).getCode()).isEqualTo("error.handle.exists");
    }

    @Test
    void validate_updateUserWithOwnHandle_doesNotRejectHandleField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.of(login));

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(1L);
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        verify(loginRepository).findByHandle(loginManageDto.getHandle());

        assertThat(errors.hasFieldErrors()).isFalse();
        assertThat(errors.hasFieldErrors("handle")).isFalse();
    }

    @Test
    void validate_registrationPasswordMatchesConfirm_doesNotReject() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setEmail(login.getEmail());
        registrationRequestDto.setHandle(login.getHandle());
        registrationRequestDto.setPassword("password");
        registrationRequestDto.setConfirmPassword("password");

        errors = new BeanPropertyBindingResult(registrationRequestDto, "registrationRequestDto");
        loginDtoValidator.validate(registrationRequestDto, errors);

        assertThat(errors.hasFieldErrors()).isFalse();
        assertThat(errors.hasFieldErrors("confirmPassword")).isFalse();
    }

    @Test
    void validate_registrationPasswordMismatch_rejectsConfirmPasswordField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setEmail(login.getEmail());
        registrationRequestDto.setHandle(login.getHandle());
        registrationRequestDto.setPassword("password");
        registrationRequestDto.setConfirmPassword("password1");

        errors = new BeanPropertyBindingResult(registrationRequestDto, "registrationRequestDto");
        loginDtoValidator.validate(registrationRequestDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("confirmPassword")).getCode()).isEqualTo("error.password.do.not.match");
    }

    @Test
    void validate_manageDtoNewUserPasswordBlank_rejectsPasswordField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setPassword("");

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("password")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("password")).getCode()).isEqualTo("error.blank");
    }

    @Test
    void validate_manageDtoNewUserPasswordTooShort_rejectsPasswordField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setPassword("1234");

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("password")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("password")).getCode()).isEqualTo("error.password.length");
    }

    @Test
    void validate_manageDtoNewUserPasswordTooLong_rejectsPasswordField() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setPassword("a".repeat(33));

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("password")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("password")).getCode()).isEqualTo("error.password.length");
    }

    @Test
    void validate_manageDtoNewUserValidPassword_doesNotReject() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setPassword("password");

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isFalse();
        assertThat(errors.hasFieldErrors("password")).isFalse();
    }

    @Test
    void validate_manageDtoExistingUser_skipsPasswordLengthValidation() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(1L);
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setPassword("1234");

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isFalse();
        assertThat(errors.hasFieldErrors("password")).isFalse();
    }

    @Test
    void validate_emailFieldAlreadyHasErrors_skipsEmailExistenceCheck() {
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(1L);
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        errors.rejectValue("email", "error.required");

        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("email")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("email")).getCode()).isNotEqualTo("error.email.exists");
    }

    @Test
    void validate_handleFieldAlreadyHasErrors_skipsHandleExistenceCheck() {
        when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());

        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setId(1L);
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());

        errors = new BeanPropertyBindingResult(loginManageDto, "loginManageDto");
        errors.rejectValue("handle", "error.required");

        loginDtoValidator.validate(loginManageDto, errors);

        assertThat(errors.hasFieldErrors()).isTrue();
        assertThat(errors.hasFieldErrors("handle")).isTrue();
        assertThat(Objects.requireNonNull(errors.getFieldError("handle")).getCode()).isNotEqualTo("error.handle.exists");
    }
}
