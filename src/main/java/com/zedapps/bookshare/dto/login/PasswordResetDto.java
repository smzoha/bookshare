package com.zedapps.bookshare.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetDto {

    @NotNull(message = "{error.required}")
    private String token;

    @NotBlank(message = "{error.blank}")
    @Size(min = 8, max = 32, message = "{error.min.max.length.exceeded}")
    private String password;

    @NotBlank(message = "{error.blank}")
    @Size(min = 8, max = 32, message = "{error.min.max.length.exceeded}")
    private String confirmPassword;

    public PasswordResetDto(String token) {
        this.token = token;
    }
}
