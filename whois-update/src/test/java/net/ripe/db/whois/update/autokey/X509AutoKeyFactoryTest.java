package net.ripe.db.whois.update.autokey;


import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.dao.X509Repository;
import net.ripe.db.whois.update.domain.X509KeycertId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X509AutoKeyFactoryTest {

    @Mock X509Repository x509Repository;

    @InjectMocks X509AutoKeyFactory subject;

    @Before
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

    @Test(expected = ClaimException.class)
    public void claim_not_supported() throws ClaimException {
        subject.claim("irrelevant here");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generate_invalid_placeHolder() {
        subject.generate("AUTO", RpslObject.parse("key-cert: AUTO"));
    }

    @Test
    public void generate_correct() {
        when(x509Repository.claimNextAvailableIndex("X509", "TEST")).thenReturn(new X509KeycertId("X509", 2, "TEST"));

        final X509KeycertId generated = subject.generate("AuTO-1", RpslObject.parse("kEy-Cert: auto\nremarks: optional"));
        assertThat(generated.toString(), is("X509-2"));
    }
}
