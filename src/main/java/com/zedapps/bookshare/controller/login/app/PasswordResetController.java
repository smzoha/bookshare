package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.service.login.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final LoginRepository loginRepository;
    private final PasswordResetService passwordResetService;

    @GetMapping("/resetPasswordRequest")
    public String getResetPasswordRequest(@AuthenticationPrincipal LoginDetails loginDetails) {
        return "app/login/resetPasswordRequest";
    }

    @PostMapping("/resetPasswordRequest")
    public String submitResetPasswordRequest(@AuthenticationPrincipal LoginDetails loginDetails,
                                             @RequestParam String email,
                                             RedirectAttributes redirectAttributes,
                                             ModelMap model) {

        Optional<Login> login = loginRepository.findActiveLoginByEmail(email);

        if (login.isEmpty()) {
            model.put("error", true);
            return "app/login/resetPasswordRequest";
        }

        passwordResetService.savePasswordResetToken(email);

        redirectAttributes.addFlashAttribute("passwordResetReqSuccess", true);

        return "redirect:/login";
    }

    @GetMapping("/resetPassword")
    public String getResetPassword(@RequestParam String token, ModelMap model) {
        passwordResetService.validateToken(token);

        model.put("passwordResetDto", new PasswordResetDto(token));

        return "app/login/resetPassword";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid @ModelAttribute PasswordResetDto passwordResetDto,
                                Errors errors,
                                RedirectAttributes redirectAttributes) {

        passwordResetService.validatePasswordResetDto(passwordResetDto, errors);

        if (errors.hasErrors()) {
            return "app/login/resetPassword";
        }

        passwordResetService.resetPassword(passwordResetDto);

        redirectAttributes.addFlashAttribute("passwordResetSuccess", true);

        return "redirect:/login";
    }
}
