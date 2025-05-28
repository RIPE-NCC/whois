package net.ripe.db.whois.update.mail;


import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class MailConfigurationTestIntegration extends AbstractUpdateDaoIntegrationTest {
    @Autowired private MailConfiguration subject;

    @Test
    public void getSession() {
        assertThat(subject.getSession(), not(nullValue()));
    }

    @Test
    public void from() throws Exception {
        assertThat(subject.getFrom(), not(nullValue()));
    }
}
