package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class NrtmRunner extends AbstractScenarioRunner {

    public NrtmRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Nrtm";
    }

    @Override
    public void create(final Scenario scenario) {
        triggerNrtmEvent(scenario, (RpslObject obj) -> createObjectViaApi(obj));
    }

    @Override
    public void modify(final Scenario scenario) {
        triggerNrtmEvent(scenario, (RpslObject obj) -> modifyObjectViaApi(obj));
    }

    @Override
    public void delete(final Scenario scenario) {
        triggerNrtmEvent(scenario, (RpslObject obj) -> deleteObjectViaApi(obj));
    }

    private void triggerNrtmEvent(final Scenario scenario, final Updater updater) {

            verifyPreCondition(scenario);

            String nrtmCommand = String.format("-g TEST:3:%d-LAST -k", getCurrentOffset());
            AsyncNrtmClient client = new AsyncNrtmClient(context.getNrtmServer().getPort(), nrtmCommand, 2);
            client.start();

            // Perform a create, modify or delete action
            updater.update(objectForScenario(scenario));

            String eventStream = client.end();
            /*
             * Unexpectedly, the stream starts with the previous delete (that can contain a "changed" attribute).
             * TODO: Extract the last object from the stream and verify presence of changed based on scenario
             */

            logEvent("nrtm-event", eventStream);

            if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(eventStream, containsString("changed:"));
            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(eventStream, not(containsString("changed:")));
            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
                /* no event received whatsoever */
                assertThat(eventStream, not(containsString("mntner:")));
            }

    }

    private Integer getCurrentOffset() {
        final String currentStatusResp = TelnetWhoisClient.queryLocalhost(context.getNrtmServer().getPort(), "-q sources");
        for (String line : currentStatusResp.split("\n")) {
            if (line.startsWith("TEST:")) {
                return Integer.parseInt(line.split("-")[1]);
            }
        }
        return 1;
    }

    private static class AsyncNrtmClient {

        private final FutureTask<String> task;

        public AsyncNrtmClient(final int port, final String query, final int timeout) {
            task = new FutureTask<>(new Callable<String>() {
                public String call() {
                    String result = TelnetWhoisClient.queryLocalhost(port, query, timeout * 1000);
                    return result;
                }
            });
        }

        public void start() {
            Executor ex = Executors.newFixedThreadPool(1);
            ex.execute(task);
        }

        public String end() {
            try {
                return task.get();
            } catch (Exception e) {
                return null;
            }
        }
    }

}
