package net.ripe.db.whois.api.acl;

import com.google.common.collect.Lists;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AclProxyServiceTest {
    @Mock AclServiceDao aclServiceDao;
    @InjectMocks AclProxyService subject;

    @Test
    public void getProxies() {
        List<Proxy> proxies = Lists.newArrayList(new Proxy());
        when(aclServiceDao.getProxies()).thenReturn(proxies);

        final List<Proxy> list = subject.getProxies();
        assertThat(list, is(proxies));
    }

    @Test
    public void getProxy() {
        final Proxy proxy = new Proxy("10.0.0.0/32", "test");
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenReturn(proxy);

        final Response response = subject.getProxy("10.0.0.0/32");
        assertThat((Proxy) response.getEntity(), is(proxy));
    }

    @Test
    public void getProxy_not_found() {
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getProxy("10.0.0.0/32");
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void saveProxy_create() {
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenThrow(EmptyResultDataAccessException.class);

        final Proxy proxy = new Proxy("10.0.0.0/32", "some");
        final Response response = subject.saveProxy(proxy);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Proxy.class));
        assertThat(((Proxy) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        verify(aclServiceDao).createProxy(proxy);
    }

    @Test
    public void saveProxy_modify() {
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenReturn(new Proxy("10.0.0.0/32", ""));

        final Proxy proxy = new Proxy("10.0.0.0/32", "some");
        final Response response = subject.saveProxy(proxy);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Proxy.class));
        assertThat(((Proxy) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        verify(aclServiceDao).updateProxy(proxy);
    }

    @Test
    public void deleteProxy_unknown() {
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.deleteProxy("10.0.0.0/32");
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));

        verify(aclServiceDao, never()).deleteProxy(anyString());
    }

    @Test
    public void deleteProxy() {
        when(aclServiceDao.getProxy("10.0.0.0/32")).thenReturn(new Proxy("10.0.0.0/32", ""));

        final Response response = subject.deleteProxy("10.0.0.0/32");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        verify(aclServiceDao).deleteProxy("10.0.0.0/32");
    }
}
