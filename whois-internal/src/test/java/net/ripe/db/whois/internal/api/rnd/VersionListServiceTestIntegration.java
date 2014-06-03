package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


@Category(IntegrationTest.class)
public class VersionListServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setup() {
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
    }

    @Test
    public void versionsReturnSomethingAtAll() {
        updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));
        try {
            DatabaseHelper.dumpSchema(whoisDataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        assertThat(result, notNullValue());
    }
}