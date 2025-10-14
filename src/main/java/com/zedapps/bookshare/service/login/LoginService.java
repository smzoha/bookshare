package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Login> getLoginList() {
        return loginRepository.findAll();
    }

    public Login getLogin(String email) {
        return loginRepository.findActiveLoginByEmail(email).orElseThrow(NoResultException::new);
    }

    @Transactional
    public Login saveLogin(@Valid LoginManageDto loginDto) {
        Login login = loginRepository.findActiveLoginByEmail(loginDto.getEmail()).orElse(new Login());
        updateLoginFromManageDto(loginDto, login);

        login = loginRepository.save(login);

        return login;
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

    private void updateLoginFromManageDto(LoginManageDto loginManageDto, Login login) {
        login.setEmail(loginManageDto.getEmail());

        if (login.getId() == null || passwordEncoder.matches(loginManageDto.getPassword(), login.getPassword())) {
            login.setPassword(passwordEncoder.encode(loginManageDto.getPassword()));
        }

        login.setFirstName(loginManageDto.getFirstName());
        login.setLastName(loginManageDto.getLastName());
        login.setHandle(loginManageDto.getHandle());
        login.setRole(loginManageDto.getRole());
        login.setActive(loginManageDto.isActive());
    }
}
