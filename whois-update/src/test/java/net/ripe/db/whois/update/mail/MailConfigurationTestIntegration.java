package net.ripe.db.whois.update.mail;


import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("IntegrationTest")
public class MailConfigurationTestIntegration extends AbstractUpdateDaoIntegrationTest {
    @Autowired private MailConfiguration subject;

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void getSession() {
        assertNotNull(subject.getSession());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void from() throws Exception {
        assertNotNull(subject.getFrom());
    }
}
