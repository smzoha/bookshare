package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.book.Genre;
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
@WebMvcTest(GenreAdminController.class)
@WithMockLoginDetails(role = "ADMIN")
@RecordApplicationEvents
public class GenreAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookAdminService bookAdminService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private List<Genre> genres;

    @BeforeEach
    void setUp() {
        genres = new ArrayList<>();

        for (int i = 1; i < 10; i++) {
            Genre genre = TestUtils.getGenre("Genre" + i);
            genre.setId((long) i);

            genres.add(genre);
        }
    }

    @Test
    void listGenres_always_returnsGenreListView() throws Exception {
        when(bookAdminService.getGenreList()).thenReturn(genres);

        mockMvc.perform(get("/admin/genre"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("genres", genres))
                .andExpect(view().name("admin/genre/genreList"));

        verify(bookAdminService).getGenreList();
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showNewGenreForm_always_returnsEmptyGenreForm() throws Exception {
        mockMvc.perform(get("/admin/genre/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("genre"))
                .andExpect(model().attribute("genre", hasProperty("id", equalTo(null))))
                .andExpect(model().attribute("genre", hasProperty("name", equalTo(null))))
                .andExpect(model().attribute("genre", hasProperty("books", empty())))
                .andExpect(model().attribute("genre", hasProperty("createdAt", equalTo(null))))
                .andExpect(view().name("admin/genre/genreForm"));
    }

    @Test
    void showEditGenreForm_existingId_returnsPopulatedGenreForm() throws Exception {
        Genre genre = genres.getFirst();
        when(bookAdminService.getGenre(genre.getId())).thenReturn(genre);

        mockMvc.perform(get("/admin/genre/" + genre.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("genre", hasProperty("id", equalTo(genre.getId()))))
                .andExpect(model().attribute("genre", hasProperty("name", equalTo(genre.getName()))))
                .andExpect(view().name("admin/genre/genreForm"));

        verify(bookAdminService).getGenre(genre.getId());
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void saveGenre_validGenre_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/genre/save")
                        .param("name", "Genre 11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(bookAdminService).saveGenre(any(Genre.class));
    }

    @Test
    void saveGenre_blankName_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/admin/genre/save")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/genre/genreForm"))
                .andExpect(model().attributeHasFieldErrors("genre", "name"));

        verify(bookAdminService, never()).saveGenre(any(Genre.class));
    }

    @Test
    void saveGenre_duplicateName_returnsFormWithError() throws Exception {
        Genre genre = genres.getFirst();
        when(bookAdminService.getGenreByName(genre.getName())).thenReturn(Optional.of(genre));

        mockMvc.perform(post("/admin/genre/save")
                        .param("name", genre.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/genre/genreForm"))
                .andExpect(model().attributeHasFieldErrorCode("genre", "name", "error.input.exists"));

        verify(bookAdminService, never()).saveGenre(any(Genre.class));
    }
}
