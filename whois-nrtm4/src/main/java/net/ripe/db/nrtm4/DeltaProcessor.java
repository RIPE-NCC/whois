package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFile;
import net.ripe.db.nrtm4.persist.DeltaFileDao;
import org.springframework.stereotype.Service;


@Service
public class DeltaProcessor {

    private final DeltaFileDao deltaFileDao;

    DeltaProcessor(
            final DeltaFileDao deltaFileDao
    ) {
        this.deltaFileDao = deltaFileDao;
    }

    public int findLastSerialId() {
        final DeltaFile deltaFile = deltaFileDao.findLastChange();
        return deltaFile.getLastSerialId();
    }

    void saveDeltas() {
        // save these changes to the delta_files table

    }
}
