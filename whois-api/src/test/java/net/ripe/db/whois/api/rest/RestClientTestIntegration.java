package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.QueryFlag;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class RestClientTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:  Test Person\n" +
            "address: Singel 258\n" +
            "phone:   +31 6 12345678\n" +
            "nic-hdl: TP1-TEST\n" +
            "mnt-by:  OWNER-MNT\n" +
            "changed: dbtest@ripe.net 20120101\n" +
            "source:  TEST\n");

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Test
    public void search_restClient() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final RestClient restClient = new RestClient();
        restClient.setRestApiUrl(String.format("http://localhost:%d/whois", getPort()));

        final Iterable<RpslObject> result = restClient.search("AS102", Collections.<String>emptySet(), Collections.<AttributeType>emptySet(),
                Collections.<String>emptySet(), Collections.<String>emptySet(), ImmutableSet.of(ObjectType.AUT_NUM), ImmutableSet.of(QueryFlag.NO_REFERENCED));
        final Iterator<RpslObject> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        final RpslObject rpslObject = iterator.next();
        assertFalse(iterator.hasNext());
        assertThat(rpslObject.getKey().toUpperCase(), is("AS102"));
    }

    @Test
    public void update_person_with_empty_remarks_has_remarks() throws Exception {

        final RpslObject updatedObject = new RpslObjectBuilder(TEST_PERSON).addAttribute(new RpslAttribute(AttributeType.REMARKS, "")).sort().get();

        final RestClient restClient = new RestClient();
        restClient.setRestApiUrl(String.format("http://localhost:%d/whois", getPort()));
        restClient.setSource("TEST");

        RpslObject updated = restClient.update(updatedObject, "test");
        assertThat(updated.findAttributes(AttributeType.REMARKS), hasSize(1));
    }

    @Test
    public void testLookupWithoutPass() throws Exception {
        RestClient restClient = new RestClient();
        restClient.setRestApiUrl(String.format("http://localhost:%d/whois", getPort()));
        restClient.setSource("TEST");
        RpslObject object = restClient.lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW"));
    }

    @Test
    public void testLookupWithPass() throws Exception {
        RestClient restClient = new RestClient();
        restClient.setRestApiUrl(String.format("http://localhost:%d/whois", getPort()));
        restClient.setSource("TEST");

        RpslObject object = restClient.lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString(), "test");

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/"));
    }
}