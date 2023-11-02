package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class BatchUpdatesServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @BeforeEach
    public void setup() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", "secret", ObjectType.values()));

        databaseHelper.addObject(
            "mntner:      OWNER-MNT\n" +
            "descr:       used to maintain other MNTNERs\n" +
            "upd-to:      updto_owner@ripe.net\n" +
            "mnt-nfy:     mntnfy_owner@ripe.net\n" +
            "notify:      notify_owner@ripe.net\n" +
            "auth:        MD5-PW $1$fyALLXZB$V5Cht4.DAIM3vi64EpC0w/  #owner\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
            "person:  Test Person\n" +
            "address: St James Street\n" +
            "address: Burnley\n" +
            "address: UK\n" +
            "phone:   +44 282 420469\n" +
            "nic-hdl: TP1-TEST\n" +
            "mnt-by:  OWNER-MNT\n" +
            "source:  TEST");

        databaseHelper.addObject(
        "mntner:      OWNER2-MNT\n" +
            "descr:       used to maintain other MNTNERs\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      updto_owner2@ripe.net\n" +
            "mnt-nfy:     mntnfy_owner2@ripe.net\n" +
            "notify:      notify_owner2@ripe.net\n" +
            "auth:        MD5-PW $1$9vNwegLB$SrX4itajapDaACGZaLOIY1  #owner2\n" +
            "mnt-by:      OWNER2-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
        "mntner:      OWNER3-MNT\n" +
            "descr:       used to maintain other MNTNERs\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      updto_owner3@ripe.net\n" +
            "upd-to:      updto2_owner3@ripe.net\n" +
            "notify:      notify_owner3@ripe.net\n" +
            "auth:        MD5-PW $1$u/Ttxt8r$zeII/ZqRwC2PuRyGyv0U51  #owner3\n" +
            "mnt-by:      OWNER3-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
    "mntner:      OWNER4-MNT\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      updto_owner3@ripe.net\n" +
            "upd-to:      updto2_owner3@ripe.net\n" +
            "notify:      notify_owner3@ripe.net\n" +
            "auth:        SSO person@net.net # 906635c2-0405-429a-800b-0602bd716124\n" +
            "mnt-by:      OWNER4-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
        "organisation:    ORG-LIR1-TEST\n" +
            "org-type:        LIR\n" +
            "org-name:        Local Internet Registry\n" +
            "address:         RIPE NCC\n" +
            "e-mail:          dbtest@ripe.net\n" +
            "ref-nfy:         dbtest-org@ripe.net\n" +
            "mnt-ref:         owner3-mnt\n" +
            "mnt-by:          owner2-mnt\n" +
            "source:  TEST");

        databaseHelper.addObject(
    "organisation:    ORG-LIR2-TEST\n" +
            "org-type:        LIR\n" +
            "org-name:        Local Internet Registry\n" +
            "address:         RIPE NCC\n" +
            "e-mail:          dbtest@ripe.net\n" +
            "ref-nfy:         dbtest-org@ripe.net\n" +
            "mnt-by:          owner4-mnt\n" +
            "source:  TEST");

        databaseHelper.addObject(
        "mntner:      RIPE-NCC-HM-MNT\n" +
            "descr:       hostmaster MNTNER\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      updto_hm@ripe.net\n" +
            "mnt-nfy:     mntnfy_hm@ripe.net\n" +
            "notify:      notify_hm@ripe.net\n" +
            "auth:        MD5-PW $1$mV2gSZtj$1oVwjZr0ecFZQHsNbw2Ss.  #hm\n" +
            "mnt-by:      RIPE-NCC-HM-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
        "mntner:      LIR-MNT\n" +
            "descr:       used for lir\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      updto_lir@ripe.net\n" +
            "mnt-nfy:     mntnfy_lir@ripe.net\n" +
            "notify:      notify_lir@ripe.net\n" +
            "auth:        MD5-PW $1$epUPWc4g$/6BKqK4lKR/lNqLa7K5qT0  #lir\n" +
            "mnt-by:      LIR-MNT\n" +
            "source:      TEST");

        databaseHelper.addObject(
        "inetnum:      0.0.0.0 - 255.255.255.255\n" +
            "netname:      IANA-BLK\n" +
            "descr:        The whole IPv4 address space\n" +
            "country:      NL\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "status:       ALLOCATED UNSPECIFIED\n" +
            "remarks:      The country is really worldwide.\n" +
            "remarks:      This address space is assigned at various other places in\n" +
            "remarks:      the world and might therefore not be in the RIPE database.\n" +
            "mnt-by:       RIPE-NCC-HM-MNT\n" +
            "mnt-lower:    RIPE-NCC-HM-MNT\n" +
            "mnt-routes:   RIPE-NCC-HM-MNT\n" +
            "source:       TEST");

        databaseHelper.addObject(
        "inetnum:      192.0.0.0 - 192.255.255.255\n" +
            "netname:      TEST-NET-NAME\n" +
            "descr:        TEST network\n" +
            "country:      NL\n" +
            "org:          ORG-LIR1-TEST\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "status:       ALLOCATED UNSPECIFIED\n" +
            "mnt-by:       RIPE-NCC-HM-MNT\n" +
            "mnt-lower:    LIR-mnt\n" +
            "source:       TEST");

        databaseHelper.addObject(
    "inetnum:      19.0.0.0 - 19.255.255.255\n" +
            "netname:      TEST-NET-NAME\n" +
            "descr:        TEST network\n" +
            "country:      NL\n" +
            "org:          ORG-LIR2-TEST\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "status:       ALLOCATED PA\n" +
            "mnt-by:       RIPE-NCC-HM-MNT\n" +
            "mnt-by:       OWNER4-MNT\n" +
            "source:       TEST");
    }

    @Test
    public void batch_update_one_resource_success() {
        final WhoisResources whoisResources =
                mapRpslObjects(
                    RpslObject.parse(
                        "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                        "netname:      TEST-NET-NAME\n" +
                        "descr:        TEST network\n" +
                        "country:      BE\n" +
                        "org:          ORG-LIR1-TEST\n" +
                        "admin-c:      TP1-TEST\n" +
                        "tech-c:       TP1-TEST\n" +
                        "status:       ALLOCATED UNSPECIFIED\n" +
                        "mnt-by:       RIPE-NCC-HM-MNT\n" +
                        "mnt-lower:    LIR-mnt\n" +
                        "source:       TEST")
                );

        final WhoisResources response = RestTest.target(getPort(), "whois/batch/TEST")
                .queryParam("override", "personadmin,secret")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getErrorMessages(), hasSize(1));

        final RpslObject inetnum = databaseHelper.lookupObject(ObjectType.INETNUM, "192.0.0.0 - 192.255.255.255");
        assertThat(inetnum, not(nullValue()));
        assertThat(inetnum.getValueForAttribute(AttributeType.COUNTRY), equalTo(ciString("BE"))); // object should have been updated
    }

    @Test
    public void batch_update_two_objects_failure() {
        final WhoisResources whoisResources =
                mapRpslObjects(
                    RpslObject.parse(
                        "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                        "netname:      TEST-NET-NAME\n" +
                        "descr:        TEST network\n" +
                        "country:      BE\n" +
                        "org:          ORG-LIR1-TEST\n" +
                        "admin-c:      TP1-TEST\n" +
                        "tech-c:       TP1-TEST\n" +
                        "status:       ALLOCATED UNSPECIFIED\n" +
                        "mnt-by:       RIPE-NCC-HM-MNT\n" +
                        "mnt-lower:    LIR-mnt\n" +
                        "source:       TEST"),
                    RpslObject.parse(
                        "person:  Test Person\n" +
                        "address: St James Street\n" +
                        "address: Burnley\n" +
                        "address: UK\n" +
                        "phone:   +44 282 420469\n" +
                        "nic-hdl: NX-TEST\n" +
                        "mnt-by:  NON-EXISTING-MNT\n" +
                        "source:  TEST")
                );

        try {
            RestTest.target(getPort(), "whois/batch/TEST")
                    .queryParam("override", "personadmin,secret")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail("should have returned 400");
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            assertThat(response.getWhoisObjects(), hasSize(2));
            assertThat(response.getErrorMessages().stream().filter((errorMessage) -> "Error".equals(errorMessage.getSeverity())).collect(Collectors.toList()), hasSize(2));

            final RpslObject inetnum = databaseHelper.lookupObject(ObjectType.INETNUM, "192.0.0.0 - 192.255.255.255");
            assertThat(inetnum, not(nullValue()));
            assertThat(inetnum.getValueForAttribute(AttributeType.COUNTRY), equalTo(ciString("NL"))); // object should not have been updated

            try {
                databaseHelper.lookupObject(ObjectType.PERSON, "NX-TEST");
                fail("object should not have been created");
            } catch (EmptyResultDataAccessException erdae) {
                // it should not find this object
            }
        }
    }

    @Test
    public void batch_update_two_objects_success() {
        final WhoisResources whoisResources =
                mapRpslObjects(
                    RpslObject.parse(
                        "mntner:      OWNER2-MNT\n" +
                        "descr:       used to maintain other MNTNERs\n" +
                        "admin-c:     TP1-TEST\n" +
                        "upd-to:      different_email@ripe.net\n" +
                        "mnt-nfy:     mntnfy_owner2@ripe.net\n" +
                        "notify:      notify_owner2@ripe.net\n" +
                        "auth:        MD5-PW $1$9vNwegLB$SrX4itajapDaACGZaLOIY1  #owner2\n" +
                        "mnt-by:      OWNER2-MNT\n" +
                        "source:      TEST"),
                    RpslObject.parse(
                        "mntner:      OWNER3-MNT\n" +
                        "descr:       used for lots of things\n" +
                        "admin-c:     TP1-TEST\n" +
                        "upd-to:      updto_owner3@ripe.net\n" +
                        "upd-to:      updto2_owner3@ripe.net\n" +
                        "notify:      notify_owner3@ripe.net\n" +
                        "auth:        MD5-PW $1$u/Ttxt8r$zeII/ZqRwC2PuRyGyv0U51  #owner3\n" +
                        "mnt-by:      OWNER3-MNT\n" +
                        "source:      TEST")
                );

        final WhoisResources response = RestTest.target(getPort(), "whois/batch/TEST")
                .queryParam("override", "personadmin,secret")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(2));

        RpslObject owner2mnt = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER2-MNT");
        assertThat(owner2mnt, not(nullValue()));
        assertThat(owner2mnt.getValueForAttribute(AttributeType.UPD_TO), equalTo(ciString("different_email@ripe.net")));

        RpslObject owner3mnt = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER3-MNT");
        assertThat(owner3mnt, not(nullValue()));
        assertThat(owner3mnt.getValueForAttribute(AttributeType.DESCR), equalTo(ciString("used for lots of things")));
    }

    @Test
    public void batch_update_two_objects_with_dry_run() {
        final WhoisResources whoisResources =
                mapRpslObjects(
                        RpslObject.parse(
                                "mntner:      OWNER2-MNT\n" +
                                "descr:       used to maintain other MNTNERs\n" +
                                "admin-c:     TP1-TEST\n" +
                                "upd-to:      different_email@ripe.net\n" +
                                "mnt-nfy:     mntnfy_owner2@ripe.net\n" +
                                "notify:      notify_owner2@ripe.net\n" +
                                "auth:        MD5-PW $1$9vNwegLB$SrX4itajapDaACGZaLOIY1  #owner2\n" +
                                "mnt-by:      OWNER2-MNT\n" +
                                "source:      TEST"),
                        RpslObject.parse(
                                "mntner:      OWNER3-MNT\n" +
                                "descr:       used for lots of things\n" +
                                "admin-c:     TP1-TEST\n" +
                                "upd-to:      updto_owner3@ripe.net\n" +
                                "upd-to:      updto2_owner3@ripe.net\n" +
                                "notify:      notify_owner3@ripe.net\n" +
                                "auth:        MD5-PW $1$u/Ttxt8r$zeII/ZqRwC2PuRyGyv0U51  #owner3\n" +
                                "mnt-by:      OWNER3-MNT\n" +
                                "source:      TEST")
                );

        final WhoisResources response = RestTest.target(getPort(), "whois/batch/TEST")
                .queryParam("override", "personadmin,secret")
                .queryParam("dry-run", "true")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(2));
        assertThat(response.getErrorMessages().get(0).getText(), is("Dry-run performed, no changes to the database have been made"));
        assertThat(response.getErrorMessages().get(2).getText(), is("Dry-run performed, no changes to the database have been made"));

        RpslObject owner2mnt = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER2-MNT");
        assertThat(owner2mnt, not(nullValue()));
        assertThat(owner2mnt.getValueForAttribute(AttributeType.UPD_TO), equalTo(ciString("updto_owner2@ripe.net")));

        RpslObject owner3mnt = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER3-MNT");
        assertThat(owner3mnt, not(nullValue()));
        assertThat(owner3mnt.getValueForAttribute(AttributeType.DESCR), equalTo(ciString("used to maintain other MNTNERs")));

    }

    @Test
    public void batch_update_delete_create_inetnum_success() {
        final WhoisResources whoisResources =
            mapRpslObjects(
                RpslObject.parse(
                        "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                                "netname:      TEST-NET-NAME\n" +
                                "descr:        TEST network\n" +
                                "country:      NL\n" +
                                "org:          ORG-LIR1-TEST\n" +
                                "admin-c:      TP1-TEST\n" +
                                "tech-c:       TP1-TEST\n" +
                                "status:       ALLOCATED UNSPECIFIED\n" +
                                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                                "mnt-lower:    LIR-mnt\n" +
                                "source:       TEST"),
                RpslObject.parse(
                        "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                                "netname:      TEST-NET-NAME\n" +
                                "descr:        TEST network\n" +
                                "country:      BE\n" +
                                "org:          ORG-LIR1-TEST\n" +
                                "admin-c:      TP1-TEST\n" +
                                "tech-c:       TP1-TEST\n" +
                                "status:       ALLOCATED UNSPECIFIED\n" +
                                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                                "mnt-lower:    LIR-mnt\n" +
                                "source:       TEST")
        );
        whoisResources.getWhoisObjects().get(0).setAction(Action.DELETE);

        final WhoisResources response = RestTest.target(getPort(), "whois/batch/TEST")
                .queryParam("override", "personadmin,secret")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(2));
        assertThat(response.getErrorMessages().stream().filter((message) -> message.getSeverity().equals("Info")).collect(Collectors.toList()), hasSize(2));
        assertThat(response.getErrorMessages().stream().filter((message) -> message.getSeverity().equals("Warning")).collect(Collectors.toList()), hasSize(3));

        RpslObject inetnum = databaseHelper.lookupObject(ObjectType.INETNUM, "192.0.0.0 - 192.255.255.255");
        assertThat(inetnum, not(nullValue()));
        assertThat(inetnum.getValueForAttribute(AttributeType.COUNTRY), equalTo(ciString("BE")));
    }

    @Test
    public void batch_update_no_valid_authentication() {
        assertThrows(NotAuthorizedException.class, () -> {
            final WhoisResources whoisResources =
                    mapRpslObjects(
                            RpslObject.parse(
                                    "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                                            "netname:      TEST-NET-NAME\n" +
                                            "descr:        TEST network\n" +
                                            "country:      BE\n" +
                                            "org:          ORG-LIR1-TEST\n" +
                                            "admin-c:      TP1-TEST\n" +
                                            "tech-c:       TP1-TEST\n" +
                                            "status:       ALLOCATED UNSPECIFIED\n" +
                                            "mnt-by:       RIPE-NCC-HM-MNT\n" +
                                            "mnt-lower:    LIR-mnt\n" +
                                            "source:       TEST")
                    );

            RestTest.target(getPort(), "whois/batch/TEST")
                    .request()
                    .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        });
    }

    @Test
    public void batch_update_using_sso_credential() {
        final WhoisResources whoisResources =
                mapRpslObjects(
                        RpslObject.parse(
                                "inetnum:      19.0.0.0 - 19.1.255.255\n" +
                                "netname:      TEST-NET-NAME\n" +
                                "descr:        TEST network\n" +
                                "country:      BE\n" +
                                "admin-c:      TP1-TEST\n" +
                                "tech-c:       TP1-TEST\n" +
                                "status:       ASSIGNED PA\n" +
                                "mnt-by:       OWNER4-MNT\n" +
                                "source:       TEST")
                );

        RestTest.target(getPort(), "whois/batch/TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(ObjectType.INETNUM, "19.0.0.0 - 19.1.255.255"), not(nullValue()));
    }

    @Test
    public void batch_update_create_self_referencing_mntner_using_sso_credential() {
        final WhoisResources whoisResources = mapRpslObjects(RpslObject.parse(
            "mntner:    SSO-MNT\n" +
            "descr:     Maintainer\n" +
            "admin-c:   TP1-TEST\n" +
            "upd-to:    person@net.net\n" +
            "auth:      SSO person@net.net\n" +
            "mnt-by:    SSO-MNT\n" +
            "source:    TEST"));

        RestTest.target(getPort(), "whois/batch/TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-MNT"), not(nullValue()));
    }

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

}
