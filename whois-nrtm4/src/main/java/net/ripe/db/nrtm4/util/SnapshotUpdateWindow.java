package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.dao.SnapshotFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SnapshotUpdateWindow {

    SnapshotUpdateWindow(@Value("${nrtm.snapshot.window:00:00-04:00}") final String snapshotWindowString) {
        // parse it into a representation that includes midnight -- so 10:00-06:00 starts at 10am and ends at 6am the following day

    }

    long windowLength() {
        return 0;
    }

    public boolean fileShouldBeUpdated(final SnapshotFile snapshotFile) {
        // if file age is older than the window duration, generate a new one
        // e.g. if the snapshot generation window is 4 hours and the file is over 4 hours old, it should be regenerated
        return false;
    }

}
