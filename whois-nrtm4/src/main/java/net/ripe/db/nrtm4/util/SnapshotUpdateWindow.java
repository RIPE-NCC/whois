package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.persist.SnapshotFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class SnapshotUpdateWindow {

    private final Date startTime;
    private final Date endTime;

    SnapshotUpdateWindow(@Value("${nrtm.snapshot.window:00:00-04:00}") final String snapshotWindowString) {
        // parse it into a representation that includes midnight -- so 10:00-06:00 starts at 10am and ends at 6am the following day
        final Pattern PATTERN = Pattern.compile("(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})");
        final Matcher matcher = PATTERN.matcher(snapshotWindowString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse: " + snapshotWindowString + " from property nrtm.snapshot.window");
        }
        startTime = new Date();
        endTime = new Date();
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
