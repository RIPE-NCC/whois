package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class SnapshotObjectRepositoryIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private SnapshotObjectRepository snapshotObjectRepository;

    @Autowired
    private SnapshotObjectIteratorRepository snapshotObjectIteratorRepository;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private NrtmSourceHolder source;

    @Test
    void should_insert_payloads_and_stream_them() throws IOException {
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, 1, escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, 1, escapedOrgString);
        final var list = new ArrayList<String>();
        snapshotObjectIteratorRepository.snapshotCallbackConsumer(source.getSource(), list::add);
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
        assertThat(list.get(1), is("organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n"));
    }

    @Test
    void should_insert_and_delete_payloads_and_stream_them() {
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, 1, escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, 1, escapedOrgString);
        snapshotObjectRepository.delete(2);
        final var list = new ArrayList<String>();
        snapshotObjectIteratorRepository.snapshotCallbackConsumer(source.getSource(), list::add);
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
    }

    private final RpslObject escapedInetnumString = RpslObject.parse("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n");
    private final RpslObject escapedOrgString = RpslObject.parse("organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n");

}
