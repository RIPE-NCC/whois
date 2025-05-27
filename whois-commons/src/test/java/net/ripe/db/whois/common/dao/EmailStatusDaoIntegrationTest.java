package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.mail.EmailStatusType;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@Tag("IntegrationTest")
public class EmailStatusDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Autowired
    private EmailStatusDao emailStatusDao;

    @Test
    public void add_undeliverable_then_undeliverable_created() {
        emailStatusDao.createEmailStatus("undeliverable@ripe.net", EmailStatusType.UNDELIVERABLE);
        final Set<EmailStatus> emailStatuses = emailStatusDao.getEmailStatus(Set.of("undeliverable@ripe.net"));
        assertThat(emailStatuses.size(), is(1));
    }
}
