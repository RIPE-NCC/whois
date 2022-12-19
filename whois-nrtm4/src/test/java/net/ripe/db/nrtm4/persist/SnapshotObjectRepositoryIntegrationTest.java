package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class SnapshotObjectRepositoryIntegrationTest extends AbstractDatabaseHelperIntegrationTest  {

    @Autowired
    private SnapshotObjectRepository snapshotObjectRepository;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private NrtmSourceHolder source;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    void should_insert_payloads_and_stream_them() throws IOException {
        final var version = nrtmVersionInfoRepository.createInitialSnapshot(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, ObjectType.INETNUM, "193.0.0.0 - 193.255.255.255", escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, ObjectType.ORGANISATION, "ORG-XYZ99-RIPE", escapedOrgString);
        final var stream = snapshotObjectRepository.getSnapshotAsStream(source.getSource());
        final var list = stream.collect(Collectors.toList());
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
        assertThat(list.get(1), is("organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n"));
    }

    @Test
    void should_insert_and_delete_payloads_and_stream_them() throws IOException {
        final var version = nrtmVersionInfoRepository.createInitialSnapshot(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, ObjectType.INETNUM, "193.0.0.0 - 193.255.255.255", escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, ObjectType.ORGANISATION, "ORG-XYZ99-RIPE", escapedOrgString);
        snapshotObjectRepository.delete(ObjectType.ORGANISATION, "ORG-XYZ99-RIPE");
        final var stream = snapshotObjectRepository.getSnapshotAsStream(source.getSource());
        final var list = stream.collect(Collectors.toList());
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
    }

    private final String escapedInetnumString = "inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n";
    private final String escapedOrgString = "organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n";

}
