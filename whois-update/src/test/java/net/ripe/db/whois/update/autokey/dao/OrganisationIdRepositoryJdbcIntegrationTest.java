package net.ripe.db.whois.update.autokey.dao;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import net.ripe.db.whois.update.domain.OrganisationId;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
@Transactional
public class OrganisationIdRepositoryJdbcIntegrationTest extends AbstractUpdateDaoIntegrationTest {
    @Autowired OrganisationIdRepository subject;

    @Test
    public void claimSpecified() {
        final boolean availableAndCreated = subject.claimSpecified(new OrganisationId("AK", 4, "RIPE"));
        assertThat(availableAndCreated, is(true));
        final int indexCreated = whoisTemplate.queryForObject("SELECT range_end FROM organisation_id WHERE space = 'AK' AND source = '-RIPE'", Integer.class);
        assertThat(indexCreated, is(4));

        final boolean availableAndUpdated = subject.claimSpecified(new OrganisationId("AK", 7, "RIPE"));
        assertThat(availableAndUpdated, is(true));
        final int indexUpdated = whoisTemplate.queryForObject("SELECT range_end FROM organisation_id WHERE space = 'AK' AND source = '-RIPE'", Integer.class);
        assertThat(indexUpdated, is(7));
    }

    @Test
    public void claimNextAvailableIndex_empty_database() {
        for (int i = 1; i < 10; i++) {
            assertThat(subject.claimNextAvailableIndex("AK", "RIPE").getIndex(), is(i));
            assertRows(1);
        }
    }

    private void assertRows(final int expectedRows) {
        final List<Map<String, Object>> list = whoisTemplate.queryForList("select * from organisation_id");
        assertThat(list, hasSize(expectedRows));

        for (final Map<String, Object> objectMap : list) {
            for (final Map.Entry<String, Object> entry : objectMap.entrySet()) {
                assertNotNull(entry.getKey(), entry.getValue());
            }

            final String source = objectMap.get("source").toString();
            if (source.length() > 0) {
                assertThat(source, startsWith("-"));
            }
        }
    }
}
