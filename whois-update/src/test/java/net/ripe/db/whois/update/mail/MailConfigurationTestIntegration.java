package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractDaoTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class MailConfigurationTestIntegration extends AbstractDaoTest {
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
