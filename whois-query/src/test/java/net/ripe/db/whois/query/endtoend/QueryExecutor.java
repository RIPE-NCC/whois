package net.ripe.db.whois.query.endtoend;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.io.ByteArrayInput;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.ByteArrayContains;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryExecutor {
    private static final byte[] DOUBLE_NEWLINE = new byte[]{'\n', '\n'};
    private static final byte[] STUPID_NEWLINE = new byte[]{'\r'};

    private final QueryExecutorConfiguration configuration;

    public QueryExecutor(final QueryExecutorConfiguration configuration) throws UnknownHostException {
        this.configuration = configuration;
    }

    public List<ResponseObject> getWhoisResponse(final String query) throws IOException {
        final DummyWhoisClient client = new DummyWhoisClient(configuration.getHost(), configuration.getQueryPort());
        final String response;

        final Stopwatch stopWatch = new Stopwatch().start();
        try {
            response = client.sendQuery(query);
        } finally {
            stopWatch.stop();
        }

        return parseWhoisResponseIntoRpslObjects(query, response);
    }

    private List<ResponseObject> parseWhoisResponseIntoRpslObjects(final String query, String response) throws IOException {
        byte[] whoisServerOutput = response.getBytes();

        List<ResponseObject> ret = Lists.newArrayList();
        int begin = 0;

        do {
            int end = ByteArrayContains.indexOfIgnoring(whoisServerOutput, DOUBLE_NEWLINE, begin, STUPID_NEWLINE);
            if (end < 0) {
                end = whoisServerOutput.length;
            }

            // skip ending whitespace
            int realend = end;
            while (realend > begin && Character.isWhitespace(whoisServerOutput[--realend])) ;
            realend++;

            // process paragraph
            final ByteArrayInput input = new ByteArrayInput(whoisServerOutput, begin, realend - begin);
            final byte[] bytes = ByteStreams.toByteArray(input);
            if (whoisServerOutput[begin] == '%' || query.startsWith("-t") || query.startsWith("-v")) {
                ret.add(new MessageObject(new String(bytes) + "\n"));
            } else {
                try {
                    ret.add(RpslObject.parse(0, bytes));
                } catch (Exception e) {
                    ret.add(new MessageObject(new String(bytes) + "\n"));
                }
            }

            // skip separator
            begin = end + DOUBLE_NEWLINE.length;
            while ((begin < whoisServerOutput.length) && (whoisServerOutput[begin] == '\n' || whoisServerOutput[begin] == '\r')) {
                begin++;
            }
        } while (begin < whoisServerOutput.length);

        return ret;
    }

    /* indexOfIgnoring() is the core feature of the end-to-end testing, hence I've put it here */
    @Test
    public void byteArrayIndexOfTest() {
        byte[] res = new byte[]{1, '\n', '\n', 2, '\n', '\r', '\n', '\r', 3};
        assertEquals(1, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 0, STUPID_NEWLINE));
        assertEquals(4, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 3, STUPID_NEWLINE));
        assertEquals(-1, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 6, STUPID_NEWLINE));
    }
}
