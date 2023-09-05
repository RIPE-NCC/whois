package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
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

    public void writeObjectsAsJsonToOutputStream(
        final NrtmVersionInfo version,
        final Iterator<RpslObject> rpslObjectIterator,
        final OutputStream outputStream
    ) throws IOException {

        outputStream.write(NrtmFileUtil.getNrtmFileRecord(new NrtmVersionRecord(version, NrtmDocumentType.SNAPSHOT)).getBytes());
        while (rpslObjectIterator.hasNext()) {
            outputStream.write(NrtmFileUtil.getNrtmFileRecord(new SnapshotFileRecord(rpslObjectIterator.next())).getBytes());
        }
    }
}
