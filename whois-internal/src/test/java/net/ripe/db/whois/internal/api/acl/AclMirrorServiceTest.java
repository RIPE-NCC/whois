package net.ripe.db.whois.internal.api.acl;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AclMirrorServiceTest {
    @Mock
    AclServiceDao aclServiceDao;
    @InjectMocks
    AclMirrorService subject;

    @Test
    public void getMirrors() {
        List<Mirror> mirrors = Lists.newArrayList(new Mirror());
        Mockito.when(aclServiceDao.getMirrors()).thenReturn(mirrors);

        List<Mirror> mirrorList = subject.getMirrors();
        MatcherAssert.assertThat(mirrorList, is(mirrors));
    }

    @Test
    public void getMirror_existing() {
        Mirror mirror = new Mirror("10.0.0.0/32", "comment");

        Mockito.when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.0/32")))).thenReturn(mirror);

        final Response response = subject.getMirror("10.0.0.0/32");
        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        MatcherAssert.assertThat(response.getEntity(), IsInstanceOf.instanceOf(Mirror.class));
        MatcherAssert.assertThat(((Mirror) response.getEntity()), is(mirror));
    }

    @Test
    public void getMirror_non_existing() {
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.1/32")))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getMirror("10.0.0.1/32");
        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMirror_invalid() {
        List<Mirror> mirrors = Lists.newArrayList();
        mirrors.add(new Mirror("10.0.0.1/32", "comment"));
        mirrors.add(new Mirror("10.0.0.0/32", "some comment"));

        Mockito.when(aclServiceDao.getMirrors()).thenReturn(mirrors);
        Mirror mirror = new Mirror("0", "other comment");
        subject.saveMirror(mirror);
    }

    @Test
    public void getMirror_parent() {
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.1/32"))).thenThrow(EmptyResultDataAccessException.class);

        final Response response = subject.getMirror("10.0.0.1/32");
        MatcherAssert.assertThat(response.getStatus(), is(404));
    }

    @Test
    public void getMirror_encoded_prefix() {
        Mirror mirror = new Mirror("10.0.0.0/32", "comment");

        Mockito.when(aclServiceDao.getMirror(IpInterval.parse(CIString.ciString("10.0.0.0/32")))).thenReturn(mirror);

        final Response response = subject.getMirror("10.0.0.0%2F32");
        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        MatcherAssert.assertThat(response.getEntity(), IsInstanceOf.instanceOf(Mirror.class));
        MatcherAssert.assertThat(((Mirror) response.getEntity()), is(mirror));
    }

    @Test
    public void saveMirror_create() {
        List<Mirror> mirrors = Lists.newArrayList(new Mirror("10.0.0.1/32", "comment"));
        Mockito.when(aclServiceDao.getMirrors()).thenReturn(mirrors);
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);
        Mirror mirror = new Mirror("10.0.0.0/32", "more comments");

        final Response response = subject.saveMirror(mirror);
        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        MatcherAssert.assertThat(response.getEntity(), IsInstanceOf.instanceOf(Mirror.class));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));
        Mockito.verify(aclServiceDao).createMirror(mirror);
    }

    @Test
    public void saveMirror_update() {
        List<Mirror> mirrors = Lists.newArrayList();
        mirrors.add(new Mirror("0/0", "comment"));
        mirrors.add(new Mirror("10.0.0.0/32", "more comments"));

        Mockito.when(aclServiceDao.getMirrors()).thenReturn(mirrors);

        Mirror mirror = new Mirror("10.0.0.0/32", "updated comment");
        Response response = subject.saveMirror(mirror);

        MatcherAssert.assertThat(response.getEntity(), IsInstanceOf.instanceOf(Mirror.class));
        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));
        assertThat(((Mirror) response.getEntity()).getComment(), is("updated comment"));

        Mockito.verify(aclServiceDao).updateMirror(mirror);
    }

    @Test
    public void deleteMirror() {
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenReturn(new Mirror("10.0.0.0/32", "comment"));

        Response response = subject.deleteMirror("10.0.0.0/32");

        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        Mockito.verify(aclServiceDao).deleteMirror(IpInterval.parse("10.0.0.0/32"));
    }

    @Test
    public void deleteInvalidMirror() {
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenThrow(EmptyResultDataAccessException.class);
        Response response = subject.deleteMirror("10.0.0.0/32");

        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        Mockito.verify(aclServiceDao, Mockito.never()).deleteMirror(IpInterval.parse("10.0.0.0/32"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMirror_rootObjectIpv4() {
        subject.deleteMirror("0/0");

        Mockito.verify(aclServiceDao, Mockito.never()).deleteMirror(IpInterval.parse("0.0.0.0/0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMirror_rootObjectIpv6() {
        subject.deleteMirror("::0/0");

        Mockito.verify(aclServiceDao, Mockito.never()).deleteMirror(IpInterval.parse("::0/0"));
    }

    @Test
    public void deleteMirrorWithEncodedURL() {
        Mockito.when(aclServiceDao.getMirror(IpInterval.parse("10.0.0.0/32"))).thenReturn(new Mirror("10.0.0.0/32", "comment"));

        Response response = subject.deleteMirror("10.0.0.0%2F32");

        MatcherAssert.assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(((Mirror) response.getEntity()).getPrefix(), is("10.0.0.0/32"));

        Mockito.verify(aclServiceDao).deleteMirror(IpInterval.parse("10.0.0.0/32"));
    }
}
