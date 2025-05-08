package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordCredentialValidatorTest {
    @Mock private PreparedUpdate preparedUpdate;
    @Mock private Update update;
    @Mock private UpdateContext updateContext;
    @Mock private LoggerContext loggerContext;
    @InjectMocks private PasswordCredentialValidator subject;

    @Test
    public void authenticatePassword() {
        when(preparedUpdate.getUpdate()).thenReturn(update);

        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(true));
        assertThat(authenticate("emptypassword", "md5-pw $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0"), is(true));
        assertThat(authenticate("emptypassword", "MD5-PW $1$ID$T6JBFWOLNhasGbO3Jkj37."), is(true));

        assertThat(authenticate("emptypassword", "MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # comment"), is(false));
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
        return subject.hasValidCredential(preparedUpdate, updateContext, Collections.singleton(new PasswordCredential(offered)), new PasswordCredential(known), null);
    }

    @Test
    public void supports() {
        assertThat(subject.getSupportedCredentials(), equalTo(PasswordCredential.class));
    }

    @Test
    public void tostring() {
        assertThat(new PasswordCredential("secret").toString(), is("PasswordCredential"));
    }

}
