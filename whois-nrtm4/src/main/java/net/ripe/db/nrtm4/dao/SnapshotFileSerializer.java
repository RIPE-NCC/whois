package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class SnapshotFileSerializer {

    private final boolean isPrettyPrintSnapshots;
    private final Dummifier dummifierNrtm;

    SnapshotFileSerializer(
        @Value("${nrtm.prettyprint.snapshots:false}") final boolean isPrettyPrintSnapshots,
        final Dummifier dummifierNrtm
    ) {
        this.isPrettyPrintSnapshots = isPrettyPrintSnapshots;
        this.dummifierNrtm = dummifierNrtm;
    }

    public void writeObjectQueueAsSnapshot(
        final PublishableSnapshotFile snapshotFile,
        final LinkedBlockingQueue<RpslObjectData> queue,
        final OutputStream outputStream
    ) throws IOException, InterruptedException {
        final JsonGenerator jGenerator = new ObjectMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
        if (isPrettyPrintSnapshots) {
            final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            jGenerator.setPrettyPrinter(pp);
        }
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", snapshotFile.getNrtmVersion());
        jGenerator.writeStringField("type", NrtmDocumentType.SNAPSHOT.lowerCaseName());
        jGenerator.writeStringField("source", snapshotFile.getSourceModel().getName().toString());
        jGenerator.writeStringField("session_id", snapshotFile.getSessionID());
        jGenerator.writeNumberField("version", snapshotFile.getVersion());
        jGenerator.writeArrayFieldStart("objects");
        for (RpslObjectData rpslObjectData = queue.poll(); ; rpslObjectData = queue.poll()) {
            if (rpslObjectData == null) {
                TimeUnit.MILLISECONDS.sleep(10);
                continue;
            }
            if (rpslObjectData.objectId() == 0) {
                break;
            }
            final RpslObject rpsl = rpslObjectData.rpslObject();
            if (!dummifierNrtm.isAllowed(NRTM_VERSION, rpsl)) {
                continue;
            }
            final String rpslStr = dummifierNrtm.dummify(NRTM_VERSION, rpslObjectData.rpslObject()).toString();
            jGenerator.writeString(rpslStr);
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.close();
    }

}
