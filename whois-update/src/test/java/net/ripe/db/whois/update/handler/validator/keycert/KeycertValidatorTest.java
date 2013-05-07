package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.X509AutoKeyFactory;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeycertValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock X509AutoKeyFactory x509AutoKeyFactory;
    @Mock KeyWrapperFactory keyWrapperFactory;
    @InjectMocks KeycertValidator subject;
    List<Message> messages;

    @Before
    public void setup() {
        messages = Lists.newArrayList();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                messages.add((Message) args[2]);
                return null;
            }
        }).when(updateContext).addMessage(any(UpdateContainer.class), any(RpslAttribute.class), any(Message.class));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.KEY_CERT));
    }


    @Test
    public void auto_1_with_x509() throws Exception {
        RpslObject object = RpslObject.parse(getResource("keycerts/AUTO-1-X509.TXT"));
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);
        when(keyWrapperFactory.createKeyWrapper(object, update, updateContext)).thenReturn(X509CertificateWrapper.parse(object));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void auto_3_with_pgp() throws Exception {
        RpslObject object = RpslObject.parse("" +
                "key-cert:     AUTO-3\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       Version: GnuPG v1.4.11 (Darwin)\n" +
                "certif:\n" +
                "certif:       mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV\n" +
                "certif:       4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF\n" +
                "certif:       7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz\n" +
                "certif:       RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0\n" +
                "certif:       VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt\n" +
                "certif:       Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz\n" +
                "certif:       aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL\n" +
                "certif:       BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi\n" +
                "certif:       Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX\n" +
                "certif:       /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV\n" +
                "certif:       gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK\n" +
                "certif:       yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC\n" +
                "certif:       H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP\n" +
                "certif:       ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1\n" +
                "certif:       qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl\n" +
                "certif:       sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN\n" +
                "certif:       LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e\n" +
                "certif:       27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah\n" +
                "certif:       P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ\n" +
                "certif:       EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml\n" +
                "certif:       xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q\n" +
                "certif:       mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM\n" +
                "certif:       QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh\n" +
                "certif:       4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj\n" +
                "certif:       kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=\n" +
                "certif:       =XRho\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       noreply@ripe.net\n" +
                "changed:      noreply@ripe.net 20120213\n" +
                "source:       TEST\n");
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);
        when(x509AutoKeyFactory.isKeyPlaceHolder("AUTO-3")).thenReturn(true);
        when(keyWrapperFactory.createKeyWrapper(object, update, updateContext)).thenReturn(PgpPublicKeyWrapper.parse(object));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, object.getAttributes().get(0), UpdateMessages.autokeyForX509KeyCertsOnly());
    }

    @Test
    public void one_public_key_with_multiple_sub_keys() throws Exception {
        RpslObject object = RpslObject.parse(getResource("keycerts/PGPKEY-MULTIPLE-SUBKEYS.TXT"));
        when(update.getUpdatedObject()).thenReturn(object);
        when(keyWrapperFactory.createKeyWrapper(object, update, updateContext)).thenReturn(PgpPublicKeyWrapper.parse(object));

        subject.validate(update, updateContext);

        assertThat(messages.size(), is(0));
    }


    private String getResource(final String resourceName) throws IOException {
        return IOUtils.toString(new ClassPathResource(resourceName).getInputStream());
    }
}
