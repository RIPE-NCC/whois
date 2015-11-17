package net.ripe.db.whois.changedphase3.util;

public class RunnerProvider {
    public static ScenarioRunner getRunnerForProtocol(final Scenario.Protocol protocol, Context context) {
        ScenarioRunner runner = null;
        switch (protocol) {
            case REST___:
                runner = new RestRunner(context);
                break;

            case SYNCUPD:
                runner = new SyncUpdateRunner(context);
                break;

            case MAILUPD:
                runner = new MailUpdateRunner(context);
                break;

            case TELNET_:
                runner = new TelnetRunner(context);
                break;

            case NRTM___:
                runner = new NrtmRunner(context);
                break;

        }

        return runner;
    }
}
