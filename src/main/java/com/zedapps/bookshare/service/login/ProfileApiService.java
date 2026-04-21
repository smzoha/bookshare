package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.book.ReadingProgressDto;
import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.connection.ConnectionRepository;
import com.zedapps.bookshare.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author smzoha
 * @since 22/4/26
 **/
@Service
@RequiredArgsConstructor
public class ProfileApiService {

    private final LoginService loginService;
    private final ConnectionRepository connectionRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public LoginApiDto getLogin(String handle, boolean detailed) {
        Login login = loginService.getLoginByHandle(handle);

        String profilePictureUrl = Utils.getImageUrl(login.getProfilePicture());

        List<Connection> connectionList = connectionRepository.findConnectionsByPerson1(login);
        List<ReadingProgress> readingProgressList = profileService.getDistinctReadingProgressList(login);

        return new LoginApiDto(login.getFirstName(), login.getLastName(), login.getHandle(), login.getEmail(),
                login.getBio(), profilePictureUrl, login.getRole().name(), login.isActive(),
                detailed ? getShelfDtoList(login.getShelves(), login.getEmail()) : null,
                detailed ? getReadingProgressDtoList(readingProgressList, login.getEmail()) : null,
                detailed ? getConnectionList(connectionList) : null);
    }

    @Transactional
    public void performConnectionAction(LoginDetails loginDetails, ConnectionApiDto connectionApiDto) {
        Login authLogin = loginService.getLogin(loginDetails.getEmail());
        Login profileLogin = loginService.getLoginByHandle(connectionApiDto.handle());

        profileService.performConnectionAction(authLogin, profileLogin, connectionApiDto.action());
    }

    private List<ShelfDto> getShelfDtoList(Set<Shelf> shelves, String email) {
        return shelves.stream()
                .map(shelf -> new ShelfDto(shelf.getName(), email, shelf.getBooks().size(), shelf.isDefaultShelf()))
                .toList();
    }

    private List<ReadingProgressDto> getReadingProgressDtoList(List<ReadingProgress> readingProgressList, String email) {
        return readingProgressList.stream()
                .map(rp -> new ReadingProgressDto(rp.getBook().getTitle(), rp.getBook().getIsbn(),
                        email, rp.getPagesRead(), rp.getStartDate(), rp.getEndDate(), rp.isCompleted()))
                .toList();
    }

    private List<LoginApiDto> getConnectionList(List<Connection> connectionList) {
        return connectionList.stream()
                .map(connection -> {
                    Login friend = connection.getPerson2();

                    return new LoginApiDto(friend.getFirstName(), friend.getLastName(), friend.getHandle(), friend.getEmail(),
                            null, Utils.getImageUrl(friend.getProfilePicture()), null, friend.isActive(),
                            null, null, null);
                })
                .toList();
    }
}
