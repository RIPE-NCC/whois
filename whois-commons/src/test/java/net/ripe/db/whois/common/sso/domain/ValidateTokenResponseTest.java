package net.ripe.db.whois.common.sso.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ValidateTokenResponseTest {

    @Test
    public void serialise() throws IOException, ClassNotFoundException  {
        final ValidateTokenResponse subject = new ValidateTokenResponse();
        subject.response = new ValidateTokenResponse.Response();
        subject.response.content = new ValidateTokenResponse.Content();
        subject.response.content.active = true;
        try (final ByteArrayOutputStream byteArrayOutputStream  = serialise(subject)) {
            final ValidateTokenResponse output = deserialise(byteArrayOutputStream);
            assertThat(output.response.content.active, is(true));
        }
    }

    private static ByteArrayOutputStream serialise(final ValidateTokenResponse subject) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(subject);
        }
        return byteArrayOutputStream;
    }

    private ValidateTokenResponse deserialise(final ByteArrayOutputStream byteArrayOutputStream) throws IOException, ClassNotFoundException {
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            return (ValidateTokenResponse)objectInputStream.readObject();
        }
    }
}
