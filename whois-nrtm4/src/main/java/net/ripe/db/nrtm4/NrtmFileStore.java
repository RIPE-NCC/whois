package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


@Service
public class NrtmFileStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileService.class);
    private static final int OUTPUT_BUFFER_SIZE = 1024;

    private final String path;

    NrtmFileStore(
        @Value("${nrtm.file.path:/tmp}") final String path
    ) {
        this.path = path;
    }

    boolean checkIfFileExists(final String sessionId, final String name) {
        return NrtmFileUtil.checkIfFileExists(path, sessionId, name);
    }

    void streamFromFile(final String sessionId, final String name, final OutputStream out) throws IOException {
        try (final FileInputStream fis = getFileInputStream(sessionId, name)) {
            fis.transferTo(out);
        }
    }

    public OutputStream getFileOutputStream(final String sessionId, final String name) throws FileNotFoundException {
        final File dir = new File(path, sessionId);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                LOGGER.error("Could not create directory for sessionID: " + sessionId);
                throw new RuntimeException("Failed to create NRTM directory " + path + " " + sessionId);
            }
        }
        return new BufferedOutputStream(new FileOutputStream(new File(dir, name)), OUTPUT_BUFFER_SIZE);
    }

    public FileInputStream getFileInputStream(final String sessionId, final String name) throws FileNotFoundException {
        return NrtmFileUtil.getFileInputStream(path, sessionId, name);
    }

}
