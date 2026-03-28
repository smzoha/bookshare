package com.zedapps.bookshare.service;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.AuthProvider;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Service
@RequiredArgsConstructor
public class LoginDetailOidcService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final LoginRepository loginRepository;
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();

    @Override
    public OidcUser loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = defaultOAuth2UserService.loadUser(userRequest);

        Map<String, Object> attributes = user.getAttributes();
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String providerId = (String) attributes.get("sub");

        Optional<Login> persistedLogin = loginRepository.findByEmail(email);
        Login login = persistedLogin.orElse(null);

        if (persistedLogin.isPresent()) {
            if (!persistedLogin.get().isActive()) {
                throw new OAuth2AuthenticationException("Login is inactive!");
            }
        } else {
            login = new Login();
            login.setEmail(email);
            login.setFirstName(firstName);
            login.setLastName(lastName);
            login.setHandle(firstName + "." + lastName + "." + UUID.randomUUID().toString().substring(0, 4));
            login.setRole(Role.USER);
            login.setAuthProvider(AuthProvider.GOOGLE);
            login.setProviderId(providerId);
            login.setActive(true);

            login = loginRepository.save(login);
        }

        return new LoginDetails(login.getEmail(), login.getFirstName(), login.getLastName(), login.getHandle(),
                List.of(new SimpleGrantedAuthority(login.getRole().name())),
                null, null);
    }
}
