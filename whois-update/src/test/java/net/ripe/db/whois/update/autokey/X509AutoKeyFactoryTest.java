package net.ripe.db.whois.update.autokey;


import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.dao.X509Repository;
import net.ripe.db.whois.update.domain.X509KeycertId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class X509AutoKeyFactoryTest {

    @Mock X509Repository x509Repository;

    @InjectMocks X509AutoKeyFactory subject;

    @BeforeEach
    public void setup() {
        subject = new X509AutoKeyFactory(x509Repository, "TEST");
    }

    @Test
    public void attributeType() {
        assertThat(subject.getAttributeType(), is(AttributeType.KEY_CERT));
    }

    @Test
    public void correct_keyPlaceHolder() {
        assertThat(subject.isKeyPlaceHolder("AUTO-100"), is(true));
    }

    @Test
    public void incorrect_keyPlaceHolder() {
        assertThat(subject.isKeyPlaceHolder("AUTO-100NL"), is(false));
        assertThat(subject.isKeyPlaceHolder("AUTO-"), is(false));
        assertThat(subject.isKeyPlaceHolder("AUTO"), is(false));
        assertThat(subject.isKeyPlaceHolder("AUTO-100-NL"), is(false));
    }

    @Test
    public void claim_not_supported() {
        assertThrows(ClaimException.class, () -> {
            subject.claim("irrelevant here");
        });
    }

    @Test
    public void generate_invalid_placeHolder() {
        assertThrows(IllegalArgumentException.class, () -> {
            subject.generate("AUTO", RpslObject.parse("key-cert: AUTO"));
        });
    }

    @Test
    public void generate_correct() {
        when(x509Repository.claimNextAvailableIndex("X509", "TEST")).thenReturn(new X509KeycertId("X509", 2, "TEST"));

        final X509KeycertId generated = subject.generate("AuTO-1", RpslObject.parse("kEy-Cert: auto\nremarks: optional"));
        assertThat(generated.toString(), is("X509-2"));
    }
}
