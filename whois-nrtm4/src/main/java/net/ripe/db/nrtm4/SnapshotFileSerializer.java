package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class SnapshotFileSerializer {

    private final boolean isPrettyPrintSnapshots;
    private final Set<ObjectType> objectTypes;

    SnapshotFileSerializer(
        @Value("${nrtm.prettyprint.snapshots:false}") final boolean isPrettyPrintSnapshots,
        @Value("#{'${nrtmv4.objectTypes:}'.split(',')}") final Set<ObjectType> objectTypes
        ) {
        this.isPrettyPrintSnapshots = isPrettyPrintSnapshots;
        this.objectTypes = objectTypes.isEmpty() ? EnumSet.allOf(ObjectType.class) : objectTypes;
    }

    public void writeObjectsAsJsonToOutputStream(
        final NrtmVersionInfo version,
        final Iterator<RpslObject> rpslObjectIterator,
        final OutputStream outputStream
    ) throws IOException {
        final JsonGenerator jGenerator = new ObjectMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
        if (isPrettyPrintSnapshots) {
            final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            jGenerator.setPrettyPrinter(pp);
        }
        jGenerator.writeStartObject();
        jGenerator.writeNumberField("nrtm_version", NRTM_VERSION);
        jGenerator.writeStringField("type", NrtmDocumentType.SNAPSHOT.lowerCaseName());
        jGenerator.writeStringField("source", version.source().getName().toString());
        jGenerator.writeStringField("session_id", version.sessionID());
        jGenerator.writeNumberField("version", version.version());
        jGenerator.writeArrayFieldStart("objects");
        while (rpslObjectIterator.hasNext()) {
            final RpslObject rpslObject = rpslObjectIterator.next();
            if(objectTypes.contains(rpslObject.getType())) {
                jGenerator.writeString(rpslObject.toString());
            }
        }
        jGenerator.writeEndArray();
        jGenerator.writeEndObject();
        jGenerator.close();
    }

}
