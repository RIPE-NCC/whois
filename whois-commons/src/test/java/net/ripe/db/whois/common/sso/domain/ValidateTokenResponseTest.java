package net.ripe.db.whois.common.sso.domain;

import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @Test
    public void scale() {
        System.out.println(divide(100,10));
    }

    private BigDecimal divide(final long val1, final long val2) {
        return BigDecimal.valueOf(val1)
                .divide(BigDecimal.valueOf(val2), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

    }

}
