package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


@Service
public class SnapshotFileStreamer {

    private final SnapshotObjectRepository snapshotObjectRepository;

    SnapshotFileStreamer(
        final SnapshotObjectRepository snapshotObjectRepository
    ) {
        this.snapshotObjectRepository = snapshotObjectRepository;
    }

    public void writeSnapshotAsJson(
        final PublishableSnapshotFile snapshotFile,
        final OutputStream outputStream
    ) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonGenerator jGenerator = mapper.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", snapshotFile.getNrtmVersion());
        jGenerator.writeStringField("type", NrtmDocumentType.SNAPSHOT.lowerCaseName());
        jGenerator.writeStringField("source", snapshotFile.getSource().name());
        jGenerator.writeStringField("session_id", snapshotFile.getSessionID());
        jGenerator.writeNumberField("version", snapshotFile.getVersion());
        jGenerator.writeArrayFieldStart("objects");
        snapshotObjectRepository.streamSnapshots().forEach((payload) -> {
            try {
                jGenerator.writeString(payload);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.close();
    }

}
