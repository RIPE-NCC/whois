package net.ripe.db.whois.api.rdap;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.net.InetAddress;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;


@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RdapControllerAclTestIntegration extends AbstractRdapIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "fax-no:    +31-1234567891\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "e-mail:      owner@ripe.net\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "e-mail:    test@ripe.net\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:    TEST\n");


    private static final RpslObject TEST_INETNUM = RpslObject.parse("" +
                    "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                    "netname:        IANA-BLK\n" +
                    "descr:          The whole IPv4 address space\n" +
                    "country:        NL\n" +
                    "tech-c:         TP1-TEST\n" +
                    "admin-c:        TP1-TEST\n" +
                    "status:         OTHER\n" +
                    "mnt-by:         OWNER-MNT\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:         TEST");

    private static final RpslObject TEST_INE6TNUM = RpslObject.parse("" +
                    "inet6num:       ::/0\n" +
                    "netname:        IANA-BLK\n" +
                    "descr:          The whole IPv6 address space\n" +
                    "country:        NL\n" +
                    "tech-c:         TP1-TEST\n" +
                    "admin-c:        TP1-TEST\n" +
                    "status:         OTHER\n" +
                    "mnt-by:         OWNER-MNT\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:         TEST");
    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:    Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "e-mail:    test@ripe.net\n" +
            "nic-hdl:   ROLE-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:    TEST\n");


    private static final RpslObject TEST_ORGANISATION = RpslObject.parse(
            "organisation:  ORG-TO1-TEST\n" +
                    "org-name:      Test organisation\n" +
                    "org-type:      OTHER\n" +
                    "descr:         Test\n" +
                    "address:       Amsterdam\n" +
                    "e-mail:        org@ripe.net\n" +
                    "phone:         +01-000-000-000\n" +
                    "fax-no:        +01-000-000-000\n" +
                    "admin-c:       TP1-TEST\n" +
                    "mnt-by:        OWNER-MNT\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:        TEST");


    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_INETNUM);
        databaseHelper.addObject(TEST_INE6TNUM);
        databaseHelper.addObject(TEST_ROLE);
        databaseHelper.addObject(TEST_ORGANISATION);
    }

    @Test
    public void lookup_person_entity_acl_denied() {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                createResource("entity/PP1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
                fail();
            } catch (ClientErrorException e) {
                assertErrorStatus(e, HttpStatus.TOO_MANY_REQUESTS_429);
                assertErrorTitleContains(e, "Too Many Requests");
                assertErrorDescriptionContains(e, "%ERROR:201: access denied for 127.0.0.1");
            }
        } finally {
            databaseHelper.unbanIp(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_person_acl_counted() throws Exception {
        createResource("entity/TP1-TEST")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(Entity.class);

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(1));
    }

    @Test
    public void lookup_mntner_acl_not_counted() throws Exception {
        final Entity entity = createResource("entity/OWNER-MNT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(((ArrayList)entity.getVCardArray().get(1)).get(2).toString(), is("[kind, {}, text, individual]"));
        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(0));
    }

    @Test
    public void lookup_organisation_acl_no_counted() throws Exception {
        createResource("entity/ORG-TO1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(0));
    }
    @Test
    public void lookup_inetnum_filtered_emails_acl_not_counted() throws Exception {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "org:      ORG-TO1-TEST\n" +
                "descr:          Test IPv4\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        ROLE-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip entity = createResource("ip/192.0.2.1")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(entity.getHandle(), is("192.0.2.0 - 192.0.2.255"));
        assertThat(entity.getEntitySearchResults().size(), is(4));

        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("ORG-TO1-TEST"));
        assertThat(entity.getEntitySearchResults().get(0).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test organisation], " +
                "[kind, {}, text, org], " +
                "[adr, {label=Amsterdam}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +01-000-000-000], " +
                "[tel, {type=fax}, text, +01-000-000-000]]]"));

        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(1).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]]"));

        assertThat(entity.getEntitySearchResults().get(2).getHandle(), is("ROLE-TEST"));
        assertThat(entity.getEntitySearchResults().get(2).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertThat(entity.getEntitySearchResults().get(3).getHandle(), is("TP1-TEST"));
        assertThat(entity.getEntitySearchResults().get(3).getVCardArray().toString(), is(""+
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Person], [kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ORG-TO1-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "OWNER-MNT");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ROLE-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP1-TEST");

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(0));
    }

    @Test
    public void lookup_inet6num_filtered_emails_acl_not_counted() throws Exception {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:      TEST-NET-NAME\n" +
                "org:      ORG-TO1-TEST\n" +
                "descr:          Test IPv4\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        ROLE-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        ipTreeUpdater.rebuild();

        final Ip entity = createResource("ip/2001:2002:2003::/48")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(entity.getHandle(), is("2001:2002:2003::/48"));
        assertThat(entity.getEntitySearchResults().size(), is(4));

        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("ORG-TO1-TEST"));
        assertThat(entity.getEntitySearchResults().get(0).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test organisation], " +
                "[kind, {}, text, org], " +
                "[adr, {label=Amsterdam}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +01-000-000-000], " +
                "[tel, {type=fax}, text, +01-000-000-000]]]"));

        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(1).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]]"));

        assertThat(entity.getEntitySearchResults().get(2).getHandle(), is("ROLE-TEST"));
        assertThat(entity.getEntitySearchResults().get(2).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertThat(entity.getEntitySearchResults().get(3).getHandle(), is("TP1-TEST"));
        assertThat(entity.getEntitySearchResults().get(3).getVCardArray().toString(), is(""+
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Person], [kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ORG-TO1-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "OWNER-MNT");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ROLE-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP1-TEST");

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(0));
    }

    @Test
    public void lookup_autnum_filtered_emails_acl_not_counted() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:       AS102\n" +
                "as-name:      TEST-AS-NAME\n" +
                "org:      ORG-TO1-TEST\n" +
                "descr:          Test IPv4\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        ROLE-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        final Autnum entity = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(entity.getHandle(), is("AS102"));
        assertThat(entity.getEntitySearchResults().size(), is(4));

        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("ORG-TO1-TEST"));
        assertThat(entity.getEntitySearchResults().get(0).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test organisation], " +
                "[kind, {}, text, org], " +
                "[adr, {label=Amsterdam}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +01-000-000-000], " +
                "[tel, {type=fax}, text, +01-000-000-000]]]"));

        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(1).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]]"));

        assertThat(entity.getEntitySearchResults().get(2).getHandle(), is("ROLE-TEST"));
        assertThat(entity.getEntitySearchResults().get(2).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertThat(entity.getEntitySearchResults().get(3).getHandle(), is("TP1-TEST"));
        assertThat(entity.getEntitySearchResults().get(3).getVCardArray().toString(), is(""+
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Person], [kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]]"));

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ORG-TO1-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "OWNER-MNT");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "ROLE-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP1-TEST");

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(0));
    }

    @Test
    public void lookup_role_acl_counted() throws Exception {
        final Entity entity = createResource("entity/ROLE-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), is("ROLE-TEST"));
        assertThat(entity.getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678], " +
                "[email, {type=email}, text, test@ripe.net]]"));

        assertThat(entity.getEntitySearchResults().size(), is(1));

        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(0).getVCardArray().toString(), is("" +
                "[vcard, [[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]]"));

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "OWNER-MNT");

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST)), is(1));
    }
}
