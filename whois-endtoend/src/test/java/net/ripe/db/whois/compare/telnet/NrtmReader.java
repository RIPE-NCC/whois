package net.ripe.db.whois.compare.telnet;

import com.google.common.base.Joiner;
import net.ripe.db.whois.compare.common.QueryReader;
import org.springframework.core.io.ByteArrayResource;

public class NrtmReader extends QueryReader {

    private static final String[] QUERIES_TEMPLATE = {
            "-b RIPE:3:1-LAST", //invalid parameter
            "-g UNKNOWN:3:1-LAST", //invalid source
            "-g RIPE:5:1-LAST", //invalid protocol

            "-g RIPE:3:1-LAST",  //invalid range
            "-g RIPE:3:3000000:LAST",  //very old

            "-g RIPE:3:<LATEST>-LAST", //expect 1 entry
            "-g RIPE:3:<LATEST>-<LATEST>", //expect 1 entry
            "-g RIPE:3:<LATEST-10>-LAST", //expect something between 1 and 10 results
            "-g RIPE:3:<LATEST-10>-<LATEST-5>",

            "-g RIPE:2:<LATEST>-LAST", //expect 1 entry
            "-g RIPE:2:<LATEST>-<LATEST>", //expect 1 entry
            "-g RIPE:2:<LATEST-10>-LAST", //expect something between 1 and 10 results
            "-g RIPE:2:<LATEST-10>-<LATEST-5>"
    };

    public NrtmReader(long latestSerialId) {
        super(new ByteArrayResource(generateQueries(latestSerialId).getBytes()));
    }

    @Override
    protected String getQuery(String line) {
        return line;
    }

    public static String generateQueries(long latestSerialId) {
        return Joiner.on('\n').join(QUERIES_TEMPLATE)
                .replaceAll("<LATEST>", String.valueOf(latestSerialId))
                .replaceAll("<LATEST-5>", String.valueOf(latestSerialId-5))
                .replaceAll("<LATEST-10>", String.valueOf(latestSerialId - 10));
    }

}
