package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class IndexWithValueAndTypeStatusIntegrationTest extends IndexIntegrationTestBase {
    private IndexStrategy subject;

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST\n");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_OTHER_ORGANISATION = RpslObject.parse("" +
            "organisation:   ORG-TO1-TEST\n" +
            "org-name:       Test Organisation\n" +
            "status:         OTHER\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n");

    @BeforeEach
    public void setUp() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_OTHER_ORGANISATION);

        subject = IndexStrategies.get(AttributeType.STATUS);
    }

    @Test
    public void findAutnumByStatusIndex() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "org:            ORG-TO1-TEST\n" +
                "status:         ASSIGNED\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "ASSIGNED");

        assertThat(result, hasSize(1));

        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.AUT_NUM));
        assertThat(rpslObject.getKey(), is("AS102"));
    }

    @Test
    public void findInetnumByStatusIndex() {

        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.255.255.255\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");

        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO1-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");

        List<RpslObjectInfo> allocatedPas = subject.findInIndex(whoisTemplate, "ALLOCATED PA");
        assertThat(allocatedPas, hasSize(2));
        assertThat(allocatedPas.stream().map( allocatedPa -> allocatedPa.getKey()).collect(Collectors.toSet()), containsInAnyOrder("10.0.0.0 - 10.0.0.255", "10.0.0.0 - 10.255.255.255"));
    }

    @Test
    public void findInet6numByStatusIndex() {

        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                        "netname:        RIPE-NCC\n" +
                        "descr:          Private Network\n" +
                        "country:        NL\n" +
                        "tech-c:         TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "mnt-lower:      OWNER-MNT\n" +
                        "source:         TEST"
        );

        List<RpslObjectInfo> assignedPa = subject.findInIndex(whoisTemplate, "ASSIGNED PA", ObjectType.INET6NUM);
        assertThat(assignedPa, hasSize(1));
        assertThat(assignedPa.get(0).getObjectType(), is(ObjectType.INET6NUM));
        assertThat(assignedPa.get(0).getKey(), is("2001:2002:2003::/48"));
    }

    @Test
    public void searchNonexistentRoleByName() {
        assertThat(subject.findInIndex(whoisTemplate, "nonexistent"), hasSize(0));
    }
}
