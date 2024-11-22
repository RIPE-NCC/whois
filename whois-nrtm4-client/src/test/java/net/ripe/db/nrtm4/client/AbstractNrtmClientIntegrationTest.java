package net.ripe.db.nrtm4.client;

import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-client-test.xml"})
public class AbstractNrtmClientIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    protected JdbcTemplate nrtmClientTemplate;

    @Autowired
    protected Nrtm4ClientMirrorRepository nrtm4ClientMirrorRepository;

    @Autowired
    protected UpdateNotificationFileProcessor updateNotificationFileProcessor;

    @Autowired
    protected NrtmServerDummy nrtmServerDummy;

    private static final String NRTM_PUBLIC_KEY = """
            {"kty":"OKP","crv":"Ed25519","kid":"a9ddf4a5-0ca0-47b1-a80d-3c63fd5c19c5","x":"ry9yLgcy1eUNX1lDs852mmUXRoy4qZW1HSOu54qBCHI"}
            """;


    @Autowired(required = false)
    @Qualifier("nrtmClientMasterDataSource")
    public void setNrtmClientMasterDataSource(DataSource dataSource) {
        nrtmClientTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    public void reset(){
        nrtm4ClientMirrorRepository.truncateTables();
        nrtmServerDummy.resetDefaultMocks();
    }

    @BeforeAll
    public static void setUp(){
        System.setProperty("nrtm4.client.enabled", "true");
        System.setProperty("nrtm.key", NRTM_PUBLIC_KEY);

    }

    @AfterAll
    public static void tearDown(){
        System.clearProperty("nrtm4.client.enabled");
        System.clearProperty("nrtm.key");
    }

    protected List<MirrorRpslObject> getMirrorRpslObject(){
        final String sql = """
            SELECT object
            FROM last_mirror
            """;
        return nrtmClientTemplate.query(sql,
                (rs, rn) -> new MirrorRpslObject(RpslObject.parse(rs.getBytes(1))));
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
