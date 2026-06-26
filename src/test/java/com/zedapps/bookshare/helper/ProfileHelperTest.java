package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.FeedService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.ui.ModelMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 5/6/26
 **/
@ExtendWith(MockitoExtension.class)
class ProfileHelperTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private LoginService loginService;

    @Mock
    private ShelfService shelfService;

    @Mock
    private FeedService feedService;

    @Mock
    private ReadingChallengeRepository readingChallengeRepository;

    @Mock
    private Page<FeedEntry> feedPage;

    @InjectMocks
    private ProfileHelper profileHelper;

    private Login profileLogin;
    private Login authLogin;

    private Shelf defaultShelf;
    private Shelf customShelf;

    @BeforeEach
    void setUp() {
        profileLogin = TestUtils.getLogin("friend@test.com", "friend", true);
        profileLogin.setId(1L);

        authLogin = TestUtils.getLogin("test@test.com", "test", true);
        authLogin.setId(2L);

        defaultShelf = TestUtils.getShelf(profileLogin, Shelf.SHELF_READ, true);
        defaultShelf.setId(1L);
        defaultShelf.setBooks(Set.of());

        customShelf = TestUtils.getShelf(profileLogin, "Custom", false);
        customShelf.setId(2L);
        customShelf.setBooks(Set.of());

        lenient().when(shelfService.getShelvesForCollection(profileLogin.getEmail())).thenReturn(List.of(defaultShelf, customShelf));

        lenient().when(profileService.getDistinctReadingProgressList(profileLogin)).thenReturn(List.of());
        lenient().when(profileService.getConnectionsByPerson(profileLogin)).thenReturn(List.of());

        lenient().when(feedService.getFeedEntries(eq(profileLogin), eq(5), eq(0))).thenReturn(feedPage);
        lenient().when(feedService.mapToFeedDtoList(any())).thenReturn(List.of());
    }

    @Test
    void setupReferenceData_ownProfile_setsOwnProfileTrue() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(profileLogin.getEmail(), profileLogin.getHandle(), true);
        when(loginService.getLogin(profileLogin.getEmail())).thenReturn(profileLogin);

        ModelMap model = new ModelMap();
        profileHelper.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

        assertThat(model.get("ownProfile")).isEqualTo(true);
    }

    @Test
    void setupReferenceData_otherProfile_setsOwnProfileFalse() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(authLogin.getEmail(), authLogin.getHandle(), true);
        when(loginService.getLogin(profileLogin.getEmail())).thenReturn(profileLogin);
        when(loginService.getLogin(authLogin.getEmail())).thenReturn(authLogin);

        ModelMap model = new ModelMap();
        profileHelper.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

        assertThat(model.get("ownProfile")).isEqualTo(false);
    }

    @Test
    void setupReferenceData_calculatesTotalBookCount() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(authLogin.getEmail(), authLogin.getHandle(), true);
        when(loginService.getLogin(profileLogin.getEmail())).thenReturn(profileLogin);
        when(loginService.getLogin(authLogin.getEmail())).thenReturn(authLogin);

        defaultShelf.setBooks(Set.of(mock(ShelvedBook.class), mock(ShelvedBook.class)));
        customShelf.setBooks(Set.of(mock(ShelvedBook.class)));

        ModelMap model = new ModelMap();
        profileHelper.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

        assertThat(model.get("totalBooks")).isEqualTo(3);
    }

    @Test
    void setupConnectionRefData_noRelationship_allConnectionFlagsFalse() {
        ModelMap model = new ModelMap();
        profileHelper.setupConnectionRefData(model, profileLogin, authLogin);

        assertThat(model.get("friendReqSent")).isEqualTo(false);
        assertThat(model.get("friendReqReceived")).isEqualTo(false);
        assertThat(model.get("isFriends")).isEqualTo(false);
        assertThat(model.get("showFriendReqBtn")).isEqualTo(true);
    }

    @Test
    void setupConnectionRefData_friendRequestSentByAuthUser_setsFriendReqSentTrue() {
        when(profileService.getFriendRequest(profileLogin, authLogin)).thenReturn(Optional.of(mock(FriendRequest.class)));

        ModelMap model = new ModelMap();
        profileHelper.setupConnectionRefData(model, profileLogin, authLogin);

        assertThat(model.get("friendReqSent")).isEqualTo(true);
        assertThat(model.get("showFriendReqBtn")).isEqualTo(false);
    }

    @Test
    void setupConnectionRefData_friendRequestReceivedByAuthUser_setsFriendReqReceivedTrue() {
        when(profileService.getFriendRequest(profileLogin, authLogin)).thenReturn(Optional.empty());
        when(profileService.getFriendRequest(authLogin, profileLogin)).thenReturn(Optional.of(mock(FriendRequest.class)));

        ModelMap model = new ModelMap();
        profileHelper.setupConnectionRefData(model, profileLogin, authLogin);

        assertThat(model.get("friendReqReceived")).isEqualTo(true);
        assertThat(model.get("showFriendReqBtn")).isEqualTo(false);
    }

    @Test
    void setupConnectionRefData_alreadyConnected_setsIsFriendsTrue() {
        Connection connection = new Connection(authLogin, profileLogin);
        when(profileService.getConnectionsByPerson(authLogin)).thenReturn(List.of(connection));

        ModelMap model = new ModelMap();
        profileHelper.setupConnectionRefData(model, profileLogin, authLogin);

        assertThat(model.get("isFriends")).isEqualTo(true);
        assertThat(model.get("showFriendReqBtn")).isEqualTo(false);
    }

    @Test
    void setupShelves_separatesDefaultShelvesFromCustomShelves() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(authLogin.getEmail(), authLogin.getHandle(), true);
        when(loginService.getLogin(profileLogin.getEmail())).thenReturn(profileLogin);
        when(loginService.getLogin(authLogin.getEmail())).thenReturn(authLogin);

        ModelMap model = new ModelMap();
        profileHelper.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

        assertThat(model.get("defaultShelves")).asInstanceOf(map(Long.class, String.class))
                .containsKey(defaultShelf.getId())
                .doesNotContainKey(customShelf.getId());

        assertThat(model.get("shelves")).asInstanceOf(map(Long.class, String.class))
                .containsKey(customShelf.getId())
                .doesNotContainKey(defaultShelf.getId());
    }

    @Test
    void setupShelves_setsFirstDefaultShelfAsActiveShelf() {
        Shelf secondDefaultShelf = TestUtils.getShelf(profileLogin, Shelf.SHELF_CURRENTLY_READING, true);
        secondDefaultShelf.setId(3L);
        secondDefaultShelf.setBooks(Set.of());

        LoginDetails loginDetails = TestUtils.getLoginDetails(authLogin.getEmail(), authLogin.getHandle(), true);
        when(loginService.getLogin(profileLogin.getEmail())).thenReturn(profileLogin);
        when(loginService.getLogin(authLogin.getEmail())).thenReturn(authLogin);
        when(shelfService.getShelvesForCollection(profileLogin.getEmail()))
                .thenReturn(List.of(defaultShelf, secondDefaultShelf, customShelf));

        ModelMap model = new ModelMap();
        profileHelper.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

        assertThat(model.get("activeShelf")).isEqualTo(defaultShelf);
    }

    @Test
    void putReadingChallengeInModel_existsInDatabase_populateReadingChallengeInModel() {
        int currentYear = LocalDateTime.now().getYear();

        ReadingChallenge readingChallenge = new ReadingChallenge(profileLogin, currentYear, 10);
        ReadingChallengeId readingChallengeId = new ReadingChallengeId(profileLogin.getId(), currentYear);

        when(readingChallengeRepository.findById(readingChallengeId)).thenReturn(Optional.of(readingChallenge));

        ModelMap model = new ModelMap();
        profileHelper.putReadingChallengeInModel(profileLogin, model);

        assertThat(model.containsKey("readingChallenge")).isTrue();
        assertThat(model.get("readingChallenge")).isEqualTo(readingChallenge);
    }

    @Test
    void putReadingChallengeInModel_notFound_populateReadingChallengeInModel() {
        ModelMap model = new ModelMap();
        profileHelper.putReadingChallengeInModel(profileLogin, model);

        assertThat(model.containsKey("readingChallenge")).isTrue();
        assertThat(((ReadingChallenge) model.get("readingChallenge")).getBookCount()).isNull();
    }
}
