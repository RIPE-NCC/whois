package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static net.ripe.db.whois.query.support.Fixture.createBlockEvents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AutomaticPermanentBlocksTest {

    private static final String IPV6_PREFIX = "2001:67c:2e8:1:0:0:0:0/64";
    private static final String IPV4_PREFIX = "10.0.0.1";
    private static final int QUERY_LIMIT = 100;

    @Mock DateTimeProvider dateTimeProvider;
    @Mock
    IpAccessControlListDao ipAccessControlListDao;
    @Mock IpResourceConfiguration ipResourceConfiguration;
    @InjectMocks AutomaticPermanentBlocks subject;

    LocalDate now;

    @BeforeEach
    public void setUp() throws Exception {
        now = LocalDate.now();
        when(dateTimeProvider.getCurrentDate()).thenReturn(now);
        lenient().when(ipResourceConfiguration.getLimit(any(InetAddress.class))).thenReturn(QUERY_LIMIT);
    }

    @Test
    public void test_date() throws Exception {
        subject.run();

        verify(ipAccessControlListDao, times(1)).getTemporaryBlocks(now.minusDays(30));
    }

    @Test
    public void test_run_no_temporary_blocks() throws Exception {
        lenient().when(ipAccessControlListDao.getTemporaryBlocks(now)).thenReturn(Collections.<BlockEvents>emptyList());

        subject.run();

        verify(ipAccessControlListDao, never()).savePermanentBlock(any(IpInterval.class), any(LocalDate.class), anyInt(), anyString());
    }

    @Test
    public void test_run_temporary_blocks_times_9() throws Exception {
        test_run_temporary_block(9, IPV4_PREFIX);
    }

    @Test
    public void test_run_temporary_blocks_times_10() throws Exception {
        test_run_temporary_block(10, IPV4_PREFIX);
    }

    @Test
    public void test_run_temporary_blocks_times_30() throws Exception {
        test_run_temporary_block(30, IPV4_PREFIX);
    }

    @Test
    public void test_handle_block_event_ipv6() {
        test_run_temporary_block(30, IPV6_PREFIX);
    }

    @Captor
    ArgumentCaptor<IpInterval> argumentCaptor;

    public void test_run_temporary_block(final int times, String prefix) {
        when(ipAccessControlListDao.getTemporaryBlocks(now.minusDays(30))).thenReturn(Arrays.asList(createBlockEvents(prefix, times)));

        subject.run();

        if (times < 10) {
            verify(ipAccessControlListDao, never()).savePermanentBlock(any(IpInterval.class), any(LocalDate.class), anyInt(), anyString());
        } else {
            verify(ipAccessControlListDao).savePermanentBlock(argumentCaptor.capture(), any(LocalDate.class), eq(QUERY_LIMIT), eq("Automatic permanent ban after " + times + " temporary blocks at " + FormatHelper.dateToString(now)));
            assertThat(argumentCaptor.getValue().toString(), is(IpInterval.parse(prefix).toString()));
        }
    }

    @Test
    public void test_run_temporary_blocks_already_denied() throws Exception {
        when(ipAccessControlListDao.getTemporaryBlocks(now.minusDays(30))).thenReturn(Arrays.asList(createBlockEvents(IPV4_PREFIX, 20)));
        when(ipResourceConfiguration.isDenied(any(InetAddress.class))).thenReturn(true);

        subject.run();

        verify(ipResourceConfiguration).isDenied(any(InetAddress.class));
        verify(ipAccessControlListDao, never()).savePermanentBlock(any(IpInterval.class), any(LocalDate.class), anyInt(), anyString());
    }
}
