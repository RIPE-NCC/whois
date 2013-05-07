package net.ripe.db.whois.update.autokey.dao;


import net.ripe.db.whois.update.dao.AbstractDaoTest;
import net.ripe.db.whois.update.domain.X509KeycertId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Transactional
public class X509RepositoryJdbcTest extends AbstractDaoTest {
    @Autowired X509Repository subject;

    @Test
    public void claimSpecified() {
        boolean availableAndClaimed = subject.claimSpecified(new X509KeycertId("ON", 10, "RIPE"));
        assertThat(availableAndClaimed, is(true));
        final int currentX509Id = whoisTemplate.queryForInt("SELECT keycert_id FROM x509");
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
