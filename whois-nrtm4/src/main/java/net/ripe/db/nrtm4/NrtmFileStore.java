package net.ripe.db.nrtm4;

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

    private final String path;

    NrtmFileStore(
        @Value("${nrtm.file.path:/tmp}") final String path
    ) {
        this.path = path;
    }

    boolean checkIfFileExists(final String name) {
        return new File(path, name).exists();
    }

    void streamFromFile(final String name, final OutputStream out) throws IOException {
        try (final FileInputStream fis = getFileInputStream(name)) {
            fis.transferTo(out);
        }
    }

    public void storeFile(final String name, final String payload) throws IOException {
        try (final FileOutputStream fos = getFileOutputStream(name)) {
            fos.write(payload.getBytes(StandardCharsets.UTF_8));
        }
    }

    public FileOutputStream getFileOutputStream(final String name) throws FileNotFoundException {
        return new FileOutputStream(new File(path, name));
    }

    public FileInputStream getFileInputStream(final String name) throws FileNotFoundException {
        return new FileInputStream(new File(path, name));
    }

}
