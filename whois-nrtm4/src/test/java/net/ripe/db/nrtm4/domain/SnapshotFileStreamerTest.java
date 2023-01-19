package net.ripe.db.nrtm4.domain;

import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.SnapshotObjectIteratorRepository;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;


public class SnapshotFileStreamerTest {

    @Mock
    SnapshotObjectRepository snapshotObjectRepository;
    @Mock
    SnapshotObjectIteratorRepository snapshotObjectIteratorRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void serialize_empty_snapshot_file_to_json() throws IOException {
        final var source = new NrtmSource("TEST");
        final var serializer = new SnapshotFileStreamer(snapshotObjectIteratorRepository);
        final var version = new NrtmVersionInfo(
            23L,
            source,
            26L,
            "abcdef123",
            NrtmDocumentType.SNAPSHOT,
            123455
        );
        final var file = new PublishableSnapshotFile(version);
        final var out = new ByteArrayOutputStream();
        doNothing().when(snapshotObjectIteratorRepository).snapshotCallbackConsumer(source, s -> {
        });
        serializer.writeSnapshotAsJson(file, out);
        out.close();
        final var expected = """
            {
              "nrtm_version" : 4,
              "type" : "snapshot",
              "source" : "TEST",
              "session_id" : "abcdef123",
              "version" : 26,
              "objects" : [ ]
            }""";
        assertThat(out.toString(StandardCharsets.UTF_8), is(expected));
    }

}
