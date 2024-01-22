package net.ripe.db.whois.nrtm.dao.jdbc;


import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import net.ripe.db.whois.nrtm.dao.NrtmClientDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm-test.xml"})
public class JdbcNrtmClientDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Autowired
    private NrtmClientDao subject;

    @Test
    public void createObject() {
        final RpslObjectUpdateInfo info = subject.createObject(RpslObject.parse("mntner: TEST-MNT\nmnt-by: TEST-MNT"), 423534);

        assertThat(info.getSequenceId(), is(1));
        assertThat(info.getObjectId(), is(not(0)));
        assertThat(info.getObjectType(), is(ObjectType.MNTNER));

        assertThat(databaseHelper.getWhoisTemplate().queryForObject("SELECT serial_id FROM serials WHERE object_id = ?", Integer.class, info.getObjectId()),
                is(423534));
        final Map<String, Object> stringObjectMap = databaseHelper.getWhoisTemplate().queryForMap("SELECT object_id, mntner FROM mntner ORDER BY object_id DESC LIMIT 1");
        assertThat((String) stringObjectMap.get("mntner"), is("TEST-MNT"));
        assertThat((Long) stringObjectMap.get("object_id"), is((long) info.getObjectId()));
    }

    @Test
    public void updateObject() {
        final RpslObject object = databaseHelper.addObject(RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST"));
        final RpslObject rpslObject = databaseHelper.updateObject(object);
        final RpslObjectUpdateInfo updateInfo = new RpslObjectUpdateInfo(rpslObject.getObjectId(), 2, rpslObject.getType(), rpslObject.getKey().toString());

        final RpslObjectUpdateInfo newSequenceUpdateInfo = subject.updateObject(rpslObject, updateInfo, 6234523);

        assertThat(newSequenceUpdateInfo.getSequenceId(), is(3));
        assertThat(newSequenceUpdateInfo.getKey(), is(rpslObject.getKey().toString()));
        assertThat(newSequenceUpdateInfo.getObjectId(), is(rpslObject.getObjectId()));
        assertThat(newSequenceUpdateInfo.getObjectType(), is(rpslObject.getType()));
        assertThat(databaseHelper.getWhoisTemplate().queryForObject("SELECT serial_id FROM serials WHERE object_id = ? ORDER BY serial_id DESC LIMIT 1", Integer.class, newSequenceUpdateInfo.getObjectId()),
                is(6234523));
    }

    @Test
    public void objectExistsWithSerial() {
        final RpslObject object = databaseHelper.updateObject(databaseHelper.addObject(RpslObject.parse("aut-num: AS2345")));

        assertThat(subject.objectExistsWithSerial(2462345, object.getObjectId()), is(false));

        databaseHelper.getWhoisTemplate().update("" +
                "INSERT INTO serials(serial_id, object_id, sequence_id, atlast, operation) " +
                "VALUES (2462345, ?, 2, 1, ?)",
                object.getObjectId(), Operation.UPDATE.getCode());

        assertThat(subject.objectExistsWithSerial(2462345, object.getObjectId()), is(true));
    }

    @Test
    public void deleteObject() {
        final RpslObject object = databaseHelper.updateObject(databaseHelper.addObject(RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST")));
        subject.deleteObject(new RpslObjectUpdateInfo(object.getObjectId(), 2, object.getType(), object.getKey().toString()), 2563245);

        assertThat(databaseHelper.getWhoisTemplate().queryForObject("SELECT serial_id FROM serials WHERE object_id = ? ORDER BY serial_id DESC limit 1", Integer.class, object.getObjectId()),
                is(2563245));
        //TODO more assertions
    }
}
