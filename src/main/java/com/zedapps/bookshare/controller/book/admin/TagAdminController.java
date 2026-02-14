package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/tag")
public class TagAdminController {

    private final BookAdminService bookAdminService;
    private final LoginService loginService;
    private final ApplicationEventPublisher publisher;

    public TagAdminController(BookAdminService bookAdminService,
                              LoginService loginService,
                              ApplicationEventPublisher publisher) {

        this.bookAdminService = bookAdminService;
        this.loginService = loginService;
        this.publisher = publisher;
    }

    @GetMapping
    public String getTagList(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        model.put("tags", bookAdminService.getTagList());

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
                .eventType(ActivityType.TAG_LIST_VIEW)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        return "admin/tag/tagList";
    }

    @GetMapping("/new")
    public String createTag(ModelMap model) {
        model.put("tag", new Tag());

        return "admin/tag/tagForm";
    }

    @GetMapping("{id}")
    public String getTag(@AuthenticationPrincipal LoginDetails loginDetails, @PathVariable Long id, ModelMap model) {
        Tag tag = bookAdminService.getTag(id);
        model.put("tag", tag);

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
                .eventType(ActivityType.TAG_VIEW)
                .metadata(Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedTagId", tag.getId()
                ))
                .internal(true)
                .build());

        return "admin/tag/tagForm";
    }

    @PostMapping("/save")
    public String saveTag(@Valid @ModelAttribute Tag tag,
                          Errors errors) {

        validateTagUniqueness(tag, errors);

        if (errors.hasErrors()) {
            return "admin/tag/tagForm";
        }

        tag = bookAdminService.saveTag(tag);

        return "redirect:/admin";
    }

    private void validateTagUniqueness(Tag tag, Errors errors) {
        if (StringUtils.isBlank(tag.getName())) {
            return;
        }

        Optional<Tag> tagOptional = bookAdminService.getTagByName(tag.getName());

        if (tagOptional.isPresent() && !Objects.equals(tagOptional.get().getId(), tag.getId())) {
            errors.rejectValue("name", "error.input.exists");
        }
    }
}
