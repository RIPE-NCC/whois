package net.ripe.db.whois.update.autokey.dao;


import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import net.ripe.db.whois.update.domain.X509KeycertId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Tag("IntegrationTest")
@Transactional
public class X509RepositoryJdbcIntegrationTest extends AbstractUpdateDaoIntegrationTest {
    @Autowired X509Repository subject;

    @Test
    public void claimSpecified() {
        boolean availableAndClaimed = subject.claimSpecified(new X509KeycertId("ON", 10, "RIPE"));
        assertThat(availableAndClaimed, is(true));
        final int currentX509Id = whoisTemplate.queryForObject("SELECT keycert_id FROM x509", Integer.class);
        assertThat(currentX509Id, is(10));
    }

    @Test
    public void claimNextAvailable() {
        final X509KeycertId x509KeycertId = subject.claimNextAvailableIndex("ON", "RIPE");
        assertThat(x509KeycertId.getIndex(), is(1));

        final X509KeycertId next = subject.claimNextAvailableIndex("ON", "RIPE");
        assertThat(next.getIndex(), is(2));
    }
}
