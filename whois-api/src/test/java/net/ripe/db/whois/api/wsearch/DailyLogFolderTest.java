package net.ripe.db.whois.api.wsearch;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class DailyLogFolderTest {
    @Test(expected = IllegalArgumentException.class)
    public void log_invalid_folder() throws IOException {
        new DailyLogFolder(getLogFolder("/log"));
    }

    @Test
    public void process_logs_in_folder() throws IOException {
        final Map<LoggedUpdateInfo, String> logs = getLoggedUpdateInfos("/log/update/20130306");

        final Set<LoggedUpdateInfo> updateInfos = logs.keySet();
        assertThat(updateInfos, hasSize(6));
        final Iterator<LoggedUpdateInfo> updateInfoIterator = updateInfos.iterator();

        final String date = "20130306";
        final String updateFolder = "123623.428054357.0.1362569782886.JavaMail.andre";
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.AUDIT, date, updateFolder, "000.audit.xml.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.UPDATE, date, updateFolder, "001.msg-in.txt.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.ACK, date, updateFolder, "002.msg-out.txt.gz");
    }

    @Test
    public void process_logs_in_tarfile() throws IOException {
        final Map<LoggedUpdateInfo, String> logs = getLoggedUpdateInfos("/log/update/20130305.tar");

        final Set<LoggedUpdateInfo> updateInfos = logs.keySet();
        assertThat(updateInfos, hasSize(7));
        final Iterator<LoggedUpdateInfo> updateInfoIterator = updateInfos.iterator();

        final String date = "20130305";

        String updateFolder = "114444.1975357211.0.1362480283923.JavaMail.andre";
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.AUDIT, date, updateFolder, "000.audit.xml.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.UPDATE, date, updateFolder, "001.msg-in.txt.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.ACK, date, updateFolder, "002.msg-out.txt.gz");

        updateFolder = "140319.syncupdate_127.0.0.1_1362488599134839000";
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.AUDIT, date, updateFolder, "000.audit.xml.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.UPDATE, date, updateFolder, "001.msg-in.txt.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.ACK, date, updateFolder, "002.msg-out.txt.gz");
        validateUpdateInfo(updateInfoIterator.next(), LoggedUpdateInfo.Type.EMAIL, date, updateFolder, "003.msg-out.txt.gz");
    }

    @Test
    public void daily_log_folders() throws IOException {
        final List<DailyLogFolder> dailyLogFolders = DailyLogFolder.getDailyLogFolders(getLogFolder("/log/update"), "");
        assertThat(dailyLogFolders, hasSize(3));
        assertThat(dailyLogFolders, contains(
                new DailyLogFolder(getLogFolder("/log/update/20120816.tar")),
                new DailyLogFolder(getLogFolder("/log/update/20130305.tar")),
                new DailyLogFolder(getLogFolder("/log/update/20130306"))
        ));
    }

    @Test
    public void daily_log_folders_filtered() throws IOException {
        final List<DailyLogFolder> dailyLogFolders = DailyLogFolder.getDailyLogFolders(getLogFolder("/log/update"), "20130306");
        assertThat(dailyLogFolders, hasSize(1));
        assertThat(dailyLogFolders, contains(
                new DailyLogFolder(getLogFolder("/log/update/20130306"))
        ));
    }

    private Map<LoggedUpdateInfo, String> getLoggedUpdateInfos(final String path) throws IOException {
        final Map<LoggedUpdateInfo, String> logs = Maps.newLinkedHashMap();

        new DailyLogFolder(getLogFolder(path)).processLoggedFiles(new DailyLogFolder.LoggedFilesProcessor() {
            @Override
            public boolean accept(final LoggedUpdateInfo loggedUpdateInfo) {
                return true;
            }

            @Override
            public void process(final LoggedUpdateInfo loggedUpdateInfo, String contents) {
                logs.put(loggedUpdateInfo, contents);
            }
        });
        return logs;
    }

    private static void validateUpdateInfo(final LoggedUpdateInfo updateInfo, final LoggedUpdateInfo.Type type, final String date, final String updateFolder, final String filename) {
        assertThat(updateInfo.getType(), is(type));
        assertThat(updateInfo.getFilename(), is(filename));
        assertThat(updateInfo.getLoggedUpdateId().getDailyLogFolder(), is(date));
        assertThat(updateInfo.getLoggedUpdateId().getUpdateFolder(), is(updateFolder));
    }

    private static File getLogFolder(final String path) throws IOException {
        return new ClassPathResource(path).getFile();
    }
}
