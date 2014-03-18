package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.support.AbstractDaoTest;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.QueryMessages;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ContextConfiguration(locations = {"classpath:applicationContext-query-test.xml"})
@TestExecutionListeners(listeners = {
//        SetupQueryDatabaseTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class},
        inheritListeners = false)
public abstract class AbstractWhoisIntegrationTest extends AbstractDaoTest {
    public static final String HOST = "localhost";

    @Autowired protected QueryServer queryServer;
    @Autowired protected IpResourceConfiguration ipResourceConfiguration;

    // TODO: [AH] not do this for each test, but reinit context only where needed
    @Before
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
}
