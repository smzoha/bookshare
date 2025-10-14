package com.zedapps.bookshare.dto.login;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author smzoha
 * @since 14/10/25
 **/
@Getter
@Setter
@NoArgsConstructor
public class LoginManageDto extends LoginBaseDto {

    private Long id;

    private String password;

    @Size(max = 4000, message = "{error.max.length.exceeded}")
    private String bio;

    @NotNull(message = "{error.required}")
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active;

    public LoginManageDto(Login login) {
        this.id = login.getId();
        this.bio = login.getBio();
        this.role = login.getRole();
        this.active = login.isActive();

        setFirstName(login.getFirstName());
        setLastName(login.getLastName());
        setEmail(login.getEmail());
        setPassword(login.getPassword());
        setHandle(login.getHandle());
    }
}
