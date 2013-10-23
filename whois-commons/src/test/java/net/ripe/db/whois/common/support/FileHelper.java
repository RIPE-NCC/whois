package net.ripe.db.whois.common.support;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class FileHelper {

    public static String fileToString(final String fileName) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource(fileName).getInputStream()));
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
    }

    public static File addToZipFile(final File directory, final String zipFilename, final String entryFilename, final String entryContent) throws IOException {
        final File zipFile = File.createTempFile(zipFilename, ".zip", directory);

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

    public static File addToZipFile(final String zipFilename, final String entryFilename, final String entryContent) throws IOException {
        return addToZipFile(null, zipFilename, entryFilename, entryContent);
    }

    public static File addToGZipFile(final File directory, final String gzipFilename, final String content) throws IOException {
        final File gzipFile = File.createTempFile(gzipFilename, ".gz", directory);

        final FileOutputStream fileOutputStream = new FileOutputStream(gzipFile);
        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream), Charsets.ISO_8859_1));
            writer.write(content);
            writer.flush();
            writer.close();
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }

        return gzipFile;
    }

    public static File addToGZipFile(final String gzipFilename, final String content) throws IOException {
        return addToGZipFile(null, gzipFilename, content);
    }

    public static File addToTextFile(final File directory, final String filename, final String content) throws IOException {
        final File file = File.createTempFile(filename, ".txt", directory);
        Files.write(content.getBytes(), file);
        return file;
    }

    public static File addToTextFile(final String filename, final String content) throws IOException {
        return addToTextFile(null, filename, content);
    }

    public static void delete(final File file) {
        recursiveDelete(file, file);
    }

    private static void recursiveDelete(final File root, final File file) {
        if (!file.isDirectory()) {
            file.delete();
        } else {
            for (File next : file.listFiles()) {
                recursiveDelete(root, next);
            }
            if (!file.equals(root)) {
                // don't delete root directory
                file.delete();
            }
        }
    }


}
