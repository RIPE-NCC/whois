package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

@org.junit.jupiter.api.Tag("IntegrationTest")
public class MailConfigurationTestIntegration extends AbstractUpdateDaoIntegrationTest {
    @Autowired private MailConfiguration subject;

    @Test
    public void getSession() {
        assertNotNull(subject.getSession());
    }

    @Test
    public void from() throws Exception {
        assertNotNull(subject.getFrom());
    }
}
