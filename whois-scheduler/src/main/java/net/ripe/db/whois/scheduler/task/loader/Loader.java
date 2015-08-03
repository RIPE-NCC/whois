package net.ripe.db.whois.scheduler.task.loader;

import java.util.List;

public interface Loader {

    String loadSplitFiles(String... filenames);
    void resetDatabase();
    void validateFiles(List<String> filenames);
}
