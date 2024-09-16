package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

public class ExportRunner extends AbstractScenarioRunner {

    private static final String EXPORT_DIR;
    static {
        final String jvmId = System.getProperty("jvmId");
        EXPORT_DIR = "var" + (jvmId != null ? jvmId : "") + "/export";
    }

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

            final String dumpFile = readFile(EXPORT_DIR + "/public/test.db.gz");
            final String splitFile = readFile(EXPORT_DIR + "/public/split/test.db.mntner.gz");
            final String internalFile = readFile(EXPORT_DIR + "/internal/split/test.db.mntner.gz");

            if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(dumpFile, containsString("TESTING-MNT"));
                assertThat(dumpFile, containsString("changed:"));

                assertThat(splitFile, containsString("TESTING-MNT"));
                assertThat(splitFile, containsString("changed:"));

                assertThat(internalFile, containsString("TESTING-MNT"));
                assertThat(internalFile, containsString("changed:"));

            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(dumpFile, containsString("TESTING-MNT"));
                assertThat(dumpFile, not(containsString("changed:")));

                assertThat(splitFile, containsString("TESTING-MNT"));
                assertThat(splitFile, not(containsString("changed:")));

                assertThat(internalFile, containsString("TESTING-MNT"));
                assertThat(internalFile, not(containsString("changed:")));

            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
                assertThat(dumpFile, not(containsString("TESTING-MNT")));
                assertThat(splitFile, not(containsString("TESTING-MNT")));
                assertThat(internalFile, not(containsString("TESTING-MNT")));
            }

        } catch (IOException exc) {
            System.err.println("Error reading splitfile: " +exc.getClass().getName() + ": " + exc.getMessage());
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
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(filename))))) {
            final StringBuilder sb = new StringBuilder();
            String chunk;
            while ((chunk = in.readLine()) != null) {
                sb.append(chunk);
            }
            return sb.toString();
        }
    }

}
