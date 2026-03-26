package com.zedapps.bookshare.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author smzoha
 * @since 26/3/26
 **/
@Configuration
public class GmailConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${app.gmail.client.id}")
    private String clientId;

    @Value("${app.gmail.client.secret}")
    private String clientSecret;

    @Value("${app.gmail.refresh.token}")
    private String refreshToken;

    @Bean
    public Gmail gmail() throws Exception {
        UserCredentials userCredentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        userCredentials.refreshIfExpired();

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(userCredentials))
                .setApplicationName("bookshare")
                .build();
    }
}
