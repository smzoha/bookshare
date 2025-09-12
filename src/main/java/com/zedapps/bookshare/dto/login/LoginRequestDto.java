package com.zedapps.bookshare.dto.login;

import lombok.Getter;
import lombok.Setter;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Getter
@Setter
public class LoginRequestDto {

    private String username;
    private String password;
}
