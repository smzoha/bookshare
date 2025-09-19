package com.zedapps.bookshare.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Getter
@Setter
public class RegistrationRequestDto {

    @Email(message = "{error.email}")
    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String email;

    @NotBlank(message = "{error.blank}")
    @Size(min = 8, max = 32, message = "{error.min.max.length.exceeded}")
    private String password;

    @NotBlank(message = "{error.blank}")
    @Size(min = 8, max = 32, message = "{error.min.max.length.exceeded}")
    private String confirmPassword;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String firstName;

    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String lastName;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String handle;
}
