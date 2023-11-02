package net.ripe.db.whois.common.sso.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ValidateTokenResponseTest {

    @Test
    public void serialise() throws IOException, ClassNotFoundException  {
        final ValidateTokenResponse subject = new ValidateTokenResponse();
        subject.response = new ValidateTokenResponse.Response();
        subject.response.content = new ValidateTokenResponse.Content();
        subject.response.content.active = true;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                objectOutputStream.writeObject(subject);
            }
            try (final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
                final ValidateTokenResponse output = (ValidateTokenResponse)objectInputStream.readObject();
                assertThat(output.response.content.active, is(true));
            }
        }
    }

}
