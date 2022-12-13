package net.ripe.db.nrtm4.publish;

import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class SnapshotFileStreamerTest {

    @Mock
    SnapshotObjectRepository snapshotObjectRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void serialize_empty_snapshot_file_to_json() throws IOException {
//        final var source = new NrtmSource("TEST");
//        final var serializer = new SnapshotFileStreamer(snapshotObjectRepository);
//        final var version = new NrtmVersionInfo(
//            23L,
//            source,
//            26L,
//            "abcdef123",
//            NrtmDocumentType.SNAPSHOT,
//            123455
//        );
//        final var file = new PublishableSnapshotFile(version);
//        final var out = new ByteArrayOutputStream();
//        doNothing().when(snapshotObjectRepository).streamSnapshot(out);
//        serializer.processSnapshot(file, out);
//        out.close();
//        assertThat(out.toString(StandardCharsets.UTF_8), is("{\"nrtm_version\":4,\"type\":\"snapshot\",\"source\":\"TEST\",\"version\":26,\"objects\":[]}"));
//    }

}
