package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.WhoisQueryTestConfiguration;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.Charset;

@ContextConfiguration(classes= WhoisQueryTestConfiguration.class)
public abstract class AbstractQueryIntegrationTest extends AbstractDaoIntegrationTest {
    public static final String HOST = "localhost";

    @Autowired protected QueryServer queryServer;
    @Autowired protected IpResourceConfiguration ipResourceConfiguration;

    // TODO: [AH] not do this for each test, but reinit context only where needed
    @BeforeEach
    public final void setUpAbstractIntegrationTest() throws Exception {
        databaseHelper.clearAclLimits();
        databaseHelper.insertAclIpLimit("0/0", -1, true);
        databaseHelper.insertAclIpLimit("::0/0", -1, true);
        ipResourceConfiguration.reload();
    }

    public static String stripHeader(final String response) {
        String result = response;

        result = stripHeader(result, QueryMessages.termsAndConditions().toString());

        return result;
    }

    private static String stripHeader(final String response, final String headerString) {
        if (response.startsWith(StringUtils.trim(headerString))) {
            if (response.length() < headerString.length()) {
                return "";
            } else {
                return response.substring(headerString.length() + 1);
            }
        }

        return response;
    }

    protected String query(final String query) {
        return TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);
    }

    protected String query(final String query, final Charset charset) {
        final TelnetWhoisClient telnetWhoisClient = new TelnetWhoisClient(queryServer.getPort(), charset);
        return telnetWhoisClient.sendQuery(query);
    }
}
