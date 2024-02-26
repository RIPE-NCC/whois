package net.ripe.db.whois.api.mail.bounce;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.dao.BouncedMailDao;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.mail.MailGatewaySmtp;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml", "classpath:applicationContext-api-test-use-mail-service.xml"}, inheritLocations = false)
@Tag("IntegrationTest")
public class BouncedMessageTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private MailGatewaySmtp mailGatewaySmtp;

    @Autowired
    private LoggerContext loggerContext;

    @Autowired
    private BouncedMailDao bouncedMailDao;

}
