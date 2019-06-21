package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


@Category(IntegrationTest.class)
public class WhoisVersionServiceTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final String VERSION_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";

    @Autowired
    private MaintenanceMode maintenanceMode;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void versions_returns_xml() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisVersions whoisVersions = whoisResources.getVersions();
        assertThat(whoisVersions.getType(), is("aut-num"));
        assertThat(whoisVersions.getKey(), is("AS102"));
        assertThat(whoisVersions.getVersions(), hasSize(1));
        final WhoisVersion whoisVersion = whoisVersions.getVersions().get(0);
        assertThat(whoisVersion, is(new WhoisVersion("ADD/UPD", whoisVersion.getDate(), 1)));
    }

    @Test
    public void versions_deleted() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(3));
        assertThat(versions.get(0).getDeletedDate(), is(not(nullValue())));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(1));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(2).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(2).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(2).getRevision(), is(2));
        assertThat(versions.get(2).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_deleted_versions_json() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(3));
        assertThat(versions.get(0).getDeletedDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(1));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(2).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(2).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(2).getRevision(), is(2));
        assertThat(versions.get(2).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_last_version_deleted() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(1));
        assertThat(versions.get(0).getDeletedDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));
    }

    @Test(expected = NotFoundException.class)
    public void versions_no_versions_found() throws IOException {
        RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void version_nonexistant_version() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/2")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test(expected = NotFoundException.class)
    public void version_wrong_object_type() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        RestTest.target(getPort(), "whois/test/inetnum/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test
    public void version_returns_xml() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));

        final List<Attribute> attributes = object.getAttributes();
        final List<RpslAttribute> originalAttributes =  RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n")
                .getAttributes();

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_returns_json() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));

        final List<Attribute> attributes = object.getAttributes();

        final List<RpslAttribute> originalAttributes = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n")
                .getAttributes();

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test(expected = NotFoundException.class)
    public void version_not_showing_deleted_version() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test
    public void lookup_non_streaming_puts_xlink_into_root_element_and_nowhere_else() throws Exception {
        databaseHelper.addObject("aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1").request(MediaType.APPLICATION_XML).get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\" version=\"1\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

}
