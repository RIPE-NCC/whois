package net.ripe.db.whois.internal.logsearch;

import net.ripe.db.whois.internal.logsearch.logformat.LoggedUpdate;

public interface LoggedUpdateProcessor<T extends LoggedUpdate> {
    boolean accept(T loggedUpdate);

    void process(T loggedUpdate, String contents);
}

