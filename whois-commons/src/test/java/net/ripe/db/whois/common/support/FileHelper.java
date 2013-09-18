package net.ripe.db.whois.common.support;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileHelper {
    public static String fileToString(final String fileName) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource(fileName).getInputStream()));
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
    }

    public static File addToZipFile(final String zipFilename, final String entryFilename, final String entryContent) throws IOException {
        final File zipFile = File.createTempFile(zipFilename, ".zip");

        final FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        try {
            final ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(fileOutputStream);
            zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(entryFilename));
            IOUtils.write(entryContent.getBytes(), zipArchiveOutputStream);
            zipArchiveOutputStream.closeArchiveEntry();
            zipArchiveOutputStream.finish();
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }

        return zipFile;
    }
}
