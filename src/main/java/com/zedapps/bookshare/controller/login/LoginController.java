package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginRequestDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.Utils;
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
 * @since 11/9/25
 **/
@Controller
@RequestMapping
public class LoginController {

    private final LoginService loginService;
    private final LoginDtoValidator loginDtoValidator;

    public LoginController(LoginService loginService, LoginDtoValidator loginDtoValidator) {
        this.loginService = loginService;
        this.loginDtoValidator = loginDtoValidator;
    }

    @GetMapping("/login")
    public String getLogin(ModelMap model) {
        if (Utils.isAuthenticated()) return "redirect:/";

        model.put("login", new LoginRequestDto());
        model.put("register", new RegistrationRequestDto());

        return "app/login/login";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("register") RegistrationRequestDto registrationRequestDto,
                           Errors errors,
                           ModelMap model) {

        loginDtoValidator.validate(registrationRequestDto, errors);

        if (errors.hasErrors()) {
            model.put("login", new LoginRequestDto());

            return "app/login/login";
        }

        Login login = loginService.createLogin(registrationRequestDto);

        return "redirect:/login";
    }
}
