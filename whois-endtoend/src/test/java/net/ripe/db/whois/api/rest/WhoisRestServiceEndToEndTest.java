package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableMap;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

public class WhoisRestServiceEndToEndTest extends AbstractIntegrationTest {

    private static ImmutableMap<String, RpslObject> baseFixtures = ImmutableMap.<String, RpslObject>builder()
            .put("PP1-TEST", RpslObject.parse("" +
                    "person:    Pauleth Palthen\n" +
                    "address:   Singel 258\n" +
                    "phone:     +31-1234567890\n" +
                    "e-mail:    noreply@ripe.net\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "nic-hdl:   PP1-TEST\n" +
                    "changed:   noreply@ripe.net 20120101\n" +
                    "remarks:   remark\n" +
                    "source:    TEST\n"))

            .put("OWNER-MNT", RpslObject.parse("" +
                    "mntner:      OWNER-MNT\n" +
                    "descr:       Owner Maintainer\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      noreply@ripe.net\n" +
                    "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                    "mnt-by:      OWNER-MNT\n" +
                    "referral-by: OWNER-MNT\n" +
                    "changed:     dbtest@ripe.net 20120101\n" +
                    "source:      TEST"))

            .put("RIPE-NCC-HM-MNT", RpslObject.parse("" +
                    "mntner:      RIPE-NCC-HM-MNT\n" +
                    "descr:       hostmaster MNTNER\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      updto_hm@ripe.net\n" +
                    "mnt-nfy:     mntnfy_hm@ripe.net\n" +
                    "notify:      notify_hm@ripe.net\n" +
                    "auth:        MD5-PW $1$mV2gSZtj$1oVwjZr0ecFZQHsNbw2Ss.  #hm\n" +
                    "mnt-by:      RIPE-NCC-HM-MNT\n" +
                    "referral-by: RIPE-NCC-HM-MNT\n" +
                    "changed:     dbtest@ripe.net\n" +
                    "source:      TEST"))

            .put("LIR-MNT", RpslObject.parse("" +
                    "mntner:      LIR-MNT\n" +
                    "descr:       used for lir\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      updto_lir@ripe.net\n" +
                    "mnt-nfy:     mntnfy_lir@ripe.net\n" +
                    "notify:      notify_lir@ripe.net\n" +
                    "auth:        MD5-PW $1$epUPWc4g$/6BKqK4lKR/lNqLa7K5qT0  #lir\n" +
                    "auth:        SSO db-test@ripe.net\n" +
                    "mnt-by:      LIR-MNT\n" +
                    "referral-by: LIR-MNT\n" +
                    "changed:     dbtest@ripe.net\n" +
                    "source:      TEST"))

            .put("END-USER-MNT", RpslObject.parse("" +
                    "mntner:      END-USER-MNT\n" +
                    "descr:       used for lir\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      updto_lir@ripe.net\n" +
                    "auth:        MD5-PW $1$4qnKkEY3$9NduUoRMNiBbAX9QEDMkh1  #end\n" +
                    "mnt-by:      END-USER-MNT\n" +
                    "referral-by: END-USER-MNT\n" +
                    "changed:     dbtest@ripe.net 20120101\n" +
                    "source:      TEST"))

            .put("TP1-TEST", RpslObject.parse("" +
                    "person:    Test Person\n" +
                    "address:   Singel 258\n" +
                    "phone:     +31 6 12345678\n" +
                    "nic-hdl:   TP1-TEST\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "changed:   dbtest@ripe.net 20120101\n" +
                    "source:    TEST\n"))

            .put("TR1-TEST", RpslObject.parse("" +
                    "role:      Test Role\n" +
                    "address:   Singel 258\n" +
                    "phone:     +31 6 12345678\n" +
                    "nic-hdl:   TR1-TEST\n" +
                    "admin-c:   TR1-TEST\n" +
                    "abuse-mailbox: abuse@test.net\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "changed:   dbtest@ripe.net 20120101\n" +
                    "source:    TEST\n"))

            .put("ORG-LIR1-TEST", RpslObject.parse("" +
                    "organisation:    ORG-LIR1-TEST\n" +
                    "org-type:        LIR\n" +
                    "org-name:        Local Internet Registry\n" +
                    "address:         RIPE NCC\n" +
                    "e-mail:          dbtest@ripe.net\n" +
                    "ref-nfy:         dbtest-org@ripe.net\n" +
                    "mnt-ref:         lir-mnt\n" +
                    "mnt-by:          owner-mnt\n" +
                    "changed: denis@ripe.net 20121016\n" +
                    "source:  TEST\n"))

            .put("ALLOC-PA", RpslObject.parse("" +
                    "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                    "netname:      TEST-NET-NAME\n" +
                    "descr:        TEST network\n" +
                    "country:      NL\n" +
                    "org:          ORG-LIR1-TEST\n" +
                    "admin-c:      TP1-TEST\n" +
                    "tech-c:       TP1-TEST\n" +
                    "status:       ALLOCATED PA\n" +
                    "mnt-by:       RIPE-NCC-HM-MNT\n" +
                    "mnt-lower:    LIR-MNT\n" +
                    "changed:      dbtest@ripe.net 20020101\n" +
                    "source:    TEST\n"))

            .build();

    private static ImmutableMap<String, RpslObject> fixtures = ImmutableMap.<String, RpslObject>builder()
            .put("ASS-PA", RpslObject.parse("" +
                    "inetnum:      192.168.200.0 - 192.168.200.255\n" +
                    "netname:      RIPE-NET1\n" +
                    "descr:        /24 assigned\n" +
                    "country:      NL\n" +
                    "org:          ORG-LIR1-TEST\n" +
                    "admin-c:      TP1-TEST\n" +
                    "tech-c:       TP1-TEST\n" +
                    "status:       ASSIGNED PA\n" +
                    "mnt-by:       LIR-MNT\n" +
                    "changed:      dbtest@ripe.net 20020101\n" +
                    "source:    TEST\n"))

            .build();


    @Autowired
    private WhoisObjectServerMapper whoisObjectMapper;

    @Before
    public void setup() {
        databaseHelper.addObjects(baseFixtures.values());
    }

    @Test
//    @Ignore
    public void DELETE_THIS_EXAMPLE_TEST() {
        final RpslObject updatedObject = new RpslObjectBuilder(baseFixtures.get("PP1-TEST")).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        String whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), String.class);

        System.err.println(whoisResources);

//        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
//                .request(MediaType.APPLICATION_XML)
//                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

//        RestTest.assertErrorMessage(whoisResources, 0, "Info", "Authorisation override used");
//        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
//        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
//        assertThat(object.getAttributes(), contains(
//                new Attribute("person", "Pauleth Palthen"),
//                new Attribute("address", "Singel 258"),
//                new Attribute("phone", "+31-1234567890"),
//                new Attribute("e-mail", "noreply@ripe.net"),
//                new Attribute("nic-hdl", "PP1-TEST"),
//                new Attribute("remarks", "remark"),
//                new Attribute("remarks", "updated"),
//                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
//                new Attribute("changed", "noreply@ripe.net 20120101"),
//                new Attribute("source", "TEST")));
//
//        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void Create_assignment() {
        final RpslObject updatedObject = fixtures.get("ASS-PA");

        String whoisResources = null;
        try {
            whoisResources = RestTest.target(getPort(), "whois/test/inetnum")
                    .request(MediaType.APPLICATION_XML).cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), String.class);
        } catch (ClientErrorException e) {
            System.err.println(e.getResponse().getStatus());
            System.err.println(e.getResponse().readEntity(String.class));
        }

        System.err.println(whoisResources);

//        WhoisResources whoisResource = RestTest.target(getPort(), "whois/test/inetnum?password=lir")
//                .request(MediaType.APPLICATION_XML)
//                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

//        RestTest.assertErrorMessage(whoisResources, 0, "Info", "Authorisation override used");
//        assertThat(whoisResource.getWhoisObjects(), hasSize(1));
//        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
//        assertThat(object.getAttributes(), contains(
//                new Attribute("person", "Pauleth Palthen"),
//                new Attribute("address", "Singel 258"),
//                new Attribute("phone", "+31-1234567890"),
//                new Attribute("e-mail", "noreply@ripe.net"),
//                new Attribute("nic-hdl", "PP1-TEST"),
//                new Attribute("remarks", "remark"),
//                new Attribute("remarks", "updated"),
//                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
//                new Attribute("changed", "noreply@ripe.net 20120101"),
//                new Attribute("source", "TEST")));
//
//        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }


}
