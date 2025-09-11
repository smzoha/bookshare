package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginRequestDto;
import com.zedapps.bookshare.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Controller
@RequestMapping
public class LoginController {

    @GetMapping("/login")
    public String getLogin(ModelMap model) {
        if (Utils.isAuthenticated()) return "redirect:/";

        model.put("login", new LoginRequestDto());

        return "app/login/login";
    }
}
