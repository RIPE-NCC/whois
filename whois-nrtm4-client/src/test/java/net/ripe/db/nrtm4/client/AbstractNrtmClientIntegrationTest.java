package net.ripe.db.nrtm4.client;

import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientDocumentType;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.processor.UpdateNotificationFileProcessor;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-client-test.xml"})
public class AbstractNrtmClientIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    protected Nrtm4ClientMirrorRepository nrtm4ClientMirrorRepository;

    @Autowired
    protected UpdateNotificationFileProcessor updateNotificationFileProcessor;

    @Autowired
    protected NrtmServerDummy nrtmServerDummy;

    @BeforeEach
    public void reset(){
        nrtm4ClientMirrorRepository.truncateTables();
        nrtmServerDummy.resetDefaultMocks();
    }

    @BeforeAll
    public static void setUp(){
        System.setProperty("nrtm4.client.enabled", "true");
    }

    @AfterAll
    public static void tearDown(){
        System.clearProperty("nrtm4.client.enabled");
    }

    protected List<RpslObject> getMirrorRpslObject(){
        final String sql = """
            SELECT object
            FROM last_mirror
            """;
        return nrtmClientTemplate.query(sql,
                (rs, rn) -> RpslObject.parse(rs.getBytes(1)));
    }

    protected RpslObject getMirrorRpslObjectByPkey(final String primaryKey){
        final String sql = """
            SELECT object
            FROM last_mirror
            WHERE pkey = ?
            """;
        return nrtmClientTemplate.queryForObject(sql,
                (rs, rn) -> RpslObject.parse(rs.getBytes(1)),
                primaryKey);
    }

    protected List<NrtmClientVersionInfo> getNrtmLastSnapshotVersion(){
        final String sql = """
            SELECT id, source, MAX(version), session_id, type, created
            FROM version_info
            WHERE type = 'nrtm-snapshot'
            GROUP BY source
            """;
        return nrtmClientTemplate.query(sql,
            (rs, rn) -> new NrtmClientVersionInfo(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getLong(3),
                    rs.getString(4),
                    NrtmClientDocumentType.fromValue(rs.getString(5)),
                    rs.getLong(6)
            ));
    }
}
