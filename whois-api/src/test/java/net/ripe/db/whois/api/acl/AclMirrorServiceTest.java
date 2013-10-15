package net.ripe.db.whois.api.acl;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AclMirrorServiceTest {
    @Mock
    AclServiceDao aclServiceDao;
    @InjectMocks
    AclMirrorService subject;
    @Mock
    HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        when(request.getCharacterEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    }

    @Test
    public void getMirrors() {
        List<Mirror> mirrors = Lists.newArrayList(new Mirror());
        when(aclServiceDao.getMirrors()).thenReturn(mirrors);

        List<Mirror> mirrorList = subject.getMirrors();
        assertThat(mirrorList, is(mirrors));
    }

    @Test
    public void getMirror_existing() {
        Mirror mirror = new Mirror("10.0.0.0/32", "comment");

        when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.0/32")))).thenReturn(mirror);

        final Response response = subject.getMirror("10.0.0.0/32", request);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Mirror.class));
        assertThat(((Mirror) response.getEntity()), is(mirror));
    }

    @Test
    public void getMirror_non_existing() {
        when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.1/32")))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getMirror("10.0.0.1/32", request);
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMirror_invalid() {
        List<Mirror> mirrors = Lists.newArrayList();
        mirrors.add(new Mirror("10.0.0.1/32", "comment"));
        mirrors.add(new Mirror("10.0.0.0/32", "some comment"));

        when(aclServiceDao.getMirrors()).thenReturn(mirrors);
        Mirror mirror = new Mirror("0", "other comment");
        subject.saveMirror(mirror);
    }

    @Test
    public void getMirror_parent() {
        when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.1/32"))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getMirror("10.0.0.1/32", request);
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void getMirror_encoded_prefix() {
        Mirror mirror = new Mirror("10.0.0.0/32", "comment");

        when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.0/32")))).thenReturn(mirror);

        final Response response = subject.getMirror("10.0.0.0%2F32", request);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Mirror.class));
        assertThat(((Mirror) response.getEntity()), is(mirror));
    }

    @Test
    public void saveMirror_create() {
        List<Mirror> mirrors = Lists.newArrayList(new Mirror("10.0.0.1/32", "comment"));
        when(aclServiceDao.getMirrors()).thenReturn(mirrors);
        when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);
        Mirror mirror = new Mirror("10.0.0.0/32", "more comments");

        final Response response = subject.saveMirror(mirror);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(Mirror.class));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));
        verify(aclServiceDao).createMirror(mirror);
    }

    @Test
    public void saveMirror_update() {
        List<Mirror> mirrors = Lists.newArrayList();
        mirrors.add(new Mirror("0/0", "comment"));
        mirrors.add(new Mirror("10.0.0.0/32", "more comments"));

        when(aclServiceDao.getMirrors()).thenReturn(mirrors);

        Mirror mirror = new Mirror("10.0.0.0/32", "updated comment");
        Response response = subject.saveMirror(mirror);

        assertThat(response.getEntity(), instanceOf(Mirror.class));
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));
        assertThat(((Mirror) response.getEntity()).getComment(), is("updated comment"));

        verify(aclServiceDao).updateMirror(mirror);
    }

    @Test
    public void deleteMirror() {
        when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenReturn(new Mirror("10.0.0.0/32", "comment"));

        Response response = subject.deleteMirror("10.0.0.0/32", request);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        verify(aclServiceDao).deleteMirror(IpInterval.parse("10.0.0.0/32"));
    }

    @Test
    public void deleteInvalidMirror() {
        when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);
        Response response = subject.deleteMirror("10.0.0.0/32", request);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        verify(aclServiceDao, never()).deleteMirror(IpInterval.parse("10.0.0.0/32"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMirror_rootObjectIpv4() {
        subject.deleteMirror("0/0", request);

        verify(aclServiceDao, never()).deleteMirror(IpInterval.parse("0.0.0.0/0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMirror_rootObjectIpv6() {
        subject.deleteMirror("::0/0", request);

        verify(aclServiceDao, never()).deleteMirror(IpInterval.parse("::0/0"));
    }
}
