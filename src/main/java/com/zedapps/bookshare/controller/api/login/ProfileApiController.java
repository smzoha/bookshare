package com.zedapps.bookshare.controller.api.login;

import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeRequest;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.ProfileApiService;
import com.zedapps.bookshare.service.login.ReadingChallengeApiService;
import com.zedapps.bookshare.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author smzoha
 * @since 22/4/26
 **/
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileApiService profileApiService;
    private final ReadingChallengeApiService readingChallengeApiService;

    @GetMapping("/{handle}")
    public ResponseEntity<LoginApiDto> getLogin(@PathVariable String handle,
                                                @RequestParam(defaultValue = "false") boolean detailed) {

        LoginApiDto loginApiDto = profileApiService.getLogin(handle, detailed);

        return ResponseEntity.ok().body(loginApiDto);
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectionActions(@RequestBody ConnectionApiDto connectionApiDto,
                                               @AuthenticationPrincipal LoginDetails loginDetails) {

        profileApiService.performConnectionAction(loginDetails, connectionApiDto);

        return ResponseEntity.ok().body(connectionApiDto);
    }

    @GetMapping("/readingChallenge")
    public ResponseEntity<ReadingChallengeDto> getReadingChallenge(@AuthenticationPrincipal LoginDetails loginDetails) {
        Optional<ReadingChallengeDto> readingChallengeDtoOptional = readingChallengeApiService.getReadingChallenge(loginDetails.getEmail());

        return readingChallengeDtoOptional
                .map(readingChallengeDto -> ResponseEntity.ok().body(readingChallengeDto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/readingChallenge")
    public ResponseEntity<?> saveReadingChallenge(@Valid @RequestBody ReadingChallengeRequest request,
                                                  Errors errors,
                                                  @AuthenticationPrincipal LoginDetails loginDetails) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        ReadingChallengeDto readingChallengeDto = readingChallengeApiService.saveReadingChallenge(request, loginDetails);

        return ResponseEntity.ok().body(readingChallengeDto);
    }
}
