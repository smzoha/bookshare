package com.zedapps.bookshare.controller.api.dashboard;

import com.zedapps.bookshare.dto.api.dashboard.ReadingStatsDto;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.dashboard.ReadingStatsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author smzoha
 * @since 25/6/26
 **/
@RestController
@RequestMapping("/api/v1/readingStats")
@RequiredArgsConstructor
public class ReadingStatsApiController {

    private final ReadingStatsApiService readingStatsApiService;

    @GetMapping("/{year}")
    public ResponseEntity<ReadingStatsDto> getReadingStats(@AuthenticationPrincipal LoginDetails loginDetails,
                                                           @PathVariable Integer year) {

        ReadingStatsDto readingStatsDto = readingStatsApiService.getReadingStatsDto(loginDetails, year);

        return ResponseEntity.ok(readingStatsDto);
    }
}
