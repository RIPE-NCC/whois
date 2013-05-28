package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.domain.QueryMessages;
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
        SetupQueryDatabaseTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class},
        inheritListeners = false)
public abstract class AbstractWhoisIntegrationTest extends AbstractDatabaseHelperTest {
    public static final String HOST = "localhost";

    @Autowired protected QueryServer queryServer;
    @Autowired protected IpResourceConfiguration ipResourceConfiguration;
    @Autowired protected IpTreeUpdater ipTreeUpdater;

    @Before
    public final void setUpAbstractIntegrationTest() throws Exception {
        databaseHelper.clearAclLimits();
        databaseHelper.insertAclIpLimit("0/0", -1, true);
        databaseHelper.insertAclIpLimit("::0/0", -1, true);
        ipResourceConfiguration.reload();
        ipTreeUpdater.rebuild();
    }

    public static String stripHeader(final String response) {
        String result = response;

        result = stripHeader(result, QueryMessages.termsAndConditions().toString());
        result = stripHeader(result, QueryMessages.versionsAdvert().toString());

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
