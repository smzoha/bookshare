package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.entity.login.enums.Role;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author smzoha
 * @since 14/10/25
 **/
@Controller
@RequestMapping("/admin/users")
public class LoginAdminController {

    private final LoginService loginService;
    private final LoginDtoValidator loginDtoValidator;

    public LoginAdminController(LoginService loginService, LoginDtoValidator loginDtoValidator) {
        this.loginService = loginService;
        this.loginDtoValidator = loginDtoValidator;
    }

    @GetMapping
    public String getLoginList(ModelMap model) {
        model.put("logins", loginService.getLoginList());
        return "admin/users/userList";
    }

    @GetMapping("/new")
    public String addNewUser(ModelMap model) {
        LoginManageDto loginDto = new LoginManageDto();
        loginDto.setRole(Role.USER);

        model.put("user", loginDto);
        model.put("roles", Role.values());

        return "admin/users/userForm";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") LoginManageDto login, Errors errors, ModelMap model) {
        loginDtoValidator.validate(login, errors);

        if (errors.hasErrors()) {
            model.put("roles", Role.values());
            return "admin/users/userForm";
        }

        loginService.saveLogin(login);

        return "redirect:/admin";
    }
}
