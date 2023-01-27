package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


@Service
public class SnapshotFileSerializer {

    private final SnapshotObjectReadOnlyDao snapshotObjectReadOnlyDao;

    SnapshotFileSerializer(
        final SnapshotObjectReadOnlyDao snapshotObjectReadOnlyDao
    ) {
        this.snapshotObjectReadOnlyDao = snapshotObjectReadOnlyDao;
    }

    public void writeSnapshotAsJson(
        final PublishableSnapshotFile snapshotFile,
        final OutputStream outputStream
    ) throws IOException {
        final JsonGenerator jGenerator = new ObjectMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
        final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        jGenerator.setPrettyPrinter(pp);
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", snapshotFile.getNrtmVersion());
        jGenerator.writeStringField("type", NrtmDocumentType.SNAPSHOT.lowerCaseName());
        jGenerator.writeStringField("source", snapshotFile.getSource().name());
        jGenerator.writeStringField("session_id", snapshotFile.getSessionID());
        jGenerator.writeNumberField("version", snapshotFile.getVersion());
        jGenerator.writeArrayFieldStart("objects");
        snapshotObjectReadOnlyDao.consumeAllObjects(snapshotFile.getSource(), str -> {
            try {
                jGenerator.writeString(str);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.close();
    }

    static class PrettyPrinterWithObjectOnNewLine extends DefaultPrettyPrinter {

    }
}
