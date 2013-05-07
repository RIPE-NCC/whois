package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PasswordCredentialValidatorTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private LoggerContext loggerContext;
    @InjectMocks private PasswordCredentialValidator subject;

    @Test
    public void authenticatePassword() {
        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(true));
        assertThat(authenticate("emptypassword", "md5-pw $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(true));
        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # comment"), is(true));
        assertThat(authenticate("emptypassword", "MD5-PW $1$ID$T6JBFWOLNhasGbO3Jkj37."), is(true));

        assertThat(authenticate("EmptyPassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(false));
        assertThat(authenticate("", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(false));
        assertThat(authenticate("emptypassword", "$1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(false));
        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2Xnz$p5ddbI7SXq4z4yNrObFS/0"), is(false));
        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/"), is(false));
        assertThat(authenticate("emptypassword", "MD5-PW $1$$"), is(false));
        assertThat(authenticate("C'était", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(false));
        assertThat(authenticate("password", "MD5-PW $1$ID$LseZOi4AIPMb6gXOp5QpQ0"), is(true));

        assertThat(authenticate("", ""), is(false));
    }

    private boolean authenticate(final String offered, final String known) {
        return subject.hasValidCredential(update, updateContext, PasswordCredential.forPasswords(offered), new PasswordCredential(known));
    }

    @Test
    public void supports() {
        assertEquals(PasswordCredential.class, subject.getSupportedCredentials());
    }
}