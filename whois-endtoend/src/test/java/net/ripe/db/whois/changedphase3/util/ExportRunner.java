package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ExportRunner extends AbstractScenarioRunner {
    private static final String EXPORT_DIR = "var" + System.getProperty("jvmId") + "/export";

    public ExportRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Export";
    }

    @Override
    public void before(final Scenario scenario) {
        super.before(scenario);
        deleteExportDir();
    }

    @Override
    public void after(final Scenario scenario) {
        deleteExportDir();
        super.after(scenario);
    }

    @Override
    public void create(final Scenario scenario) {
        prepareFromDump(scenario, (RpslObject obj) -> createObjectViaApi(obj));
    }


    @Override
    public void modify(final Scenario scenario) {
        prepareFromDump(scenario, (RpslObject obj) -> modifyObjectViaApi(obj));
    }

    @Override
    public void delete(final Scenario scenario) {
        prepareFromDump(scenario, (RpslObject obj) -> deleteObjectViaApi(obj));
    }

    private void prepareFromDump(final Scenario scenario, final Updater updater) {

        try {
            verifyPreCondition(scenario);

            context.getNrtmServer().start();

            // Perform a create, modify or delete action
            updater.update(objectForScenario(scenario));

            context.getDatabaseTextExport().run();

            String oldDumpFile = readFile(EXPORT_DIR + "/dbase/ripe.db.gz");
            String oldSplitFile = readFile(EXPORT_DIR + "/dbase/split/ripe.db.mntner.gz");

            String newdumpFile = readFile(EXPORT_DIR + "/dbase_new/ripe.db.gz");
            String newSplitFile = readFile(EXPORT_DIR + "/dbase_new/split/ripe.db.mntner.gz");

            String internalFile = readFile(EXPORT_DIR + "/internal/split/ripe.db.mntner.gz");

            if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(oldDumpFile, containsString("TESTING-MNT"));
                assertThat(oldDumpFile, containsString("changed:"));

                assertThat(oldSplitFile, containsString("TESTING-MNT"));
                assertThat(oldSplitFile, containsString("changed:"));

                assertThat(newdumpFile, containsString("TESTING-MNT"));
                assertThat(newdumpFile, containsString("changed:"));

                assertThat(newSplitFile, containsString("TESTING-MNT"));
                assertThat(newSplitFile, containsString("changed:"));

                assertThat(internalFile, containsString("TESTING-MNT"));
                assertThat(internalFile, containsString("changed:"));

            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(oldDumpFile, containsString("TESTING-MNT"));
                assertThat(oldDumpFile, not(containsString("changed:")));

                assertThat(oldSplitFile, containsString("TESTING-MNT"));
                assertThat(oldSplitFile, not(containsString("changed:")));

                assertThat(newdumpFile, containsString("TESTING-MNT"));
                assertThat(newdumpFile, not(containsString("changed:")));

                assertThat(newSplitFile, containsString("TESTING-MNT"));
                assertThat(newSplitFile, not(containsString("changed:")));

                assertThat(internalFile, containsString("TESTING-MNT"));
                assertThat(internalFile, not(containsString("changed:")));

            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
                assertThat(oldDumpFile, not(containsString("TESTING-MNT")));
                assertThat(oldSplitFile, not(containsString("TESTING-MNT")));
                assertThat(newdumpFile, not(containsString("TESTING-MNT")));
                assertThat(newSplitFile, not(containsString("TESTING-MNT")));
                assertThat(internalFile, not(containsString("TESTING-MNT")));
            }

        } catch (IOException exc) {
            System.err.println("Error reading splitfile:" +exc.toString());
            fail();
        } finally {
            context.getNrtmServer().stop(true);
        }
    }

    interface Updater {
        void update(final RpslObject obj);
    }

    private void deleteExportDir() {
        try {
            if (new File(EXPORT_DIR).exists()) {
                // recursively remove  export dir
                FileUtils.deleteDirectory(new File(EXPORT_DIR));
            }
        } catch (IOException exc) {
            logEvent("Error deleting export-dir", exc.toString());
        }
    }

    private String readFile(final String filename) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(filename))));

            StringBuffer sb = new StringBuffer();

            String chunk;
            while ((chunk = in.readLine()) != null) {
                sb.append(chunk);
            }
            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch( IOException exc) {}
            }
        }
    }

}
