package net.ripe.db.whois.query.support;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.io.ByteArrayInput;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.ByteArrayContains;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.MessageObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class QueryExecutor {
    private static final byte[] DOUBLE_NEWLINE = new byte[]{'\n', '\n'};
    private static final byte[] STUPID_NEWLINE = new byte[]{'\r'};

    private static final Set<String> ERRORS_TO_IGNORE = Sets.newHashSet("ERROR:101:", "ERROR:106:");

    private final QueryExecutorConfiguration configuration;
    private final AccessControlListManager accessControlListManager;
    private final Logger logger;

    public QueryExecutor(final QueryExecutorConfiguration configuration, final AccessControlListManager accessControlListManager, final Logger logger) throws UnknownHostException {
        this.configuration = configuration;
        this.accessControlListManager = accessControlListManager;
        this.logger = logger;
    }

    public List<ResponseObject> getWhoisResponse(final String query) throws IOException {
        final DummyWhoisClient client = new DummyWhoisClient(configuration.getHost(), configuration.getPort());
        final String response;

        final Stopwatch stopWatch = new Stopwatch().start();
        try {
            response = client.sendQuery(query);
        } finally {
            stopWatch.stop();
        }

        final List<ResponseObject> responseObjects = parseWhoisResponseIntoRpslObjects(query, response);
        logIfError(response);

        int personObjectCount = 0;
        int totalObjectCount = 0;
        for (final ResponseObject responseObject : responseObjects) {
            if (responseObject instanceof RpslObject) {
                totalObjectCount++;
                final RpslObject rpslObject = (RpslObject) responseObject;
                final ObjectType objectType = rpslObject.getType();
                if (ObjectType.PERSON.equals(objectType) || ObjectType.ROLE.equals(objectType)) {
                    personObjectCount++;
                }
            }
        }

        logger.info(String.format("%s > objects: %6d, personal: %6d, comments: %6d in %s for query %s",
                configuration.getIdentifier(),
                totalObjectCount,
                personObjectCount,
                responseObjects.size() - totalObjectCount,
                stopWatch.toString(),
                query));

        return responseObjects;
    }

    private void logIfError(final String response) {
        final int errorIndex = response.indexOf("ERROR:");
        if (errorIndex == -1) {
            return;
        }

        for (final String errorToIgnore : ERRORS_TO_IGNORE) {
            if (response.contains(errorToIgnore)) {
                return;
            }
        }

        logger.warn("Error occured: \n\n{}", StringUtils.left(response.substring(errorIndex), 200));
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
                    throw new RuntimeException("Parsing of RPSL object failed: [" + new String(whoisServerOutput, begin, realend - begin) + "]", e);
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

    @Test
    public void byteArrayIndexOfTest() {
        byte[] res = new byte[]{1, '\n', '\n', 2, '\n', '\r', '\n', '\r', 3};
        assertEquals(1, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 0, STUPID_NEWLINE));
        assertEquals(4, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 3, STUPID_NEWLINE));
        assertEquals(-1, ByteArrayContains.indexOfIgnoring(res, DOUBLE_NEWLINE, 6, STUPID_NEWLINE));
    }
}
