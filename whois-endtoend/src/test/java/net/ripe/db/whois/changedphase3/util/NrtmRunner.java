package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class NrtmRunner extends AbstactScenarioRunner {

    public NrtmRunner(final Context context) {
        super(context);
    }

    public String getProtocolName() {
        return "Nrtm";
    }

    public void event(final Scenario scenario) {

        try {
            verifyPreCondition(scenario);

            context.getNrtmServer().start();

            String nrtmCommand = String.format("-g TEST:3:%d-LAST -k", getCurrentOffset());
            AsyncNrtmClient client = new AsyncNrtmClient(NrtmServer.getPort(), nrtmCommand, 2);
            client.start();

            // Perform a create or modify action
            if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
                doCreate(objectForScenario(scenario));
            } else {
                doModify(addRemarks(objectForScenario(scenario)));
            }
            String eventStream = client.end();
            /*
             * Unexpectedly, the stream starts with the previous delete (that can contain a "changed" attribute).
             * TODO: Extract the last object from the stream and verify presence of changed based on scenario
             */

            if (scenario.getMode() == Scenario.Mode.NEW_MODE) {
                assertThat(eventStream, not(containsString("changed:")));
            } else {
                // changed is dummified, so we don't really care about the actual value
            }

        } finally {
            context.getNrtmServer().stop(true);
        }
    }

    private Integer getCurrentOffset() {
        final String currentStatusResp = TelnetWhoisClient.queryLocalhost(NrtmServer.getPort(), "-q sources");
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
