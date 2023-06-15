package net.ripe.db.whois.api.log;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.TestWhoisLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class QueryLogTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST\n");

    @Autowired private TestWhoisLog queryLog;
    @Autowired private RestClient restClient;

    @BeforeEach
    public void setup() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);

        ReflectionTestUtils.setField(restClient, "restApiUrl", String.format("http://localhost:%d/whois", getPort()));
        ReflectionTestUtils.setField(restClient, "sourceName", "TEST");
    }

    @Test
    public void lookup_passes_x_forwarded_for() {
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(queryLog.getMessages(), hasSize(1));
        assertThat(queryLog.getMessage(0), containsString(" PW-API-INFO <0+1+0> "));
        assertThat(queryLog.getMessage(0), containsString("ms [10.20.30.40] -- "));
    }

    @Test
    public void search_passes_x_forwarded_for() {
        final Collection<RpslObject> objects = restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("query-string", "OWNER-MNT")
                .addParam("type-filter", "mntner")
                .addParam("flags", "B")
                .search();

        assertThat(objects, hasSize(2));

        assertThat(queryLog.getMessages(), hasSize(1));
        assertThat(queryLog.getMessage(0), containsString(" PW-API-INFO <1+1+0> "));
        assertThat(queryLog.getMessage(0), containsString("ms [10.20.30.40] -- "));
    }

    @Test
    public void streaming_search_passes_on_x_forwarded_for() {
        final Iterator<WhoisObject> objects = restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("query-string", "OWNER-MNT")
                .addParam("type-filter", "mntner")
                .addParam("flags", "B")
                .streamingSearch();

        final List<WhoisObject> whoisObjects = Lists.newArrayList(objects);

        assertThat(whoisObjects, hasSize(2));

        assertThat(queryLog.getMessages(), hasSize(1));
        assertThat(queryLog.getMessage(0), containsString(" PW-API-INFO <1+1+0> "));
        assertThat(queryLog.getMessage(0), containsString("ms [10.20.30.40] -- "));
    }

    @Test
    public void streaming_search_nothing_found() {
        try {
            final Iterator<WhoisObject> objects = restClient.request()
                    .addParam("query-string", "NONEXISTING")
                    .addParam("type-filter", "mntner")
                    .addParam("flags", "B")
                    .streamingSearch();
            final List<WhoisObject> whoisObjects = Lists.newArrayList(objects);
            fail();
            // TODO shouldn't this return a list of zero or a NotFound?
        } catch(RestClientException ignored) {
           // this should happen
        }
    }

}
