package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.repository.book.TagRepository;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/tags")
public class TagAdminController {

    private final TagRepository tagRepository;

    public TagAdminController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public String getTagList(ModelMap model) {
        model.put("tags", tagRepository.findAll());

        return "admin/tags/tagList";
    }

    @GetMapping("/new")
    public String createTag(ModelMap model) {
        model.put("tag", new Tag());

        return "admin/tags/tagForm";
    }

    @GetMapping("{id}")
    public String getTag(@PathVariable Long id, ModelMap model) {
        Tag tag = tagRepository.findById(id).orElseThrow(NoResultException::new);
        model.put("tag", tag);

        return "admin/tags/tagForm";
    }

    @PostMapping("/save")
    public String saveTag(@Valid @ModelAttribute Tag tag,
                          Errors errors) {

        validateTagUniqueness(tag, errors);

        if (errors.hasErrors()) {
            return "admin/tags/tagForm";
        }

        tagRepository.save(tag);

        return "redirect:/admin";
    }

    private void validateTagUniqueness(Tag tag, Errors errors) {
        if (StringUtils.isBlank(tag.getName())) {
            return;
        }

        Optional<Tag> tagOptional = tagRepository.findTagByName(tag.getName());

        if (tagOptional.isPresent() && !Objects.equals(tagOptional.get().getId(), tag.getId())) {
            errors.rejectValue("name", "error.input.exists");
        }
    }
}
