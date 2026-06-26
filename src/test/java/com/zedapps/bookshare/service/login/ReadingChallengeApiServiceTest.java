package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.login.ReadingChallengeDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.entity.login.ReadingChallengeId;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 27/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class ReadingChallengeApiServiceTest {

    @Mock
    private ReadingChallengeRepository readingChallengeRepository;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private ReadingChallengeApiService readingChallengeApiService;

    private Login login;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);
    }

    @Test
    void getReadingChallenge_existingChallenge_returnsMappedDto() {
        int currentYear = LocalDate.now().getYear();
        ReadingChallenge readingChallenge = TestUtils.getReadingChallenge(login, currentYear, 10);

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(readingChallengeRepository.findById(new ReadingChallengeId(login.getId(), currentYear)))
                .thenReturn(Optional.of(readingChallenge));

        Optional<ReadingChallengeDto> dtoOptional = readingChallengeApiService.getReadingChallenge(login.getEmail());

        assertTrue(dtoOptional.isPresent());

        ReadingChallengeDto dto = dtoOptional.get();
        assertEquals(login.getEmail(), dto.email());
        assertEquals(currentYear, dto.year());
        assertEquals(10, dto.bookCount());

        verify(readingChallengeRepository).findById(new ReadingChallengeId(login.getId(), currentYear));
    }

    @Test
    void getReadingChallenge_noChallenge_returnsEmptyOptional() {
        int currentYear = LocalDate.now().getYear();

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(readingChallengeRepository.findById(new ReadingChallengeId(login.getId(), currentYear)))
                .thenReturn(Optional.empty());

        Optional<ReadingChallengeDto> result = readingChallengeApiService.getReadingChallenge(login.getEmail());

        assertTrue(result.isEmpty());
    }

    @Test
    void saveReadingChallenge_persistsRequestAndReturnsDto() {
        LoginDetails loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), true);
        ReadingChallengeRequest request = new ReadingChallengeRequest(2026, 50);

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(readingChallengeRepository.save(any(ReadingChallenge.class)))
                .thenReturn(TestUtils.getReadingChallenge(login, request.year(), request.bookCount()));

        ReadingChallengeDto dto = readingChallengeApiService.saveReadingChallenge(request, loginDetails);

        assertEquals(login.getEmail(), dto.email());
        assertEquals(2026, dto.year());
        assertEquals(50, dto.bookCount());

        ArgumentCaptor<ReadingChallenge> captor = ArgumentCaptor.forClass(ReadingChallenge.class);
        verify(readingChallengeRepository).save(captor.capture());

        ReadingChallenge savedChallenge = captor.getValue();
        assertEquals(login, savedChallenge.getLogin());
        assertEquals(request.year(), savedChallenge.getYear());
        assertEquals(request.bookCount(), savedChallenge.getBookCount());
    }
}
