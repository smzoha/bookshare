package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;
    private final ActivityService activityService;

    public List<Login> getLoginList() {
        return loginRepository.findAll();
    }

    public List<Login> getActiveLoginList() {
        return loginRepository.findAllByActive(true);
    }

    public Login getLogin(String email) {
        return loginRepository.findActiveLoginByEmail(email).orElseThrow(NoResultException::new);
    }

    public Login getLoginByHandle(String handle) {
        return loginRepository.findByHandle(handle).orElseThrow(NoResultException::new);
    }

    @Transactional
    public void saveLogin(@Valid LoginManageDto loginDto) {
        Login login = (loginDto.getId() != null
                ? loginRepository.findById(loginDto.getId())
                : loginRepository.findByEmail(loginDto.getEmail())).orElse(new Login());

        updateLoginFromManageDto(loginDto, login);

        if (login.getId() == null) {
            setupShelvesForNewLogin(login);
        }

        login = loginRepository.save(login);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(loginDto.getId() != null ? ActivityType.USER_UPDATE : ActivityType.USER_ADD,
                login.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedUserEmail", login.getEmail(),
                        "loginFirstName", login.getFirstName(),
                        "loginLastName", login.getLastName()
                ));
    }

    @Transactional
    public void createLogin(RegistrationRequestDto registrationDto) {
        Login login = createLoginFromRegistrationDto(registrationDto);
        setupShelvesForNewLogin(login);

        login = loginRepository.save(login);

        activityService.saveActivityOutbox(ActivityType.REGISTER,
                login.getId(),
                Map.of(
                        "affectedUserEmail", login.getEmail(),
                        "loginFirstName", login.getFirstName(),
                        "loginLastName", login.getLastName()
                ));
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

        if (login.getId() == null || (StringUtils.isNotBlank(loginManageDto.getPassword())
                && !passwordEncoder.matches(loginManageDto.getPassword(), login.getPassword()))) {

            login.setPassword(passwordEncoder.encode(loginManageDto.getPassword()));
        }

        login.setFirstName(loginManageDto.getFirstName());
        login.setLastName(loginManageDto.getLastName());
        login.setHandle(loginManageDto.getHandle());
        login.setRole(loginManageDto.getRole());
        login.setActive(loginManageDto.isActive());
        login.setBio(loginManageDto.getBio());
        login.setProfilePicture(Objects.isNull(loginManageDto.getProfilePictureId()) ? null
                : imageRepository.findById(loginManageDto.getProfilePictureId()).orElse(null));
    }

    private void setupShelvesForNewLogin(Login login) {
        Shelf.DEFAULT_SHELF_NAMES.forEach(shelfName -> {
            Shelf shelf = new Shelf();
            shelf.setName(shelfName);
            shelf.setUser(login);
            shelf.setDefaultShelf(true);

            login.getShelves().add(shelf);
        });
    }
}
