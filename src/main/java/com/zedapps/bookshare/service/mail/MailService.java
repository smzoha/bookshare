package com.zedapps.bookshare.service.mail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final Gmail gmail;

    @Value("${spring.mail.username}")
    private String mailAddress;

    private static final String PASSWORD_RESET_MAIL_BODY = """
            Howdy Partner,%n\
            %n\
            Looks like you've misplaced the keys to your account.%n\
            %n\
            Use the link below to set a new password:%n\
            %s%n\
            %n\
            This link will expire in 10 minutes, so be sure to use it soon.%n\
            %n\
            If you didn't request this, you can safely ignore this email.%n\
            %n\
            We'll be glad to have you back in the saddle with a book in hand.%n\
            %n\
            — BookShare Team%n\
            """;

    @Async
    public void sendPasswordResetEmail(String email, String token) throws MessagingException, IOException {
        String passwordResetUrl = UriComponentsBuilder.fromUriString("http://localhost:6001/resetPassword")
                .queryParam("token", token)
                .build()
                .toUriString();

        String content = String.format(PASSWORD_RESET_MAIL_BODY, passwordResetUrl);

        sendMail(email, "[BookShare] Password Reset Request", content);
    }

    private void sendMail(String to, String subject, String content) throws MessagingException, IOException {
        MimeMessage mimeMessage = createMimeMessage(to, subject, content);
        Message message = createMessage(mimeMessage);

        gmail.users().messages().send("me", message).execute();
        log.info("Sent mail to {}", to);
    }

    private MimeMessage createMimeMessage(String to, String subject, String content) throws MessagingException {
        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(mailAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, false);

        return mimeMessage;
    }

    private Message createMessage(MimeMessage mimeMessage) throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mimeMessage.writeTo(outputStream);

        byte[] emailBytes = outputStream.toByteArray();
        String encodedEmail = Base64.getEncoder().encodeToString(emailBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        return message;
    }
}
