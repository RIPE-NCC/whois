package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.stream.Collectors;

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

    @Test
    void should_insert_payloads_and_stream_them() throws IOException {
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1, 1, escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, 1, escapedOrgString);
        final var stream = snapshotObjectRepository.getSnapshotAsStream(source.getSource());
        final var list = stream.toList();
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
        assertThat(list.get(1), is("organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n"));
    }

    @Test
    void should_insert_and_delete_payloads_and_stream_them() throws IOException {
        final var version = nrtmVersionInfoRepository.createInitialVersion(source.getSource(), 0);
        snapshotObjectRepository.insert(version.getId(), 1,1, escapedInetnumString);
        snapshotObjectRepository.insert(version.getId(), 2, 1, escapedOrgString);
        snapshotObjectRepository.delete(2);
        final var stream = snapshotObjectRepository.getSnapshotAsStream(source.getSource());
        final var list = stream.collect(Collectors.toList());
        assertThat(list.get(0), is("inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n"));
    }

    private final String escapedInetnumString = "inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n";
    private final String escapedOrgString = "organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n";

}
