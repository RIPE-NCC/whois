package net.ripe.db.whois.update.mail;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MailMessageLogCallbackTest {
    @Mock Message message;
    @Mock OutputStream outputStream;

    @Test
    public void log() throws IOException {
        final MailMessageLogCallback subject = new MailMessageLogCallback(message);
        subject.log(outputStream);
        verify(outputStream).write("".getBytes());
    }

    @Test
    public void log_never_throws_exception() throws IOException {
        final MailMessageLogCallback subject = new MailMessageLogCallback(message);

        subject.log(outputStream);

        verify(outputStream).write("".getBytes());
    }
}
