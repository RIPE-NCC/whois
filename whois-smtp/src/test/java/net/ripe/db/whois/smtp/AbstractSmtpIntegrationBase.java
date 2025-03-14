package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-smtp-test.xml"})
public abstract class AbstractSmtpIntegrationBase extends AbstractDatabaseHelperIntegrationTest {
    @Autowired protected SmtpServer smtpServer;
}
