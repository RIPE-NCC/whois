package net.ripe.db.nrtm4;

import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


@Service
public class NrtmFileStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileService.class);

    private final String path;

    NrtmFileStore(
        @Value("${nrtm.file.path:/tmp}") final String path
    ) {
        this.path = path;
    }

    boolean checkIfFileExists(final String sessionId, final String name) {
        final File dir = new File(path, sessionId);
        return dir.exists() && new File(dir, name).exists();
    }

    void streamFromFile(final String sessionId, final String name, final OutputStream out) throws IOException {
        try (final FileInputStream fis = getFileInputStream(sessionId, name)) {
            fis.transferTo(out);
        }
    }

    public void storeFile(final String sessionId, final String name, final String payload) throws IOException {
        try (final FileOutputStream fos = getFileOutputStream(sessionId, name)) {
            fos.write(payload.getBytes(StandardCharsets.UTF_8));
        }
    }

    public FileOutputStream getFileOutputStream(final String sessionId, final String name) throws FileNotFoundException {
        final File dir = new File(path, sessionId);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                LOGGER.error("Could not create directory for sessionID: " + sessionId);
                throw new RuntimeException("Failed to create NRTM directory " + path + " " + sessionId);
            }
        }
        return new FileOutputStream(new File(dir, name));
    }

    public FileInputStream getFileInputStream(final String sessionId, final String name) throws FileNotFoundException {
        final File dir = new File(path, sessionId);
        return new FileInputStream(new File(dir, name));
    }

}
