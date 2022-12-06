package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class SnapshotObjectRepositoryIntegrationTest extends AbstractDatabaseHelperIntegrationTest  {

    @Autowired
    private SnapshotObjectRepository snapshotObjectRepository;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    void should_insert_payloads_and_stream_them() throws IOException {
        snapshotObjectRepository.insert(1, escapedInetnumString);
        snapshotObjectRepository.insert(2, escapedOrgString);

        final var outStream = new ByteArrayOutputStream();
        snapshotObjectRepository.streamSnapshot(outStream);
        assertThat(outStream.toString(StandardCharsets.UTF_8), is("[\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\"," +
            "\"organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n\"]"));
    }

    @Test
    void should_insert_and_delete_payloads_and_stream_them() throws IOException {
        snapshotObjectRepository.insert(1, escapedInetnumString);
        snapshotObjectRepository.insert(2, escapedOrgString);
        snapshotObjectRepository.delete(2);
        final var outStream = new ByteArrayOutputStream();
        snapshotObjectRepository.streamSnapshot(outStream);
        assertThat(outStream.toString(StandardCharsets.UTF_8), is("[\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\"]"));
    }

    private final String escapedInetnumString = "\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\"";
    private final String escapedOrgString = "\"organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        Zürich\\naddress:        NETHERLANDS\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n\"";

}
