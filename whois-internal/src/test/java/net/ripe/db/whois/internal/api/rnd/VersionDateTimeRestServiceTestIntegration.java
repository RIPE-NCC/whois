package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Category(IntegrationTest.class)
public class VersionDateTimeRestServiceTestIntegration extends AbstractInternalTest {

    @Autowired
    @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource dataSource;

    //datetime pattern should be the same as in VersionDateTime
    private DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @Before
    public void setUp() throws Exception {
        testDateTimeProvider.reset();

        databaseHelper.setupWhoisDatabase(new JdbcTemplate(dataSource));
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");

        updateDao.createObject(RpslObject.parse("" +
                "mntner:         TEST-MNT\n" +
                "descr:          Maintainer\n" +
                "auth:           SSO person@net.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:         TEST-MNT\n" +
                "referral-by:    TEST-MNT\n" +
                "upd-to:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST"));
    }

//    @Test
//    public void lookupFirstVersion() {
//        final RpslObject mntner = RpslObject.parse("" +
//                "mntner:         TST-MNT\n" +
//                "descr:          Maintainer\n" +
//                "auth:           SSO person@net.net\n" +
//                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
//                "mnt-by:         TST-MNT\n" +
//                "referral-by:    TST-MNT\n" +
//                "upd-to:         noreply@ripe.net\n" +
//                "changed:        noreply@ripe.net 20120101\n" +
//                "source:         TEST");
//
//
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(mntner);
//
//        final LocalDateTime localDateTime = new LocalDateTime().plusSeconds(10);
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(1));
//        updateDao.updateObject(objectInfo.getObjectId(), new RpslObjectBuilder(mntner).removeAttribute(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get());
//
//        final String creationTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime);
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/mntner/TST-MNT/versions/%s", creationTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//        assertThat(result, not(is(nullValue())));
//        assertThat(result.getWhoisObjects(), hasSize(1));
//
//        final WhoisObject whoisObject = result.getWhoisObjects().get(0);
//
//        assertThat(whoisObject.getVersionDateTime(), is(creationTimestamp));
//
//        assertThat(whoisObject.getAttributes(), hasItems(
//                new Attribute("auth", "MD5-PW", "Filtered", null, null),
//                new Attribute("auth", "SSO", "Filtered", null, null)
//        ));
//
//
//        assertThat(whoisObject.getAttributes(), not(hasItems(
//                new Attribute("changed", "noreply@ripe.net 20120101"),
//                new Attribute("upd-to", "noreply@ripe.net")
//        )));
//    }
//
//    @Test
//    public void lookupRandomVersion() {
//        final RpslObject organisation = RpslObject.parse("" +
//                "organisation: ORG-TOL1-TEST\n" +
//                "org-name: Acme carpets\n" +
//                "org-type: OTHER\n" +
//                "address: street\n" +
//                "address: postcode\n" +
//                "address: country\n" +
//                "e-mail: test@dev.net\n" +
//                "mnt-ref: TEST-MNT\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(organisation);
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//        testDateTimeProvider.setTime(localDateTime.plusDays(5));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "one line address"))
//                        .get()
//        );
//
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(13));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "different address"))
//                        .get());
//
//        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(6));
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
//    }
//
//    @Test
//    public void doesNotExist() {
//        try {
//            RestTest.target(getPort(), String.format("api/rnd/test/aut-num/AS123/versions/%s", DEFAULT_DATE_TIME_FORMATTER.print(new LocalDateTime())), null, apiKey)
//                    .request(MediaType.APPLICATION_JSON_TYPE)
//                    .get(WhoisResources.class);
//
//        } catch (NotFoundException e) {
//            WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
//            assertThat(whoisResources.getErrorMessages(), hasSize(1));
//            assertThat(whoisResources.getErrorMessages().get(0).toString(), is("There is no entry for object AS123 for the supplied date time."));
//        }
//
//    }
//
//    @Test
//    public void multiple_updates_in_same_minute_returns_warning() {
//        final RpslObject one = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "address: one street\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@ripe.net\n" +
//                "source: TEST");
//        final RpslObject two = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "address: two street\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@ripe.net\n" +
//                "source: TEST");
//        final RpslObject three = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "address: three street\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@ripe.net\n" +
//                "source: TEST");
//        final RpslObject four = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "address: four street\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@ripe.net\n" +
//                "source: TEST");
//
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(one);
//        final LocalDateTime localDateTime = new LocalDateTime();
//        final String creationDateTime = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime);
//
//        testDateTimeProvider.setTime(localDateTime.plusSeconds(5));
//        updateDao.updateObject(objectInfo.getObjectId(), two);
//
//        testDateTimeProvider.setTime(localDateTime.plusSeconds(10));
//        updateDao.updateObject(objectInfo.getObjectId(), three);
//
//        testDateTimeProvider.setTime(localDateTime.plusMinutes(1));
//        updateDao.updateObject(objectInfo.getObjectId(), four);
//
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/person/TP1-TEST/versions/%s", creationDateTime), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects(), hasSize(1));
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(1).getValue(), is("three street"));
//
//        final ErrorMessage message = result.getErrorMessages().get(0);
//        assertThat(message.getSeverity(), is("Warning"));
//        assertThat(message.toString(), is("There are 3 versions for the supplied datetime."));
//    }
//
//    @Test(expected = NotFoundException.class)
//    public void deleted() {
//        final RpslObject object = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "address: street\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@ripe.net\n" +
//                "source: TEST");
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(object);
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//        testDateTimeProvider.setTime(localDateTime.plusDays(7));
//
//        updateDao.deleteObject(objectInfo.getObjectId(), "TP1-TEST");
//
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(13));
//        updateDao.createObject(object);
//
//        final String timestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(9));
//        RestTest.target(getPort(), String.format("api/rnd/test/person/TP1-TEST/versions/%s", timestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//    }
//
//    @Test
//    public void recreated_object_with_update_on_the_same_minute() {
//        final RpslObject domain1 = RpslObject.parse("domain:test.sk\ndescr:description1\nsource:TEST\n");
//        final RpslObject domain2 = RpslObject.parse("domain:test.sk\ndescr:description2\nsource:TEST\n");
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(domain1);
//        updateDao.updateObject(objectInfo.getObjectId(), domain2);
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(2));
//        updateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(3));
//        updateDao.createObject(domain1);
//
//        final String afterCreationDateTime = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(1));
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/domain/test.sk/versions/%s", afterCreationDateTime), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects(), hasSize(1));
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(1).getValue(), is("description2"));
//
//        final ErrorMessage message = result.getErrorMessages().get(0);
//        assertThat(message.getSeverity(), is("Warning"));
//        assertThat(message.toString(), is("There are 2 versions for the supplied datetime."));
//    }
//
//
//    @Test
//    public void versionWithIncomingReference() {
//        final RpslObject organisation = RpslObject.parse("" +
//                "organisation: ORG-TOL1-TEST\n" +
//                "org-name: Acme carpets\n" +
//                "org-type: OTHER\n" +
//                "address: street\n" +
//                "address: postcode\n" +
//                "address: country\n" +
//                "e-mail: test@dev.net\n" +
//                "mnt-ref: TEST-MNT\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObject person = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "org: ORG-TOL1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(organisation);
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//        testDateTimeProvider.setTime(localDateTime.plusDays(5));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "one line address"))
//                        .get()
//        );
//
//        updateDao.createObject(person);
//
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(13));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "different address"))
//                        .get());
//
//        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(6));
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
////        assertThat(result.getReferencedBy().getWhoisObjects().get(0).getType(), is("PERSON"));
////        assertThat(result.getReferencedBy().getWhoisObjects().get(0).getPrimaryKey().get(0), is(new Attribute("NIC-HDL", "TP1-TEST")));
//    }
//
//    @Test
//    public void versionWithOutgoingReference() {
//        final RpslObject organisation = RpslObject.parse("" +
//                "organisation: ORG-TOL1-TEST\n" +
//                "org-name: Acme carpets\n" +
//                "org-type: OTHER\n" +
//                "address: street\n" +
//                "address: postcode\n" +
//                "address: country\n" +
//                "admin-c: TP1-TEST\n" +
//                "e-mail: test@dev.net\n" +
//                "mnt-ref: TEST-MNT\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObject person = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        updateDao.createObject(person);
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(organisation);
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//        testDateTimeProvider.setTime(localDateTime.plusDays(5));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "one line address"))
//                        .get()
//        );
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(13));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "different address"))
//                        .get());
//
//        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(6));
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
////        assertThat(result.getReferencing().getWhoisObjects().get(0).getType(), is("PERSON"));
////        assertThat(result.getReferencing().getWhoisObjects().get(0).getPrimaryKey().get(0), is(new Attribute("NIC-HDL", "TP1-TEST")));
//    }
//
//    @Test
//    public void versionWithIncomingAndOutgoingReferences() {
//        final RpslObject organisation = RpslObject.parse("" +
//                "organisation: ORG-TOL1-TEST\n" +
//                "org-name: Acme carpets\n" +
//                "org-type: OTHER\n" +
//                "address: street\n" +
//                "address: postcode\n" +
//                "address: country\n" +
//                "admin-c: TP1-TEST\n" +
//                "admin-c: OP1-TEST\n" +
//                "e-mail: test@dev.net\n" +
//                "mnt-ref: TEST-MNT\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObject outgoingPerson = RpslObject.parse("" +
//                "person: Test Person\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObject outgoingPerson2 = RpslObject.parse("" +
//                "person: Other Person\n" +
//                "nic-hdl: OP1-TEST\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//        final RpslObject incomingMntner = RpslObject.parse("" +
//                "mntner: ORG-MNT\n" +
//                "descr: orgreferencing mntner\n" +
//                "org: ORG-TOL1-TEST\n" +
//                "admin-c: TP1-TEST\n" +
//                "upd-to: test@ripe.net\n" +
//                "auth: MD5-PW $1$gCwy2NMX$KU54GS8qZnb4AwSm.t9Gr1\n" +
//                "mnt-by: ORG-MNT\n" +
//                "referral-by: ORG-MNT\n" +
//                "changed: ttest@ripe.net\n" +
//                "source: TEST");
//        final RpslObject incomingOrg = RpslObject.parse("" +
//                "organisation: ORG-OTH1-TEST\n" +
//                "org-name: Other org\n" +
//                "org-type: OTHER\n" +
//                "org: ORG-TOL1-TEST\n" +
//                "address: street\n" +
//                "admin-c: TP1-TEST\n" +
//                "e-mail: test@dev.net\n" +
//                "mnt-ref: TEST-MNT\n" +
//                "mnt-by: TEST-MNT\n" +
//                "changed: test@test.net\n" +
//                "source: TEST");
//
//
//        updateDao.createObject(outgoingPerson);
//        updateDao.createObject(outgoingPerson2);
//        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(organisation);
//
//        final LocalDateTime localDateTime = new LocalDateTime();
//        testDateTimeProvider.setTime(localDateTime.plusDays(5));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "one line address"))
//                        .get()
//        );
//
//        updateDao.createObject(incomingMntner);
//        updateDao.createObject(incomingOrg);
//
//
//        testDateTimeProvider.setTime(localDateTime.plusDays(13));
//
//        updateDao.updateObject(objectInfo.getObjectId(),
//                new RpslObjectBuilder(organisation)
//                        .removeAttributeType(AttributeType.ADDRESS)
//                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "different address"))
//                        .get());
//
//        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(6));
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
////        assertThat(result.getReferencing().getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
////        assertThat(result.getReferencing().getWhoisObjects().get(1).getPrimaryKey().get(0).getValue(), is("OP1-TEST"));
////        assertThat(result.getReferencedBy().getWhoisObjects().get(0).getType(), is("MNTNER"));
////        assertThat(result.getReferencedBy().getWhoisObjects().get(1).getType(), is("ORGANISATION"));
//    }
}
