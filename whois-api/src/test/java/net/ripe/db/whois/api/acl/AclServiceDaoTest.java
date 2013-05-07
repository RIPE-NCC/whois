package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Date;
import java.util.List;

import static net.ripe.db.whois.common.domain.BlockEvent.Type;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AclServiceDaoTest extends AbstractIntegrationTest {
    @Autowired AclServiceDao subject;

    @Test
    public void createLimit() {
        subject.createLimit(new Limit("0/0", "Root", 1000, false));

        final List<Limit> limits = subject.getLimits();
        assertThat(limits, hasSize(1));

        final Limit limit = limits.get(0);
        assertThat(limit.getPrefix(), is("0/0"));
        assertThat(limit.getComment(), is("Root"));
        assertThat(limit.getPersonObjectLimit(), is(1000));
        assertThat(limit.isUnlimitedConnections(), is(false));
    }

    @Test
    public void updateLimit() {
        subject.createLimit(new Limit("0/0", "Root", 1000, false));
        subject.updateLimit(new Limit("0/0", "Root IPv4 object", 10, true));

        final List<Limit> limits = subject.getLimits();
        assertThat(limits, hasSize(1));

        final Limit limit = limits.get(0);
        assertThat(limit.getPrefix(), is("0/0"));
        assertThat(limit.getComment(), is("Root IPv4 object"));
        assertThat(limit.getPersonObjectLimit(), is(10));
        assertThat(limit.isUnlimitedConnections(), is(true));
    }

    @Test
    public void deleteLimit() {
        subject.createLimit(new Limit("0/0", "Root", 1000, false));
        subject.deleteLimit("0/0");

        final List<Limit> limits = subject.getLimits();
        assertThat(limits, hasSize(0));
    }

    @Test
    public void createBan() {
        subject.createBan(new Ban("10.0.0.0/32", "Abuse", new Date()));

        final List<Ban> bans = subject.getBans();
        assertThat(bans, hasSize(1));

        final Ban ban = bans.get(0);
        assertThat(ban.getPrefix(), is("10.0.0.0/32"));
        assertThat(ban.getComment(), is("Abuse"));
    }

    @Test
    public void getBan() {
        subject.createBan(new Ban("10.0.0.0/32", "Abuse", new Date()));

        final Ban ban = subject.getBan("10.0.0.0/32");
        assertThat(ban.getPrefix(), is("10.0.0.0/32"));
        assertThat(ban.getComment(), is("Abuse"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getBan_not_found() {
        subject.createBan(new Ban("10.0.0.0/32", "Abuse", new Date()));
        subject.getBan("10.0.0.0");
    }

    @Test
    public void updateBan() {
        subject.createBan(new Ban("10.0.0.0/32", "Abuse", new Date()));
        subject.updateBan(new Ban("10.0.0.0/32", "Banned after querying too many person objects", new Date()));

        final List<Ban> bans = subject.getBans();
        assertThat(bans, hasSize(1));

        final Ban ban = bans.get(0);
        assertThat(ban.getPrefix(), is("10.0.0.0/32"));
        assertThat(ban.getComment(), is("Banned after querying too many person objects"));
    }

    @Test
    public void deleteBan() {
        subject.createBan(new Ban("10.0.0.0/32", "Abuse", new Date()));
        subject.deleteBan("10.0.0.0/32");

        final List<Ban> bans = subject.getBans();
        assertThat(bans, hasSize(0));
    }

    @Test
    public void createBanEvent() throws Exception {
        subject.createBanEvent("10.0.0.0/32", Type.BLOCK_TEMPORARY);

        final List<BanEvent> banEvents = subject.getBanEvents("10.0.0.0/32");
        assertThat(banEvents, hasSize(1));
        assertThat(banEvents.get(0).getPrefix(), is("10.0.0.0/32"));
        assertThat(banEvents.get(0).getType(), is(Type.BLOCK_TEMPORARY));
    }

    @Test
    public void getProxy() {
        subject.createProxy(new Proxy("10.0.0.0/32", "test"));

        final Proxy proxy = subject.getProxy("10.0.0.0/32");
        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
        assertThat(proxy.getComment(), is("test"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getProxy_not_found() {
        subject.createProxy(new Proxy("10.0.0.0/32", "test"));
        subject.getProxy("10.0.0.0");
    }

    @Test
    public void createProxy() {
        subject.createProxy(new Proxy("10.0.0.0/32", "Test"));

        final List<Proxy> proxies = subject.getProxies();
        assertThat(proxies, hasSize(1));

        final Proxy proxy = proxies.get(0);
        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
        assertThat(proxy.getComment(), is("Test"));
    }

    @Test
    public void updateProxy() {
        subject.createProxy(new Proxy("10.0.0.0/32", "Proxy allowed"));
        subject.updateProxy(new Proxy("10.0.0.0/32", "Proxy allowed for..."));

        final List<Proxy> proxies = subject.getProxies();
        assertThat(proxies, hasSize(1));

        final Proxy proxy = proxies.get(0);
        assertThat(proxy.getPrefix(), is("10.0.0.0/32"));
        assertThat(proxy.getComment(), is("Proxy allowed for..."));
    }

    @Test
    public void deleteProxy() {
        subject.createProxy(new Proxy("10.0.0.0/32", "Test"));
        subject.deleteProxy("10.0.0.0/32");

        final List<Proxy> proxies = subject.getProxies();
        assertThat(proxies, hasSize(0));
    }

    @Test
    public void createMirror() {
        subject.createMirror(new Mirror("127.0.0.1/32", "comment"));
        final List<Mirror> mirrors = subject.getMirrors();
        assertThat(mirrors, hasSize(1));

        final Mirror mirror = mirrors.get(0);
        assertThat(mirror.getPrefix(), is("127.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment"));
    }

    @Test
    public void getMirror() {
        subject.createMirror(new Mirror("127.0.0.1/32", "comment"));
        final Mirror mirror = subject.getMirror("127.0.0.1/32");

        assertThat(mirror.getPrefix(), is("127.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment"));
    }

    @Test
    public void updateMirror() {
        subject.createMirror(new Mirror("127.0.0.1/32", "comment"));
        subject.updateMirror(new Mirror("127.0.0.1/32", "changed comment"));

        final List<Mirror> mirrors = subject.getMirrors();
        assertThat(mirrors, hasSize(1));

        final Mirror mirror = mirrors.get(0);
        assertThat(mirror.getPrefix(), is("127.0.0.1/32"));
        assertThat(mirror.getComment(), is("changed comment"));
    }

    @Test
    public void deleteMirror() {
        subject.createMirror(new Mirror("127.0.0.1/32", "comment"));

        List<Mirror> mirrors = subject.getMirrors();
        assertThat(mirrors, hasSize(1));

        subject.deleteMirror("127.0.0.1/32");

        mirrors = subject.getMirrors();
        assertThat(mirrors, hasSize(0));
    }

    @Test
    public void getMirrors() {
        subject.createMirror(new Mirror("127.0.0.1/32", "comment1"));
        subject.createMirror(new Mirror("128.0.0.1/32", "comment2"));
        subject.createMirror(new Mirror("129.0.0.1/32", "comment3"));
        subject.createMirror(new Mirror("130.0.0.1/32", "comment4"));
        subject.createMirror(new Mirror("131.0.0.1/32", "comment5"));

        List<Mirror> mirrors = subject.getMirrors();
        assertThat(mirrors, hasSize(5));

        Mirror mirror = mirrors.get(0);
        assertThat(mirror.getPrefix(), is("127.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment1"));

        mirror = mirrors.get(1);
        assertThat(mirror.getPrefix(), is("128.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment2"));

        mirror = mirrors.get(2);
        assertThat(mirror.getPrefix(), is("129.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment3"));

        mirror = mirrors.get(3);
        assertThat(mirror.getPrefix(), is("130.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment4"));

        mirror = mirrors.get(4);
        assertThat(mirror.getPrefix(), is("131.0.0.1/32"));
        assertThat(mirror.getComment(), is("comment5"));
    }
}
