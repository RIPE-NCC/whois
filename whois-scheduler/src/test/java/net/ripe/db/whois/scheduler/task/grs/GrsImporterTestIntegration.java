package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext
public class GrsImporterTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired ResourceDataDao resourceDataDao;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DatabaseHelper.addGrsDatabases("TEST-GRS");
    }

    @Before
    public void setUp() throws Exception {
        queryServer.start();
    }

    @Test
    public void incremental_insert_autnum() throws Exception {
        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS105"), is(false));

        resourceDataDao.append("test", createAuthoritativeResource(105, 105));
        authoritativeResourceData.everyMinuteRefreshAuthoritativeResourceCache();

        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS105"), is(true));
    }

    @Test
    public void incremental_remove_autnum() throws Exception {
        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS102"), is(true));

        resourceDataDao.remove("test", createAuthoritativeResource(102, 102));
        authoritativeResourceData.everyMinuteRefreshAuthoritativeResourceCache();

        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS105"), is(false));
    }

    // helper methods

    private boolean isMaintainedInRirSpace(final ObjectType objectType, final String pkey) {
        return authoritativeResourceData.getAuthoritativeResource(CIString.ciString("TEST")).isMaintainedInRirSpace(objectType, CIString.ciString(pkey));
    }

    private AuthoritativeResource createAuthoritativeResource(final long start, final long end) {
        final SortedRangeSet<Asn, AsnRange> asns = new SortedRangeSet<>();
        asns.add(AsnRange.from(start).to(end));
        return new AuthoritativeResource(asns, new SortedRangeSet<Ipv4, Ipv4Range>(), new SortedRangeSet<Ipv6, Ipv6Range>());
    }

}
