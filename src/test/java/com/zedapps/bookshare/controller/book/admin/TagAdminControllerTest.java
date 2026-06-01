package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 31/5/26
 **/
@WebMvcTest(TagAdminController.class)
@WithMockLoginDetails(role = "ADMIN")
@RecordApplicationEvents
public class TagAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookAdminService bookAdminService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private List<Tag> tags;

    @BeforeEach
    void setUp() {
        tags = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            Tag tag = TestUtils.getTag("Tag " + i);
            tag.setId((long) i);

            tags.add(tag);
        }
    }

    @Test
    void listTags_always_returnsTagListView() throws Exception {
        when(bookAdminService.getTagList()).thenReturn(tags);

        mockMvc.perform(get("/admin/tag"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tags", tags))
                .andExpect(view().name("admin/tag/tagList"));

        verify(bookAdminService).getTagList();
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showNewTagForm_always_returnsEmptyTagForm() throws Exception {
        mockMvc.perform(get("/admin/tag/new"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tag", hasProperty("id", equalTo(null))))
                .andExpect(model().attribute("tag", hasProperty("name", equalTo(null))))
                .andExpect(model().attribute("tag", hasProperty("books", empty())))
                .andExpect(model().attribute("tag", hasProperty("createdAt", equalTo(null))))
                .andExpect(view().name("admin/tag/tagForm"));
    }

    @Test
    void showEditTagForm_existingId_returnsPopulatedTagForm() throws Exception {
        Tag tag = tags.getFirst();
        when(bookAdminService.getTag(tag.getId())).thenReturn(tag);

        mockMvc.perform(get("/admin/tag/" + tag.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tag", hasProperty("id", equalTo(tag.getId()))))
                .andExpect(model().attribute("tag", hasProperty("name", equalTo(tag.getName()))))
                .andExpect(view().name("admin/tag/tagForm"));

        verify(bookAdminService).getTag(tag.getId());
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void saveTag_validTag_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/tag/save")
                        .param("name", "Tag 10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(bookAdminService).saveTag(any(Tag.class));
    }

    @Test
    void saveTag_blankName_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/admin/tag/save")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tag/tagForm"))
                .andExpect(model().attributeHasFieldErrors("tag", "name"));

        verify(bookAdminService, never()).saveTag(any(Tag.class));
    }

    @Test
    void saveTag_duplicateName_returnsFormWithError() throws Exception {
        Tag tag = tags.getFirst();
        when(bookAdminService.getTagByName(tag.getName())).thenReturn(Optional.of(tag));

        mockMvc.perform(post("/admin/tag/save")
                        .param("name", tag.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tag/tagForm"))
                .andExpect(model().attributeHasFieldErrorCode("tag", "name", "error.input.exists"));

        verify(bookAdminService, never()).saveTag(any(Tag.class));
    }
}
