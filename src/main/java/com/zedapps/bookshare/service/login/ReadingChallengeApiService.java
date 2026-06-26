package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.login.ReadingChallengeDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.entity.login.ReadingChallengeId;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * @author smzoha
 * @since 25/6/26
 **/
@Service
@RequiredArgsConstructor
public class ReadingChallengeApiService {

    private final ReadingChallengeRepository readingChallengeRepository;
    private final LoginService loginService;

    public Optional<ReadingChallengeDto> getReadingChallenge(String email) {
        int year = LocalDate.now().getYear();

        Login login = loginService.getLogin(email);
        ReadingChallengeId readingChallengeId = new ReadingChallengeId(login.getId(), year);

        Optional<ReadingChallenge> readingChallengeOptional = readingChallengeRepository.findById(readingChallengeId);

        return readingChallengeOptional
                .map(readingChallenge -> new ReadingChallengeDto(readingChallenge.getLogin().getEmail(),
                        readingChallenge.getYear(),
                        readingChallenge.getBookCount()));
    }

    public ReadingChallengeDto saveReadingChallenge(ReadingChallengeRequest request, LoginDetails loginDetails) {
        Login login = loginService.getLogin(loginDetails.getEmail());
        ReadingChallenge readingChallenge = new ReadingChallenge(login, request.year(), request.bookCount());

        readingChallenge = readingChallengeRepository.save(readingChallenge);

        return new ReadingChallengeDto(readingChallenge.getLogin().getEmail(),
                readingChallenge.getYear(), readingChallenge.getBookCount());
    }
}
