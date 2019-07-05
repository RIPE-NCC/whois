package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JdbcAccessControlListDaoTest extends AbstractQueryDaoTest {

    @Autowired AccessControlListDao subject;
    private InetAddress inetAddress1;
    private InetAddress inetAddress2;

    @Before
    public void setup() throws UnknownHostException {
        inetAddress1 = InetAddress.getByName("128.0.0.1");
        inetAddress2 = InetAddress.getByName("128.0.0.2");
    }


    @Test
    public void save_acl_event() {
        final LocalDate blockTime = new LocalDate();
        final int limit = 10;

        subject.saveAclEvent(Ipv4Resource.parse("128.0.0.1/32"), blockTime, limit, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<Map<String, Object>> aclEvents = databaseHelper.listAclEvents();
        assertThat(aclEvents, hasSize(1));

        final Map<String, Object> entry = aclEvents.get(0);
        assertThat(entry.get("prefix"), is("128.0.0.1/32"));
        assertThat(new LocalDate(entry.get("event_time")), is(blockTime));
        assertThat(entry.get("daily_limit"), is(limit));
        assertThat(entry.get("event_type"), is(BlockEvent.Type.BLOCK_TEMPORARY.name()));
    }


    @Test
    public void save_acl_event_ipv6_canonical() throws Exception {
        final LocalDate blockTime = new LocalDate();
        final int limit = 10;

        subject.saveAclEvent(Ipv6Resource.parse("2a03:f480:1:c:0:0:0:0/64"), blockTime, limit, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<Map<String, Object>> aclEvents = databaseHelper.listAclEvents();
        assertThat(aclEvents, hasSize(1));

        final Map<String, Object> entry = aclEvents.get(0);
        assertThat(entry.get("prefix"), is("2a03:f480:1:c::/64"));
        assertThat(new LocalDate(entry.get("event_time")), is(blockTime));
        assertThat(entry.get("daily_limit"), is(limit));
        assertThat(entry.get("event_type"), is(BlockEvent.Type.BLOCK_TEMPORARY.name()));

    }


    @Test
    public void save_acl_event_twice() {
        final LocalDate blockTime = new LocalDate();
        final int limit = 10;

        subject.saveAclEvent(Ipv4Resource.parse("128.0.0.1/32"), blockTime, limit, BlockEvent.Type.BLOCK_TEMPORARY);
        subject.saveAclEvent(Ipv4Resource.parse("128.0.0.1/32"), blockTime, limit, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<Map<String, Object>> aclEvents = databaseHelper.listAclEvents();
        assertThat(aclEvents, hasSize(1));
    }

    @Test
    public void get_acl_events_empty() {
        final List<BlockEvents> temporaryBlocks = subject.getTemporaryBlocks(new LocalDate());

        assertThat(temporaryBlocks, is(empty()));
    }

    @Test
    public void get_acl_events_all() {
        final LocalDate blockTime = saveAclEvent(inetAddress1, 0, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 1, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 2, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<BlockEvents> temporaryBlocks = subject.getTemporaryBlocks(blockTime);

        assertThat(temporaryBlocks, hasSize(1));
        assertThat(temporaryBlocks.get(0).getPrefix(), is("128.0.0.1/32"));
        assertThat(temporaryBlocks.get(0).getTemporaryBlockCount(), is(3));
    }

    @Test
    public void get_acl_events_after_time() {
        saveAclEvent(inetAddress1, 0, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 1, BlockEvent.Type.BLOCK_TEMPORARY);

        final LocalDate blockTime = saveAclEvent(inetAddress1, 2, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<BlockEvents> temporaryBlocks = subject.getTemporaryBlocks(blockTime);

        assertThat(temporaryBlocks, hasSize(1));
        assertThat(temporaryBlocks.get(0).getPrefix(), is("128.0.0.1/32"));
        assertThat(temporaryBlocks.get(0).getTemporaryBlockCount(), is(1));
    }

    @Test
    public void getacl_events_after_unblock() {
        saveAclEvent(inetAddress1, 0, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 1, BlockEvent.Type.BLOCK_TEMPORARY);

        final LocalDate blockTime = saveAclEvent(inetAddress1, 2, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 3, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 4, BlockEvent.Type.BLOCK_TEMPORARY);

        saveAclEvent(inetAddress1, 5, BlockEvent.Type.UNBLOCK);
        saveAclEvent(inetAddress1, 6, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<BlockEvents> temporaryBlocks = subject.getTemporaryBlocks(blockTime);

        assertThat(temporaryBlocks, hasSize(1));
        assertThat(temporaryBlocks.get(0).getPrefix(), is("128.0.0.1/32"));
        assertThat(temporaryBlocks.get(0).getTemporaryBlockCount(), is(1));
    }

    @Test
    public void get_acl_events_multiple_inetaddresses() {
        final LocalDate blockTime = saveAclEvent(inetAddress1, 0, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress1, 1, BlockEvent.Type.BLOCK_TEMPORARY);

        saveAclEvent(inetAddress2, 1, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress2, 2, BlockEvent.Type.BLOCK_TEMPORARY);
        saveAclEvent(inetAddress2, 3, BlockEvent.Type.BLOCK_TEMPORARY);

        saveAclEvent(inetAddress1, 3, BlockEvent.Type.BLOCK_TEMPORARY);

        saveAclEvent(inetAddress2, 4, BlockEvent.Type.BLOCK_TEMPORARY);

        final List<BlockEvents> temporaryBlocks = subject.getTemporaryBlocks(blockTime);

        assertThat(temporaryBlocks, hasSize(2));

        for (BlockEvents blockEvents : temporaryBlocks) {
            if (blockEvents.getPrefix().equals("128.0.0.1/32")) {
                assertThat(blockEvents.getTemporaryBlockCount(), is(3));
            } else if (blockEvents.getPrefix().equals("128.0.0.2/32")) {
                assertThat(blockEvents.getTemporaryBlockCount(), is(4));
            } else {
                fail("Unexpected prefix: " + blockEvents.getPrefix());
            }
        }
    }

    private LocalDate saveAclEvent(InetAddress inetAddress, int day, BlockEvent.Type type) {
        final LocalDate blockTime = new LocalDate().minusYears(1).plusDays(day);
        subject.saveAclEvent(IpInterval.asIpInterval(inetAddress), blockTime, 1, type);
        return blockTime;
    }

    @Test
    public void loadIpDenied() {
        List<IpResourceEntry<Boolean>> result;

        result = subject.loadIpDenied();
        assertThat(result, is(empty()));

        databaseHelper.insertAclIpDenied("128.0.0.2/32");

        result = subject.loadIpDenied();
        assertThat(result, hasSize(1));
        for (IpResourceEntry<Boolean> entry : result) {
            final String ipInterval = entry.getIpInterval().toString();
            if (ipInterval.equals("128.0.0.1/32")) {
                assertThat(entry.getValue(), is(false));
            } else {
                assertThat(entry.getValue(), is(true));
            }
        }
    }

    @Test
    public void removePermanentBlocksBefore() {
        List<IpResourceEntry<Boolean>> denied;

        databaseHelper.insertAclIpDenied("128.0.0.2");
        denied = subject.loadIpDenied();

        assertThat(denied, hasSize(1));

        subject.removePermanentBlocksBefore(new LocalDate());
        denied = subject.loadIpDenied();

        assertThat(denied, hasSize(1));

        subject.removePermanentBlocksBefore(new LocalDate().plusDays(1));
        denied = subject.loadIpDenied();

        assertThat(denied, is(empty()));
    }

    @Test
    public void removeAclEventsBefore() {
        subject.saveAclEvent(Ipv4Resource.parse("10.0.0.0/8"), new LocalDate().minusDays(1), 100, BlockEvent.Type.BLOCK_TEMPORARY);
        assertThat(databaseHelper.listAclEvents(), hasSize(1));

        subject.removeBlockEventsBefore(new LocalDate());
        assertThat(databaseHelper.listAclEvents(), is(empty()));
    }

    @Test
    public void loadIpLimit() {
        List<IpResourceEntry<Integer>> result;

        result = subject.loadIpLimit();
        assertThat(result, is(empty()));

        databaseHelper.insertAclIpLimit("128.0.0.1", 1, false);
        databaseHelper.insertAclIpLimit("128.0.0.2", 2, false);

        result = subject.loadIpLimit();
        assertThat(result, hasSize(2));
        for (IpResourceEntry<Integer> entry : result) {
            final String ipInterval = entry.getIpInterval().toString();
            if (ipInterval.equals("128.0.0.1/32")) {
                assertThat(entry.getValue(), is(1));
            } else {
                assertThat(entry.getValue(), is(2));
            }
        }
    }

    @Test
    public void loadIpUnlimitedConnections() {
        List<IpResourceEntry<Boolean>> result;

        result = subject.loadUnlimitedConnections();
        assertThat(result, is(empty()));

        databaseHelper.insertAclIpLimit("128.0.0.1", 1, false);
        databaseHelper.insertAclIpLimit("128.0.0.2", 2, true);

        result = subject.loadUnlimitedConnections();
        assertThat(result, hasSize(1));
        for (IpResourceEntry<Boolean> entry : result) {
            final String ipInterval = entry.getIpInterval().toString();
            assertThat(ipInterval, is("128.0.0.2/32"));
        }
    }

    @Test
    public void loadIpProxy() {
        List<IpResourceEntry<Boolean>> result;

        result = subject.loadIpProxy();
        assertThat(result, is(empty()));

        databaseHelper.insertAclIpProxy("128.0.0.2");

        result = subject.loadIpProxy();
        assertThat(result, hasSize(1));
        for (IpResourceEntry<Boolean> entry : result) {
            final String ipInterval = entry.getIpInterval().toString();
            if (ipInterval.equals("128.0.0.1")) {
                assertThat(entry.getValue(), is(false));
            } else {
                assertThat(entry.getValue(), is(true));
            }
        }
    }

    @Test
    public void save_permanent_block() {
        final LocalDate blockTime = new LocalDate();
        final int limit = 10;

        subject.savePermanentBlock(Ipv4Resource.parse("128.0.0.1/32"), blockTime, limit, "permanent block");

        final List<IpResourceEntry<Boolean>> entries = subject.loadIpDenied();

        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).getIpInterval().toString(), is("128.0.0.1/32"));
        assertThat(entries.get(0).getValue(), is(true));

        final List<Map<String, Object>> aclEvents = databaseHelper.listAclEvents();
        assertThat(aclEvents, hasSize(1));

        final Map<String, Object> aclEvent = aclEvents.get(0);
        assertThat(aclEvent.get("prefix"), is("128.0.0.1/32"));
        assertThat(new LocalDate(aclEvent.get("event_time")), is(blockTime));
        assertThat(aclEvent.get("daily_limit"), is(limit));
        assertThat(aclEvent.get("event_type"), is(BlockEvent.Type.BLOCK_PERMANENTLY.name()));
    }

    @Test
    public void save_permanent_block_ipv6_canonical() {
        final LocalDate blockTime = new LocalDate();
        final int limit = 10;

        subject.savePermanentBlock(Ipv6Resource.parse("2a03:f480:1:c:0:0:0:0/64"), blockTime, limit, "permanent block");

        final List<IpResourceEntry<Boolean>> entries = subject.loadIpDenied();

        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).getIpInterval().toString(), is("2a03:f480:1:c::/64"));
        assertThat(entries.get(0).getValue(), is(true));

        final List<Map<String, Object>> aclEvents = databaseHelper.listAclEvents();
        assertThat(aclEvents, hasSize(1));

        final Map<String, Object> aclEvent = aclEvents.get(0);
        assertThat(aclEvent.get("prefix"), is("2a03:f480:1:c::/64"));
        assertThat(new LocalDate(aclEvent.get("event_time")), is(blockTime));
        assertThat(aclEvent.get("daily_limit"), is(limit));
        assertThat(aclEvent.get("event_type"), is(BlockEvent.Type.BLOCK_PERMANENTLY.name()));
    }

}
