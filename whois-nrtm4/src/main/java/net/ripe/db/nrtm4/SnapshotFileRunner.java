package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;


@Service
public class SnapshotFileRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileRunner.class);

    private final NrtmFileService nrtmFileService;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;

    SnapshotFileRunner(
        final NrtmFileService nrtmFileService,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer
    ) {
        this.nrtmFileService = nrtmFileService;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
    }

    Runner getRunner(
        final PublishableNrtmFile snapshotFile,
        final LinkedBlockingQueue<RpslObjectData> queue
    ) {
        return new Runner(nrtmFileService, snapshotFileRepository, snapshotFileSerializer, snapshotFile, queue);
    }

    private record Runner(NrtmFileService nrtmFileService, SnapshotFileRepository snapshotFileRepository,
                          SnapshotFileSerializer snapshotFileSerializer, PublishableNrtmFile snapshotFile,
                          LinkedBlockingQueue<RpslObjectData> queue) implements Runnable {

        @Override
        public void run() {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
                snapshotFileSerializer.writeObjectQueueAsSnapshot(snapshotFile, queue, gzOut);
                gzOut.close();
                snapshotFile.setHash(nrtmFileService.calculateSha256(bos));
                final Stopwatch stopwatch = Stopwatch.createStarted();
                nrtmFileService.writeToDisk(snapshotFile, bos);
                LOGGER.info("Wrote {} {}/{} to disk in {}", snapshotFile.getSource().getName(), snapshotFile.getSessionID(), snapshotFile.getFileName(), stopwatch);
                snapshotFileRepository.insert(snapshotFile, bos.toByteArray());
                LOGGER.info("Wrote {} to DB {}", snapshotFile.getFileName(), stopwatch);
            } catch (final Exception e) {
                LOGGER.warn("Exception writing snapshot {}", snapshotFile.getSource().getName(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

    }

}
