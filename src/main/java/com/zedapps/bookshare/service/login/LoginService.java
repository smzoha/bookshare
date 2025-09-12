package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Login createLogin(RegistrationRequestDto registrationDto) {
        Login login = createLoginFromRegistrationDto(registrationDto);

        login = loginRepository.save(login);

        return login;
    }

    private Login createLoginFromRegistrationDto(RegistrationRequestDto registrationDto) {
        Login login = new Login();

        login.setEmail(registrationDto.getEmail());
        login.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        login.setFirstName(registrationDto.getFirstName());
        login.setLastName(registrationDto.getLastName());
        login.setHandle(registrationDto.getHandle());

        login.setRole(Role.USER);
        login.setActive(true);

        return login;
    }
}
