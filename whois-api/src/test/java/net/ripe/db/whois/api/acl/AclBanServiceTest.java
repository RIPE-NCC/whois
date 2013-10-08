package net.ripe.db.whois.api.acl;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpInterval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AclBanServiceTest {
    @Mock AclServiceDao aclServiceDao;
    @InjectMocks AclBanService subject;

    @Test
    public void getBans() {
        List<Ban> bans = Lists.newArrayList(new Ban());
        when(aclServiceDao.getBans()).thenReturn(bans);

        final List<Ban> list = subject.getBans();
        assertThat(list, is(bans));
    }

    @Test
    public void getBan() {
        final Ban ban = new Ban("10.0.0.0/32", "test");
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenReturn(ban);

        final Response response = subject.getBan("10.0.0.0/32");
        assertThat((Ban) response.getEntity(), is(ban));
    }

    @Test
    public void getBan_not_found() {
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getBan("10.0.0.0/32");
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void saveBan_create() {
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);

        final Ban ban = new Ban("10.0.0.0/32", "some");
        final Response response = subject.saveBan(ban);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Ban.class));
        assertThat(((Ban) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        verify(aclServiceDao).createBan(ban);
        verify(aclServiceDao).createBanEvent(IpInterval.parse("10.0.0.0/32"), BlockEvent.Type.BLOCK_PERMANENTLY);
    }

    @Test
    public void saveBan_modify() {
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenReturn(new Ban("10.0.0.0/32", ""));

        final Ban ban = new Ban("10.0.0.0/32", "some");
        final Response response = subject.saveBan(ban);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Ban.class));
        assertThat(((Ban) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        verify(aclServiceDao).updateBan(ban);
        verify(aclServiceDao, never()).createBanEvent(any(IpInterval.class), any(BlockEvent.Type.class));
    }

    @Test
    public void deleteBan_unknown() {
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.deleteBan("10.0.0.0/32");
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));

        verify(aclServiceDao, never()).deleteBan(any(IpInterval.class));
        verify(aclServiceDao, never()).createBanEvent(any(IpInterval.class), any(BlockEvent.Type.class));
    }

    @Test
    public void deleteBan() {
        when(aclServiceDao.getBan(IpInterval.parse("10.0.0.0/32"))).thenReturn(new Ban("10.0.0.0/32", ""));

        final Response response = subject.deleteBan("10.0.0.0/32");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        verify(aclServiceDao).deleteBan(IpInterval.parse("10.0.0.0/32"));
        verify(aclServiceDao).createBanEvent(IpInterval.parse("10.0.0.0/32"), BlockEvent.Type.UNBLOCK);
    }

    @Test
    public void getBanEvents() {
        List<BanEvent> banEvents = Lists.newArrayList(new BanEvent());
        when(aclServiceDao.getBanEvents(IpInterval.parse("10.0.0.0/32"))).thenReturn(banEvents);

        final List<BanEvent> list = subject.getBanEvents("10.0.0.0/32");
        assertThat(list, is(banEvents));
    }
}
