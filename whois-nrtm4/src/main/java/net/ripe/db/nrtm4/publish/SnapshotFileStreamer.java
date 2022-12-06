package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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

    public void processSnapshot(
        final PublishableSnapshotFile snapshotFile,
        final OutputStream outputStream
    ) throws IOException {
        final JsonFactory jfactory = new JsonFactory();
        final JsonGenerator jGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8);
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", snapshotFile.getNrtmVersion());
        jGenerator.writeStringField("type", snapshotFile.getType().toString());
        jGenerator.writeStringField("source", snapshotFile.getSource().toString());
        jGenerator.writeNumberField("version", snapshotFile.getVersion());
        jGenerator.writeArrayFieldStart("objects");
        snapshotObjectRepository.streamSnapshot(outputStream);
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
    }
}
