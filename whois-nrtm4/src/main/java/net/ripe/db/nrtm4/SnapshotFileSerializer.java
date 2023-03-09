package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;


@Service
public class SnapshotFileSerializer {

    private final boolean isPrettyPrintSnapshots;

    SnapshotFileSerializer(
        @Value("${nrtm.prettyprint.snapshots:false}") final boolean isPrettyPrintSnapshots
    ) {
        this.isPrettyPrintSnapshots = isPrettyPrintSnapshots;
    }

    public void writeObjectQueueAsSnapshot(
        final PublishableNrtmFile snapshotFile,
        final Iterator<RpslObject> rpslObjectSupplier,
        final OutputStream outputStream
    ) throws IOException {
        final JsonGenerator jGenerator = new ObjectMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
        if (isPrettyPrintSnapshots) {
            final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            jGenerator.setPrettyPrinter(pp);
        }
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", snapshotFile.getNrtmVersion());
        jGenerator.writeStringField("type", NrtmDocumentType.SNAPSHOT.lowerCaseName());
        jGenerator.writeStringField("source", snapshotFile.getSource().getName().toString());
        jGenerator.writeStringField("session_id", snapshotFile.getSessionID());
        jGenerator.writeNumberField("version", snapshotFile.getVersion());
        jGenerator.writeArrayFieldStart("objects");
        while (rpslObjectSupplier.hasNext()) {
            jGenerator.writeString(rpslObjectSupplier.next().toString());
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.close();
    }

}
