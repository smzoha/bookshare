package com.zedapps.bookshare.controller.login.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * @author smzoha
 * @since 14/10/25
 **/
@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class LoginAdminController {

    private final LoginService loginService;
    private final LoginDtoValidator loginDtoValidator;
    private final ApplicationEventPublisher publisher;

    @GetMapping
    public String getLoginList(@AuthenticationPrincipal LoginDetails loginDetails,
                               ModelMap model) {

        model.put("logins", loginService.getLoginList());

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.USER_LIST_VIEW)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        return "admin/user/userList";
    }

    @GetMapping("/new")
    public String addNewUser(ModelMap model) {
        LoginManageDto loginDto = new LoginManageDto();
        loginDto.setRole(Role.USER);
        loginDto.setActive(true);

        model.put("user", loginDto);
        model.put("roles", Role.values());

        return "admin/user/userForm";
    }

    @GetMapping("/{handle}")
    public String getUser(@AuthenticationPrincipal LoginDetails loginDetails, @PathVariable String handle, ModelMap model) {
        Login login = loginService.getLoginByHandle(handle);
        LoginManageDto loginDto = new LoginManageDto(login);

        model.put("user", loginDto);
        model.put("roles", Role.values());

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.USER_VIEW)
                .internal(true)
                .metadata(Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedUserEmail", login.getEmail()
                ))
                .build());

        return "admin/user/userForm";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") LoginManageDto loginDto, Errors errors,
                           @AuthenticationPrincipal LoginDetails loginDetails,
                           ModelMap model) {

        loginDtoValidator.validate(loginDto, errors);

        if (errors.hasErrors()) {
            model.put("roles", Role.values());
            return "admin/user/userForm";
        }

        loginService.saveLogin(loginDto);

        return "redirect:/admin";
    }
}
