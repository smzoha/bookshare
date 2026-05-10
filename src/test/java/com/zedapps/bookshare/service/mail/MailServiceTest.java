package com.zedapps.bookshare.service.mail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 10/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String TOKEN = "test-token-abc";

    @InjectMocks
    private MailService mailService;

    @Mock
    private Gmail gmail;

    @Mock
    private Gmail.Users gmailUsers;

    @Mock
    private Gmail.Users.Messages gmailMessages;

    @Mock
    private Gmail.Users.Messages.Send gmailSend;

    @BeforeEach
    void setup() throws IOException {
        ReflectionTestUtils.setField(mailService, "mailAddress", EMAIL);

        lenient().when(gmail.users()).thenReturn(gmailUsers);
        lenient().when(gmailUsers.messages()).thenReturn(gmailMessages);
        lenient().when(gmailMessages.send(eq("me"),
                any(Message.class))).thenReturn(gmailSend);
    }

    @Test
    void sendPasswordResetEmail_validEmailAndToken_callsGmailApiWithCorrectContent() throws MessagingException, IOException {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        mailService.sendPasswordResetEmail(EMAIL, TOKEN);

        verify(gmailMessages).send(eq("me"), messageCaptor.capture());
        verify(gmailSend).execute();

        String rawContent = new String(Base64.getDecoder().decode(messageCaptor.getValue().getRaw()));
        assertThat(rawContent).contains("Howdy Partner");
        assertThat(rawContent).contains("BookShare Team");
        assertThat(rawContent).contains("[BookShare] Password Reset Request");
    }

    @Test
    void sendPasswordResetEmail_validEmailAndToken_buildsExpectedResetUrl() throws Exception {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        mailService.sendPasswordResetEmail(EMAIL, TOKEN);

        verify(gmailMessages).send(eq("me"), messageCaptor.capture());

        String textContent = decodeTextContent(messageCaptor.getValue());
        assertThat(textContent).contains("http://localhost:6001/resetPassword?token=" + TOKEN);
    }

    @Test
    void sendPasswordResetEmail_gmailThrowsMessagingException_propagatesException() {
        try (MockedConstruction<MimeMessage> ignored = mockConstruction(MimeMessage.class,
                (mock, _) -> doThrow(new MessagingException("forced"))
                        .when(mock).writeTo(any(OutputStream.class)))) {

            assertThatThrownBy(() -> mailService.sendPasswordResetEmail(EMAIL, TOKEN))
                    .isInstanceOf(MessagingException.class);
        }
    }

    private String decodeTextContent(Message googleMessage) throws Exception {
        byte[] rawBytes = Base64.getDecoder().decode(googleMessage.getRaw());
        MimeMessage parsed = new MimeMessage(Session.getDefaultInstance(new Properties()),
                new ByteArrayInputStream(rawBytes));
        return extractText(parsed.getContent());
    }

    private String extractText(Object content) throws Exception {
        if (content instanceof Multipart multipart) {
            return extractText(multipart.getBodyPart(0).getContent());
        }
        return content.toString();
    }

    @Test
    void sendPasswordResetEmail_gmailThrowsIoException_propagatesException() throws IOException {
        when(gmailSend.execute()).thenThrow(new IOException("Gmail API failure"));

        assertThatThrownBy(() -> mailService.sendPasswordResetEmail(EMAIL, TOKEN))
                .isInstanceOf(IOException.class)
                .hasMessage("Gmail API failure");
    }
}
