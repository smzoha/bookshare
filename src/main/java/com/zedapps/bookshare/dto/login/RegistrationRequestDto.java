package com.zedapps.bookshare.dto.login;

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
public class RegistrationRequestDto extends LoginBaseDto {

    @NotBlank(message = "{error.blank}")
    @Size(min = 8, max = 32, message = "{error.min.max.length.exceeded}")
    private String confirmPassword;
}
