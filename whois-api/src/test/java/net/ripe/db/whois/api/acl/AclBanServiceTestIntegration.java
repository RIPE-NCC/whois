package net.ripe.db.whois.api.acl;

import com.sun.jersey.api.client.GenericType;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.domain.BlockEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AclBanServiceTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;
    private static final String BANS_PATH = "api/acl/bans";

    @Autowired TestDateTimeProvider dateTimeProvider;

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclIpDenied("10.0.0.0/32");
        databaseHelper.insertApiKey("DB-WHOIS-testapikey", "/api/acl", "acl api key");
        setApiKey("DB-WHOIS-testapikey");
    }

    @Test
    public void bans() throws Exception {
        databaseHelper.insertAclIpDenied("10.0.0.1/32");
        databaseHelper.insertAclIpDenied("10.0.0.2/32");
        databaseHelper.insertAclIpDenied("2001::/64");

        @SuppressWarnings("unchecked")
        final List<Ban> bans = getBans();

        assertThat(bans, hasSize(4));
    }

    @Test
    public void createBan() throws Exception {
        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        assertThat(ban.getPrefix(), is("10.0.0.1/32"));
        assertThat(ban.getComment(), is("test"));

        final List<Ban> bans = getBans();
        assertThat(bans, hasSize(2));

        final List<BanEvent> banEvents = getBanEvents("10.0.0.1/32");
        assertThat(banEvents, hasSize(1));
        final BanEvent banEvent = banEvents.get(0);
        assertThat(banEvent.getPrefix(), is("10.0.0.1/32"));
        assertThat(banEvent.getType(), is(BlockEvent.Type.BLOCK_PERMANENTLY));
    }

    @Test
    public void createBanWithoutPrefixLength() throws Exception {
        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1", "test"));

        assertThat(ban.getPrefix(), is("10.0.0.1/32"));
        assertThat(ban.getComment(), is("test"));
    }

    @Test
    public void updateBan() throws Exception {
        createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "updated"));

        assertThat(ban.getPrefix(), is("10.0.0.1/32"));
        assertThat(ban.getComment(), is("updated"));

        final List<Ban> bans = getBans();
        assertThat(bans, hasSize(2));

        final List<BanEvent> banEvents = getBanEvents("10.0.0.1/32");
        assertThat(banEvents, hasSize(1));
        final BanEvent banEvent = banEvents.get(0);
        assertThat(banEvent.getPrefix(), is("10.0.0.1/32"));
        assertThat(banEvent.getType(), is(BlockEvent.Type.BLOCK_PERMANENTLY));
    }

    @Test
    public void updateBanWithoutPrefixLength() throws Exception {
        createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1", "updated"));

        assertThat(ban.getPrefix(), is("10.0.0.1/32"));
        assertThat(ban.getComment(), is("updated"));
    }

    @Test
    public void deleteBan() throws Exception {
        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        plusOneDay();

        final Ban deletedBan = createResource(AUDIENCE, BANS_PATH, ban.getPrefix())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(Ban.class);

        assertThat(deletedBan.getPrefix(), is("10.0.0.1/32"));
        assertThat(deletedBan.getComment(), is("test"));

        final List<Ban> bans = getBans();
        assertThat(bans, hasSize(1));

        final List<BanEvent> banEvents = getBanEvents("10.0.0.1/32");
        assertThat(banEvents, hasSize(2));
        assertThat(banEvents.get(0).getType(), is(BlockEvent.Type.UNBLOCK));
        assertThat(banEvents.get(1).getType(), is(BlockEvent.Type.BLOCK_PERMANENTLY));
    }

    @Test
    public void deleteBanWithoutPrefixLength() throws Exception {
        createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        plusOneDay();

        final Ban deletedBan = createResource(AUDIENCE, BANS_PATH, "10.0.0.1")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(Ban.class);

        assertThat(deletedBan.getPrefix(), is("10.0.0.1/32"));
        assertThat(deletedBan.getComment(), is("test"));

        final List<Ban> bans = getBans();
        assertThat(bans, hasSize(1));
    }

    @Test
    public void getBan() throws Exception {
        final Ban ban = createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        plusOneDay();

        final Ban createdBan = createResource(AUDIENCE, BANS_PATH, ban.getPrefix())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ban.class);

        assertThat(createdBan.getPrefix(), is("10.0.0.1/32"));
        assertThat(createdBan.getComment(), is("test"));
    }

    @Test
    public void getBanWithoutPrefixLength() throws Exception {
        createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Ban.class, new Ban("10.0.0.1/32", "test"));

        plusOneDay();

        final Ban createdBan = createResource(AUDIENCE, BANS_PATH, "10.0.0.1")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ban.class);

        assertThat(createdBan.getPrefix(), is("10.0.0.1/32"));
        assertThat(createdBan.getComment(), is("test"));
    }

    @SuppressWarnings("unchecked")
    private List<Ban> getBans() {
        return createResource(AUDIENCE, BANS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Ban>>() {
                });
    }

    @SuppressWarnings("unchecked")
    private List<BanEvent> getBanEvents(final String prefix) {
        return createResource(AUDIENCE, String.format("%s/%s/events", BANS_PATH, encode(prefix)))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<BanEvent>>() {
                });
    }

    private void plusOneDay() {
        dateTimeProvider.setTime(dateTimeProvider.getCurrentDateTime().plusDays(1));
    }
}
