package net.ripe.db.whois.api.wsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

class DailyLogFolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyLogFolder.class);

    private static final Pattern DAILY_LOG_FOLDER_PATTERN = Pattern.compile("(\\d{8})(?:\\.tar)?");
    private static final Pattern UPDATE_LOG_FOLDER_PATTERN = Pattern.compile("(\\d{6})\\..*");
    private static final Pattern TAR_ENTRY_PATH_PATTERN = Pattern.compile("^\\./(.*)");

    private final File folder;
    private final String folderName;

    public DailyLogFolder(final File folder) {
        final Matcher folderNameMatcher = DAILY_LOG_FOLDER_PATTERN.matcher(folder.getName());
        if (!folderNameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid folder: " + folder.getName());
        }

        folderName = folderNameMatcher.group(1);

        if (folder.exists()) {
            this.folder = folder;
        } else {
            final File tarFile = new File(folder.getPath() + ".tar");
            if (!tarFile.exists() || !tarFile.isFile()) {
                throw new IllegalArgumentException("Not existing folder: " + folderName);
            }

            this.folder = tarFile;
        }
    }

    public void processLoggedFiles(final LoggedFilesProcessor loggedFilesProcessor) {
        if (folder.isFile()) {
            FileInputStream is = null;

            try {
                is = new FileInputStream(folder);
                final TarArchiveInputStream tarInput = new TarArchiveInputStream(new BufferedInputStream(is));

                for (TarArchiveEntry tarEntry = tarInput.getNextTarEntry(); tarEntry != null; tarEntry = tarInput.getNextTarEntry()) {
                    final String tarEntryName = tarEntry.getName();

                    try {
                        final String entryPath = String.format("%s%s%s", folderName, File.separator, TAR_ENTRY_PATH_PATTERN.matcher(tarEntryName).replaceAll("$1"));
                        if (tarEntry.isFile() && LoggedUpdateInfo.isLoggedUpdateInfo(entryPath)) {
                            final LoggedUpdateInfo loggedUpdateInfo = LoggedUpdateInfo.parse(entryPath);
                            if (loggedFilesProcessor.accept(loggedUpdateInfo)) {
                                loggedFilesProcessor.process(loggedUpdateInfo, getContents(tarInput, tarEntry.getSize()));
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.warn("IO exception processing entry: {} in file: {}", tarEntryName, folder.getCanonicalPath(), e);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Unexpected exception processing entry: {} in file: {}", tarEntryName, folder.getCanonicalPath(), e);
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Exception processing folder: {}", folder.getAbsolutePath(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            final File[] updateLogFolders = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathName) {
                    return pathName.isDirectory() && UPDATE_LOG_FOLDER_PATTERN.matcher(pathName.getName()).matches();
                }
            });

            final SortedMap<LoggedUpdateInfo, File> loggedUpdateInfos = Maps.newTreeMap();

            for (final File updateLogFolder : updateLogFolders) {
                final File[] files = updateLogFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        return pathName.isFile() && LoggedUpdateInfo.isLoggedUpdateInfo(pathName.getName());
                    }
                });

                for (final File file : files) {
                    loggedUpdateInfos.put(LoggedUpdateInfo.parse(file.getPath()), file);
                }
            }

            for (final Map.Entry<LoggedUpdateInfo, File> updateInfoFileEntry : loggedUpdateInfos.entrySet()) {
                final LoggedUpdateInfo loggedUpdateInfo = updateInfoFileEntry.getKey();
                final File file = updateInfoFileEntry.getValue();

                if (loggedFilesProcessor.accept(loggedUpdateInfo)) {
                    FileInputStream is = null;

                    try {
                        is = new FileInputStream(file);
                        loggedFilesProcessor.process(loggedUpdateInfo, getContents(new BufferedInputStream(is), file.length()));
                    } catch (IOException e) {
                        LOGGER.warn("IO exception processing file: {}", file.getAbsolutePath(), e);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Unexpected exception processing file: {}", file.getAbsolutePath(), e);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DailyLogFolder that = (DailyLogFolder) o;
        return folderName.equals(that.folderName);
    }

    @Override
    public int hashCode() {
        return folderName.hashCode();
    }

    @Override
    public String toString() {
        return folderName;
    }

    private String getContents(final InputStream input, final long size) throws IOException {
        int remaining = (int) size;
        final byte[] buffer = new byte[remaining];
        while (remaining != 0) {
            remaining -= input.read(buffer, buffer.length - remaining, remaining);
        }

        GZIPInputStream in = null;
        InputStreamReader reader = null;

        try {
            in = new GZIPInputStream(new ByteArrayInputStream(buffer));
            reader = new InputStreamReader(in);

            return FileCopyUtils.copyToString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(in);
        }
    }

    interface LoggedFilesProcessor {
        boolean accept(LoggedUpdateInfo loggedUpdateInfo);

        void process(LoggedUpdateInfo loggedUpdateInfo, String contents);
    }

    static List<DailyLogFolder> getDailyLogFolders(final File logDir, final String fromDailyLogFolder) {
        final File[] dailyLogFolders = logDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                final Matcher dailyLogFolderMatcher = DAILY_LOG_FOLDER_PATTERN.matcher(pathname.getName());
                return dailyLogFolderMatcher.matches() && dailyLogFolderMatcher.group(1).compareTo(fromDailyLogFolder) >= 0;
            }
        });

        Arrays.sort(dailyLogFolders, new FileComparator());

        final List<DailyLogFolder> result = Lists.newArrayListWithExpectedSize(dailyLogFolders.length);
        for (final File dailyLogFolder : dailyLogFolders) {
            result.add(new DailyLogFolder(dailyLogFolder));
        }

        return result;
    }

    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(final File file1, final File file2) {
            return file1.getName().compareTo(file2.getName());
        }
    }
}
