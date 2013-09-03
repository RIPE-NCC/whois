package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;

// TODO: drop accept(), not used at all (if yes, just move as an if () at the beginning of process()
public interface LoggedUpdateProcessor<T extends LoggedUpdate> {
    boolean accept(T loggedUpdate);

    void process(T loggedUpdate, String contents);
}

