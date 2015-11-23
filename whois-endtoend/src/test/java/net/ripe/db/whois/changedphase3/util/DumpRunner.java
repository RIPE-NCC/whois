package net.ripe.db.whois.changedphase3.util;

public class DumpRunner extends AbstactScenarioRunner {

    public DumpRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Dump";
    }


}
