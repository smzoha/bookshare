package com.zedapps.bookshare.controller.dashboard;

import com.zedapps.bookshare.helper.ReadingStatsHelper;
import com.zedapps.bookshare.service.auth.LoginDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author smzoha
 * @since 22/6/26
 **/
@Controller
@RequestMapping("/readingStats")
@RequiredArgsConstructor
public class ReadingStatsController {

    private final ReadingStatsHelper readingStatsHelper;

    @GetMapping
    public String getReadingStats(@AuthenticationPrincipal LoginDetails loginDetails,
                                  @RequestParam(required = false) Integer year,
                                  ModelMap model) {

        readingStatsHelper.setupReadingStatsReferenceData(loginDetails, year, model);

        return "app/login/readingStats";
    }
}
