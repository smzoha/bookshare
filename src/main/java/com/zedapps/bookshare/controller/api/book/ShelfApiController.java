package com.zedapps.bookshare.controller.api.book;

import com.zedapps.bookshare.dto.api.shelf.ShelfCreateDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.login.ShelfApiService;
import com.zedapps.bookshare.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 20/4/26
 **/
@RestController
@RequestMapping("/api/v1/shelf")
@RequiredArgsConstructor
public class ShelfApiController {

    private final ShelfApiService shelfApiService;

    @GetMapping
    public ResponseEntity<List<ShelfDto>> getShelves(@AuthenticationPrincipal LoginDetails loginDetails) {
        return ResponseEntity.ok().body(shelfApiService.getShelfDtoList(loginDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShelf(@PathVariable Long id, @AuthenticationPrincipal LoginDetails loginDetails) {
        Shelf shelf = shelfApiService.getShelf(id);

        if (!Objects.equals(shelf.getUser().getEmail(), loginDetails.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().body(shelfApiService.getShelfDetailDto(shelf));
    }

    @PostMapping
    public ResponseEntity<?> saveShelf(@Valid @RequestBody ShelfCreateDto shelfCreateDto,
                                       Errors errors,
                                       @AuthenticationPrincipal LoginDetails loginDetails) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(Utils.getErrorResponseDto(errors));
        }

        ShelfDto shelfDto = shelfApiService.saveShelf(shelfCreateDto, loginDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(shelfDto);
    }
}
