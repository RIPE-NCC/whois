package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class SplitFilesRunner extends AbstactScenarioRunner {

    public SplitFilesRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Splitfiles";
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

            String oldDumpFile = readFile("./export/dbase/ripe.db.gz");
            String oldSplitFile = readFile("./export/dbase/split/ripe.db.mntner.gz");

            String newdumpFile = readFile("./export/dbase_new/ripe.db.gz");
            String newSplitFile = readFile("./export/dbase_new/split/ripe.db.mntner.gz");

            String internalFile = readFile("./export/internal/split/ripe.db.mntner.gz");

            if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(oldDumpFile, containsString("changed:"));
                assertThat(oldSplitFile, containsString("changed:"));
                assertThat(newdumpFile, containsString("changed:"));
                assertThat(newSplitFile, containsString("changed:"));
                assertThat(internalFile, containsString("changed:"));
            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(oldDumpFile, not(containsString("changed:")));
                assertThat(oldSplitFile, not(containsString("changed:")));
                assertThat(newdumpFile, not(containsString("changed:")));
                assertThat(newSplitFile, not(containsString("changed:")));
                assertThat(internalFile, not(containsString("changed:")));
            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
                assertThat(oldDumpFile, not(containsString("TESTING-MNT")));
                assertThat(oldSplitFile, not(containsString("TESTING-MNT")));
                assertThat(newdumpFile, not(containsString("TESTING-MNT")));
                assertThat(newSplitFile, not(containsString("TESTING-MNT")));
                assertThat(internalFile, not(containsString("TESTING-MNT")));
            }

        } catch (IOException exc) {
            logEvent("Error reading splitfile", exc.toString());
        } finally {
            context.getNrtmServer().stop(true);
        }
    }

    interface Updater {
        void update(final RpslObject obj);
    }

    private String readFile(final String filename) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(filename))));

        StringBuffer sb = new StringBuffer();

        String chunk;
        while ((chunk = in.readLine()) != null) {
            sb.append(chunk);
        }
        return sb.toString();
    }

}
