package net.ripe.db.whois.logsearch;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.support.FileHelper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class LogFileHelper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");

    // create logfile (in todays directory)

    public static File createLogFile(final File parent, final String content) throws IOException {
        return createLogFile(parent, getDate(), getTime(), content);
    }

    public static File createLogFile(final File parent, final String date, final String time, final String content) throws IOException {
        return createLogFile(parent, date, time, Double.toString(Math.random()), content);
    }

    public static File createLogFile(final File parent, final String date, final String time, final String random, final String content) throws IOException {
        return createLogFile(parent, date, time, random, content, "001.msg-in.txt.gz");
    }

    public static File createLogFile(final File parent, final String date, final String time, final String random, final String content, final String filename) throws IOException {
        final File directory = createLogDirectory(parent, date, time, random);

        final File logFile = new File(directory, filename);
        final FileOutputStream fileOutputStream = new FileOutputStream(logFile);

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream), Charsets.ISO_8859_1));
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return logFile;
    }

    // create YYYYMMDD.tar containing logfile /HHMMSS.<random>/001.msg-in.txt.gz

    public static File createTarredLogFile(final File parent, final String date, final String time, final String content) throws IOException {
        return createTarredLogFile(parent, date, time, Double.toString(Math.random()), content);
    }

    public static File createTarredLogFile(final File parent, final String date, final String time, final String random, final String content) throws IOException {
        return createTarredLogFile(parent, date, time, random, "001.msg-in.txt.gz", content);
    }

    public static File createTarredLogFile(final File parent, final String date, final String time, final String random, final String filename, final String content) throws IOException {
        final File tarFile = new File(parent, String.format("%s.tar", date));
        try (TarArchiveOutputStream outputStream = new TarArchiveOutputStream(new FileOutputStream(tarFile))) {

            final byte[] gzippedData = gzip(content);
            final TarArchiveEntry archiveEntry = new TarArchiveEntry(String.format("%s.%s/%s", time, random, filename));

            archiveEntry.setSize(gzippedData.length);
            outputStream.putArchiveEntry(archiveEntry);
            outputStream.write(gzippedData);
            outputStream.closeArchiveEntry();

            return tarFile;
        }
    }

    // create update log updlog.YYYYMMDD.bz2

    public static File createBzippedLogFile(final File parent, final String date, final String content) throws IOException {
        final File bz2file = new File(parent, String.format("updlog.%s.bz2", date));
        try (BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(new FileOutputStream(bz2file))) {
            outputStream.write(content.getBytes());
            return bz2file;
        }
    }

    private static byte[] gzip(final String content) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(content.getBytes());
        gzipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static File createLogDirectory(final File parent) {
        return createLogDirectory(parent, getDate());
    }

    public static File createLogDirectory(final File parent, final String name) {
        final File child = new File(String.format("%s/%s", parent.getAbsolutePath(), name));
        child.mkdirs();
        return child;
    }

    public static File createLogDirectory(final File parent, final String date, final String time, final String random) {
        return createLogDirectory(parent, String.format("%s/%s.%s", date, time, random));
    }

    public static void deleteLogs(final File file) {
        FileHelper.delete(file);
    }

    public static String getDate() {
        return DATE_FORMAT.print(DateTime.now());
    }

    public static String getTime() {
        return TIME_FORMAT.print(DateTime.now());
    }

    public static String getAbsolutePath(final String relativePath) throws IOException {
        return new ClassPathResource(relativePath).getFile().getAbsolutePath();
    }
}
