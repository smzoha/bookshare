package com.zedapps.bookshare.controller.api.login;

import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.service.login.ProfileApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author smzoha
 * @since 22/4/26
 **/
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileApiService profileApiService;

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
}
