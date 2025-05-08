package net.ripe.db.whois.update.domain;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.common.Credentials.PasswordCredential;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public class CredentialsTest {
    @Test
    public void empty_credentials() {
        final Credentials subject = new Credentials();

        assertThat(subject.all(), hasSize(0));
        assertThat(subject.has(PgpCredential.class), is(false));
    }

    @Test
    public void multiple_credentials() {
        final Credential credential1 = Mockito.mock(Credential.class);
        final Credential credential2 = Mockito.mock(Credential.class);
        final PasswordCredential passwordCredential = Mockito.mock(PasswordCredential.class);

        final Credentials subject = new Credentials(Sets.newHashSet(credential1, credential2, passwordCredential));

        assertThat(subject.all(), hasSize(3));
        assertThat(subject.ofType(Credential.class), hasSize(3));
        assertThat(subject.ofType(PasswordCredential.class), hasSize(1));
        assertThat(subject.ofType(PgpCredential.class), hasSize(0));
        assertThat(subject.single(PgpCredential.class), is(nullValue()));
        assertThat(subject.single(PasswordCredential.class), is(passwordCredential));

        try {
            subject.single(Credential.class);
            fail("Expected error on multiple credentials of the same type");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("More than 1 credentials of type: interface net.ripe.db.whois.common.Credentials.Credential"));
        }

        assertThat(subject.has(PgpCredential.class), is(false));
    }

    @Test
    public void pgp_credentials() {
        final Credential credential1 = Mockito.mock(Credential.class);
        final Credential credential2 = Mockito.mock(Credential.class);
        final PgpCredential pgpCredential = Mockito.mock(PgpCredential.class);

        final Credentials subject = new Credentials(Sets.newHashSet(credential1, credential2, pgpCredential));

        assertThat(subject.has(PgpCredential.class), is(true));
    }
}
