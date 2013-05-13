package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UpdateTest {

    private final String content = "mntner: mntner\nsource: TEST\n";
    private final RpslObject rpslObject = RpslObject.parse(content);

    @Test
    public void is_signed_with_one_pgp_credential() {
        final Paragraph paragraph = new Paragraph(content, new Credentials(Sets.newHashSet(PgpCredential.createKnownCredential("PGPKEY-AAAAAAAA"))));

        Update subject = new Update(paragraph, Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);

        assertThat(subject.isSigned(), is(true));
    }

    @Test
    public void is_signed_with_one_x509_credential() {
        final Paragraph paragraph = new Paragraph(content, new Credentials(Sets.newHashSet(X509Credential.createKnownCredential("X509-1"))));

        Update subject = new Update(paragraph, Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);

        assertThat(subject.isSigned(), is(true));
    }

    @Test
    public void is_not_signed_with_one_password_credential() {
        final Paragraph paragraph = new Paragraph(content, new Credentials(Sets.newHashSet(PasswordCredential.forPasswords("password"))));

        Update subject = new Update(paragraph, Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);

        assertThat(subject.isSigned(), is(false));
    }

    @Test
    public void is_override() {
        final Paragraph paragraph = new Paragraph(content, new Credentials(Sets.newHashSet(OverrideCredential.parse("username,password"))));

        Update subject = new Update(paragraph, Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);

        assertThat(subject.isOverride(), is(true));
    }

    @Test
    public void is_not_override() {
        final Paragraph paragraph = new Paragraph(content, new Credentials(Sets.newHashSet(PasswordCredential.forPasswords("password"))));

        Update subject = new Update(paragraph, Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);

        assertThat(subject.isOverride(), is(false));
    }

}
