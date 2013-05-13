package net.ripe.db.whois.common.jdbc.driver;

import java.util.Collections;
import java.util.List;

public class ResultInfo {
    private final List<List<String>> rows;

    public ResultInfo(final List<List<String>> rows) {
        this.rows = Collections.unmodifiableList(rows);
    }

    public List<List<String>> getRows() {
        return rows;
    }
}
