package com.zedapps.bookshare.service.shelf;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 16/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ShelfServiceTest {

    @InjectMocks
    private ShelfService shelfService;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private ActivityService activityService;

    private Login login;
    private Shelf shelf1;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        shelf1 = TestUtils.getShelf(login, "Shelf 1", false);
        shelf1.setId(4L);
    }

    @Test
    void getShelvesForCollection_validEmail_returnsShelvesList() {
        List<Shelf> shelves = List.of(shelf1);
        when(shelfRepository.getShelvesForCollection(login.getEmail())).thenReturn(shelves);

        List<Shelf> shelvesForCollection = shelfService.getShelvesForCollection(login.getEmail());

        assertEquals(1, shelvesForCollection.size());
        assertEquals(shelf1.getId(), shelvesForCollection.getFirst().getId());
        assertEquals(shelf1.getName(), shelvesForCollection.getFirst().getName());
    }

    @Test
    void getShelfById_existingId_returnsShelf() {
        when(shelfRepository.findById(shelf1.getId())).thenReturn(Optional.of(shelf1));

        Shelf shelf = shelfService.getShelfById(shelf1.getId());

        assertEquals(shelf1.getId(), shelf.getId());
        assertEquals(shelf1.getName(), shelf.getName());
        assertEquals(shelf1.isDefaultShelf(), shelf.isDefaultShelf());
    }

    @Test
    void getShelfById_missingId_throwsNoResultException() {
        when(shelfRepository.findById(shelf1.getId())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> shelfService.getShelfById(shelf1.getId()));
    }

    @Test
    void isShelfExistsForUser_existingNameAndEmail_returnsTrue() {
        when(shelfRepository.existsShelfByNameAndUser_Email(shelf1.getName(), shelf1.getUser().getEmail()))
                .thenReturn(true);

        boolean shelfExistsForUser = shelfService.isShelfExistsForUser(shelf1.getName(), shelf1.getUser().getEmail());

        assertTrue(shelfExistsForUser);
    }

    @Test
    void isShelfExistsForUser_nonExistingName_returnsFalse() {
        when(shelfRepository.existsShelfByNameAndUser_Email(anyString(), anyString()))
                .thenReturn(false);

        boolean shelfExistsForUser = shelfService.isShelfExistsForUser(shelf1.getName(), shelf1.getUser().getEmail());

        assertFalse(shelfExistsForUser);
    }

    @Test
    void saveShelf_newShelf_persistsShelf() {
        Shelf newShelf = TestUtils.getShelf(login, "Shelf 1", false);

        when(shelfRepository.save(newShelf)).thenReturn(shelf1);

        shelfService.saveShelf(newShelf);

        verify(shelfRepository).save(newShelf);
    }

    @Test
    void saveShelf_newShelf_firesShelfAddOutbox() {
        Shelf newShelf = TestUtils.getShelf(login, "Shelf 1", false);

        when(shelfRepository.save(newShelf)).thenReturn(shelf1);

        shelfService.saveShelf(newShelf);

        verify(activityService).saveActivityOutbox(eq(ActivityType.SHELF_ADD),
                eq(shelf1.getId()), anyMap());
    }
}
