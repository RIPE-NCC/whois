package net.ripe.db.nrtm4.client;

import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientDocumentType;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.processor.UpdateNotificationFileProcessor;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-client-test.xml"})
public class AbstractNrtmClientIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    protected JdbcTemplate nrtmClientTemplate;

    protected JdbcTemplate nrtmClientInfoTemplate;

    @Autowired
    protected Nrtm4ClientInfoRepository nrtm4ClientInfoRepository;

    @Autowired
    protected Nrtm4ClientRepository nrtm4ClientRepository;

    @Autowired
    protected UpdateNotificationFileProcessor updateNotificationFileProcessor;

    @Autowired
    protected NrtmServerDummy nrtmServerDummy;


    @Autowired(required = false)
    @Qualifier("nrtmClientMasterDataSource")
    public void setNrtmClientMasterSource(DataSource dataSource) {
        nrtmClientTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired(required = false)
    @Qualifier("nrtmClientMasterInfoSource")
    public void setNrtmClientMasterInfoSource(DataSource dataSource) {
        nrtmClientInfoTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    public void reset(){
        nrtm4ClientInfoRepository.truncateTables();
        nrtm4ClientRepository.truncateTables();
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

    protected List<MirrorRpslObject> getMirrorRpslObject(){
        final String sql = """
            SELECT object
            FROM last
            WHERE sequence_id > 0
            """;
        return nrtmClientTemplate.query(sql,
                (rs, rn) -> new MirrorRpslObject(RpslObject.parse(rs.getBytes(1))));
    }

    @Nullable
    protected RpslObject getMirrorRpslObjectByPkey(final String primaryKey){
        try {
                final String sql = """
                    SELECT object
                    FROM last
                    WHERE pkey = ?
                    AND sequence_id > 0
                    """;
                return nrtmClientTemplate.queryForObject(sql,
                        (rs, rn) -> RpslObject.parse(rs.getBytes(1)),
                        primaryKey);
            } catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

    protected List<NrtmClientVersionInfo> getNrtmLastSnapshotVersion(){
        final String sql = """
            SELECT id, source, MAX(version), session_id, type, hostname, created
            FROM version_info
            WHERE type = ?
            GROUP BY source
            """;
        return nrtmClientInfoTemplate.query(sql,
            (rs, rn) -> new NrtmClientVersionInfo(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getLong(3),
                    rs.getString(4),
                    NrtmClientDocumentType.fromValue(rs.getString(5)),
                    rs.getString(6),
                    rs.getLong(7)
            ), NrtmClientDocumentType.SNAPSHOT.getFileNamePrefix());
    }

}
