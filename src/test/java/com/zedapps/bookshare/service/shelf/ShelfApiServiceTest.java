package com.zedapps.bookshare.service.shelf;

import com.zedapps.bookshare.dto.api.shelf.ShelfCreateDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDetailDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 16/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ShelfApiServiceTest {

    @InjectMocks
    private ShelfApiService shelfApiService;

    @Mock
    private ShelfService shelfService;

    @Mock
    private LoginService loginService;

    @Mock
    private BookApiService bookApiService;

    @Mock
    private Errors errors;

    private Login login;
    private LoginDetails loginDetails;

    private Shelf shelf1;
    private Shelf shelf2;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), login.isActive());

        shelf1 = TestUtils.getShelf(login, "Shelf 1", true);
        shelf1.setId(1L);

        shelf2 = TestUtils.getShelf(login, "Shelf 2", false);
        shelf2.setId(2L);

        lenient().when(loginService.getLogin(loginDetails.getEmail())).thenReturn(login);
    }

    @Test
    void getShelfDtoList_validUser_returnsMappedShelfDtoList() {
        when(shelfService.getShelvesForCollection(login.getEmail())).thenReturn(List.of(shelf1, shelf2));

        List<ShelfDto> shelfDtoList = shelfApiService.getShelfDtoList(loginDetails);

        assertEquals(2, shelfDtoList.size());
        assertEquals(shelf1.getName(), shelfDtoList.getFirst().name());
        assertEquals(shelf1.getUser().getEmail(), shelfDtoList.getFirst().login());
    }

    @Test
    void getShelfDtoList_emptyShelfList_returnsEmptyList() {
        when(shelfService.getShelvesForCollection(login.getEmail())).thenReturn(Collections.emptyList());

        List<ShelfDto> shelfDtoList = shelfApiService.getShelfDtoList(loginDetails);

        assertTrue(shelfDtoList.isEmpty());
    }

    @Test
    void getShelfDetailDto_validShelfId_returnsMappedDetailDto() {
        when(shelfService.getShelfById(shelf1.getId())).thenReturn(shelf1);

        ShelfDetailDto shelfDetailDto = shelfApiService.getShelfDetailDto(shelf1.getId());

        assertEquals(shelf1.getName(), shelfDetailDto.name());
        assertEquals(shelf1.getUser().getEmail(), shelfDetailDto.login());
        assertEquals(shelf1.isDefaultShelf(), shelfDetailDto.defaultShelf());
    }

    @Test
    void saveShelf_newShelfDto_savesShelfAndReturnsDtoWithZeroCount() {
        ShelfCreateDto shelfCreateDto = new ShelfCreateDto("New Shelf");

        ShelfDto shelfDto = shelfApiService.saveShelf(shelfCreateDto, loginDetails);

        verify(shelfService).saveShelf(any());

        assertNotNull(shelfDto);
        assertEquals(shelfCreateDto.name(), shelfDto.name());
        assertEquals(0, shelfDto.bookCount());
    }

    @Test
    void isShelfRequestInvalid_shelfOwnedByRequester_returnsFalse() {
        when(shelfService.getShelfById(shelf1.getId())).thenReturn(shelf1);

        boolean shelfRequestInvalid = shelfApiService.isShelfRequestInvalid(loginDetails, shelf1.getId());

        assertFalse(shelfRequestInvalid);
    }

    @Test
    void isShelfRequestInvalid_shelfOwnedByOtherUser_returnsTrue() {
        Login otherLogin = TestUtils.getLogin("other@test.com", "otherLogin", true);

        Shelf otherShelf = TestUtils.getShelf(otherLogin, "Shelf 3", true);
        otherShelf.setId(100L);

        when(shelfService.getShelfById(otherShelf.getId())).thenReturn(otherShelf);

        boolean shelfRequestInvalid = shelfApiService.isShelfRequestInvalid(loginDetails, otherShelf.getId());

        assertTrue(shelfRequestInvalid);
    }

    @Test
    void validateShelfCreation_shelfNameAlreadyExists_rejectsWithError() {
        when(shelfService.isShelfExistsForUser(shelf1.getName(), loginDetails.getEmail())).thenReturn(true);

        shelfApiService.validateShelfCreation(loginDetails, shelf1.getName(), errors);

        verify(errors).rejectValue("name", "error.already.exists");
    }

    @Test
    void validateShelfCreation_newShelfName_doesNotReject() {
        when(shelfService.isShelfExistsForUser("Test Shelf", loginDetails.getEmail())).thenReturn(false);

        shelfApiService.validateShelfCreation(loginDetails, "Test Shelf", errors);

        verify(errors, never()).rejectValue(anyString(), anyString());

    }
}
