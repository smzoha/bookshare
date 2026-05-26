package com.zedapps.bookshare.controller.api.shelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.shelf.ShelfCreateDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDetailDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.shelf.ShelfApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 26/5/26
 **/
@WebMvcTest(ShelfApiController.class)
@WithMockLoginDetails(email = "test@test.com")
public class ShelfApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelfApiService shelfApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShelfDto shelfDto;

    @BeforeEach
    void setUp() {
        shelfDto = new ShelfDto("Shelf 1", "test@test.com", 1, false);
    }

    @Test
    void getShelves_authenticatedUser_returns200WithShelfDtoList() throws Exception {
        when(shelfApiService.getShelfDtoList(any(LoginDetails.class)))
                .thenReturn(List.of(shelfDto));

        mockMvc.perform(get("/api/v1/shelf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(shelfDto.name()));
    }

    @Test
    void getShelfDetail_ownedShelf_returns200WithShelfDetailDto() throws Exception {
        ShelfDetailDto shelfDetailDto = new ShelfDetailDto("Test 1", "test@test.com",
                false, List.of());

        when(shelfApiService.getShelfDetailDto(1L)).thenReturn(shelfDetailDto);

        mockMvc.perform(get("/api/v1/shelf/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(shelfDetailDto.name()))
                .andExpect(jsonPath("$.login").value(shelfDetailDto.login()))
                .andExpect(jsonPath("$.defaultShelf").value(shelfDetailDto.defaultShelf()))
                .andExpect(jsonPath("$.books.length()").value(0));
    }

    @Test
    void getShelfDetail_unownedShelf_returns403() throws Exception {
        when(shelfApiService.isShelfRequestInvalid(any(LoginDetails.class), eq(1L)))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/shelf/1"))
                .andExpect(status().isForbidden());

        verify(shelfApiService, never()).getShelfDetailDto(any());
    }

    @Test
    void createShelf_validRequest_returns201WithShelfDto() throws Exception {
        ShelfCreateDto shelfCreateDto = new ShelfCreateDto("Shelf 1");

        when(shelfApiService.saveShelf(any(ShelfCreateDto.class),
                any(LoginDetails.class))).thenReturn(shelfDto);

        mockMvc.perform(post("/api/v1/shelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shelfCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(shelfCreateDto.name()))
                .andExpect(jsonPath("$.login").value("test@test.com"));
    }

    @Test
    void createShelf_duplicateName_returns400WithValidationError() throws Exception {
        ShelfCreateDto shelfCreateDto = new ShelfCreateDto("Shelf 1");

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(2);
            errors.rejectValue("name", "error.already.exists");

            return null;
        }).when(shelfApiService).validateShelfCreation(any(LoginDetails.class),
                eq(shelfCreateDto.name()), any(Errors.class));

        mockMvc.perform(post("/api/v1/shelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shelfCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1));
    }

    @Test
    void createShelf_bindingErrors_returns400() throws Exception {
        ShelfCreateDto shelfCreateDto = new ShelfCreateDto(null);

        mockMvc.perform(post("/api/v1/shelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shelfCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1));
    }
}
