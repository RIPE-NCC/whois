package net.ripe.db.whois.update.dao;


import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.hamcrest.core.Is;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class JdbcPendingUpdateDaoTest extends AbstractUpdateDaoTest {
    @Autowired private PendingUpdateDao subject;
    @Autowired private DateTimeProvider dateTimeProvider;
    @Autowired @Qualifier("internalsDataSource") private DataSource dataSource;

    @Test
    public void findByTypeAndPkey_existing_object() throws SQLException {
        final RpslObject object = RpslObject.parse("route6: 2001:1578:0200::/40\nmnt-by: TEST-MNT\norigin: AS12726");
        subject.store(new PendingUpdate(Sets.newHashSet("RouteAutnumAuthentication"), object, dateTimeProvider.getCurrentDateTime()));

        final List<PendingUpdate> result = subject.findByTypeAndKey(ObjectType.ROUTE6, object.getKey().toString());
        assertThat(result, hasSize(1));

        final PendingUpdate pendingUpdate = result.get(0);
        assertThat(pendingUpdate.getStoredDate(), is(not(nullValue())));
        assertThat(pendingUpdate.getObject(), is(object));
        assertThat(pendingUpdate.getPassedAuthentications(), containsInAnyOrder("RouteAutnumAuthentication"));
    }

    @Test
    public void findByTypeAndPkey_non_existing_object() {
        final RpslObject object = RpslObject.parse("route: 193.0/8\norigin: AS23423\nsource: TEST");
        subject.store(new PendingUpdate(Sets.newHashSet("RouteIpAddressAuthentication"), object, dateTimeProvider.getCurrentDateTime()));

        final List<PendingUpdate> result = subject.findByTypeAndKey(ObjectType.ROUTE6, object.getKey().toString());
        assertThat(result, hasSize(0));
    }

    @Test
    public void findByTypeAndPkey_orders_results() throws InterruptedException {
        final RpslObject object = RpslObject.parse("route6: 1995:1996::/40\norigin:AS12345");
        final RpslObject object2 = RpslObject.parse("route6: 1995:1996::/40\norigin:AS12345");

        subject.store(new PendingUpdate(1, Sets.newHashSet("RouteIpAddressAuthentication"), object, new LocalDateTime(2013, 12, 15, 15, 32)));
        subject.store(new PendingUpdate(1, Sets.newHashSet("RouteIpAddressAuthentication"), object2, new LocalDateTime(2013, 12, 16, 21, 2)));

        final List<PendingUpdate> result = subject.findByTypeAndKey(object.getType(), object.getKey().toString());
        assertThat(result, hasSize(2));
        final PendingUpdate first = result.get(0);
        final PendingUpdate last = result.get(1);

        assertThat(first.getStoredDate().isBefore(last.getStoredDate()), is(true));
    }

    @Test
    public void store() {
        final RpslObject object = RpslObject.parse("route: 192.168.0/16\norigin:AS1234");
        final PendingUpdate pendingUpdate = new PendingUpdate(1, Sets.newHashSet("RouteAutnumAuthentication"), object, LocalDateTime.parse("2012-01-01"));
        subject.store(pendingUpdate);

        final List<Map<String, Object>> result = getPendingUpdates(object);
        assertThat(result, hasSize(1));
        final Map<String, Object> objectMap = result.get(0);

        assertThat(objectMap.get("object_type"), Is.<Object>is(ObjectTypeIds.getId(ObjectType.ROUTE)));
        assertThat(objectMap.get("pkey"), Is.<Object>is(object.getKey().toString()));
        assertThat(objectMap.get("stored_date").toString(), Is.<Object>is("2012-01-01"));
        assertThat(objectMap.get("passed_authentications"), Is.<Object>is("RouteAutnumAuthentication"));
        assertThat(objectMap.get("object"), Is.<Object>is(object.toString().getBytes()));
    }

    @Test
    public void remove() {
        final RpslObject object = RpslObject.parse("route6: 5555::4444/48\norigin:AS1234");
        final PendingUpdate pending = new PendingUpdate(Sets.newHashSet("RouteIpAddressAuthentication"), object, dateTimeProvider.getCurrentDateTime());
        subject.store(pending);

        final int pendingId = new JdbcTemplate(dataSource).queryForInt("select id from pending_updates");
        subject.remove(new PendingUpdate(pendingId, Sets.newHashSet("RouteIpAddressAuthentication"), object, new LocalDateTime()));

        final List<Map<String, Object>> result = getPendingUpdates(object);
        assertThat(result, hasSize(0));
    }

    private List<Map<String, Object>> getPendingUpdates(RpslObject object) {
        return databaseHelper.getInternalsTemplate().queryForList(
                "SELECT * FROM pending_updates WHERE pkey = ?",
                object.getKey().toString());
    }

    @Test
    public void find_before_date() {
        final PendingUpdate pendingUpdate = new PendingUpdate(1, Sets.newHashSet("OWNER-MNT"), RpslObject.parse("route: 10.0.0.0/8\norigin: AS0\nmnt-by: OWNER-MNT\nsource: TEST"), LocalDateTime.now().minusDays(8));
        subject.store(pendingUpdate);

        final List<PendingUpdate> pendingUpdates = subject.findBeforeDate(LocalDateTime.now().minusDays(7));

        assertThat(pendingUpdates, hasSize(1));
        assertThat(pendingUpdates.get(0).getId(), is(pendingUpdate.getId()));
        assertThat(pendingUpdates.get(0).getObject(), is(pendingUpdate.getObject()));
    }
}
