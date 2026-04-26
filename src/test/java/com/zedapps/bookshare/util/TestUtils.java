package com.zedapps.bookshare.util;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.AuthProvider;
import com.zedapps.bookshare.enums.Role;

/**
 * @author smzoha
 * @since 26/4/26
 **/
public class TestUtils {

    public static Login getLogin(String email, String handle, boolean active) {
        Login login = new Login();
        login.setEmail(email);
        login.setHandle(handle);
        login.setRole(Role.USER);
        login.setFirstName("Test");
        login.setLastName("User");
        login.setActive(active);
        login.setAuthProvider(AuthProvider.LOCAL);

        return login;
    }
}
