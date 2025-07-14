package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class WhoisRestServiceHttpsTestIntegration extends AbstractHttpsIntegrationTest {

    public static final RpslObject TEST_PERSON = RpslObject.parse(
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse(
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @BeforeEach
    public void createTestObjects() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Test
    public void lookup_person_over_https() {
        databaseHelper.addObject(RpslObject.parse("person: Test Person\nnic-hdl: TP2-TEST\nmnt-by: OWNER-MNT\nsource: test"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("source:         test"));

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person/TP2-TEST").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes().get(whoisObject.getAttributes().size() - 1).getValue(), is("test"));
    }



}
