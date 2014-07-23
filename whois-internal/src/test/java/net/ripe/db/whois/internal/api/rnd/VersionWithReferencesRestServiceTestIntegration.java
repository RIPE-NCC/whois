package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;

@Category(IntegrationTest.class)
public class VersionWithReferencesRestServiceTestIntegration extends AbstractInternalTest {

    //datetime pattern should be the same as in VersionDateTime
    private DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

//    @Autowired
//    @Qualifier("whoisReadOnlySlaveDataSource")
//    DataSource dataSource;


    @Before
    public void setUp() throws Exception {
        testDateTimeProvider.reset();
        databaseHelper.setupWhoisDatabase(whoisTemplate);

        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");

        whoisTemplate.execute("" +
                        "INSERT INTO `last` (`object_id`, `sequence_id`, `timestamp`, `object_type`, `object`, `pkey`)\n" +
                        "VALUES\n" +
                        "(1, 1, 1406014033, 9, X'6D6E746E65723A202020202020202020544553542D4D4E540A64657363723A202020202020202020204D61696E7461696E65720A617574683A202020202020202020202053534F20706572736F6E406E65742E6E65740A617574683A20202020202020202020204D44352D5057202431246439664B65547232245369375975644E66347255476D5237316E2F63716B2F2023746573740A6D6E742D62793A202020202020202020544553542D4D4E540A726566657272616C2D62793A20202020544553542D4D4E540A7570642D746F3A2020202020202020206E6F7265706C7940726970652E6E65740A6368616E6765643A20202020202020206E6F7265706C7940726970652E6E65742032303132303130310A736F757263653A202020202020202020544553540A', 'TEST-MNT'),\n" +
                        "(2, 1, 1406014033, 10, X'706572736F6E3A2020202020202020205465737420506572736F6E0A6E69632D68646C3A20202020202020205450312D544553540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'TP1-TEST'),\n" +
                        "(3, 1, 1406014033, 10, X'706572736F6E3A2020202020202020204F7468657220506572736F6E0A6E69632D68646C3A20202020202020204F50312D544553540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'OP1-TEST'),\n" +
                        "(4, 3, 1407137233, 18, X'6F7267616E69736174696F6E3A2020204F52472D544F4C312D544553540A6F72672D6E616D653A2020202020202041636D6520636172706574730A6F72672D747970653A202020202020204F544845520A616464726573733A2020202020202020646966666572656E7420616464726573730A61646D696E2D633A20202020202020205450312D544553540A61646D696E2D633A20202020202020204F50312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D7265663A2020202020202020544553542D4D4E540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-TOL1-TEST'),\n" +
                        "(5, 1, 1406446033, 9, X'6D6E746E65723A2020202020202020204F52472D4D4E540A64657363723A202020202020202020206F72677265666572656E63696E67206D6E746E65720A6F72673A2020202020202020202020204F52472D544F4C312D544553540A61646D696E2D633A20202020202020205450312D544553540A7570642D746F3A2020202020202020207465737440726970652E6E65740A617574683A20202020202020202020204D44352D50572024312467437779324E4D58244B553534475338715A6E62344177536D2E74394772310A6D6E742D62793A2020202020202020204F52472D4D4E540A726566657272616C2D62793A202020204F52472D4D4E540A6368616E6765643A2020202020202020747465737440726970652E6E65740A736F757263653A202020202020202020544553540A', 'ORG-MNT'),\n" +
                        "(6, 1, 1406446033, 18, X'6F7267616E69736174696F6E3A2020204F52472D4F5448312D544553540A6F72672D6E616D653A202020202020204F74686572206F72670A6F72672D747970653A202020202020204F544845520A6F72673A2020202020202020202020204F52472D544F4C312D544553540A616464726573733A20202020202020207374726565740A61646D696E2D633A20202020202020205450312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D7265663A2020202020202020544553542D4D4E540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-OTH1-TEST');\n"
                        );
        whoisTemplate.execute("" +
                        "INSERT INTO `history` (`object_id`, `sequence_id`, `timestamp`, `object_type`, `object`, `pkey`)\n" +
                        "VALUES\n" +
                        "(4, 1, 1406014033, 18, X'6F7267616E69736174696F6E3A2020204F52472D544F4C312D544553540A6F72672D6E616D653A2020202020202041636D6520636172706574730A6F72672D747970653A202020202020204F544845520A616464726573733A20202020202020207374726565740A616464726573733A2020202020202020706F7374636F64650A616464726573733A2020202020202020636F756E7472790A61646D696E2D633A20202020202020205450312D544553540A61646D696E2D633A20202020202020204F50312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D7265663A2020202020202020544553542D4D4E540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-TOL1-TEST'),\n" +
                        "(4, 2, 1406446033, 18, X'6F7267616E69736174696F6E3A2020204F52472D544F4C312D544553540A6F72672D6E616D653A2020202020202041636D6520636172706574730A6F72672D747970653A202020202020204F544845520A616464726573733A20202020202020206F6E65206C696E6520616464726573730A61646D696E2D633A20202020202020205450312D544553540A61646D696E2D633A20202020202020204F50312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D7265663A2020202020202020544553542D4D4E540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-TOL1-TEST');\n"
                        );
        whoisTemplate.execute("" +
                        "INSERT INTO `serials` (`serial_id`, `object_id`, `sequence_id`, `atlast`, `operation`)\n" +
                        "VALUES\n" +
                        "(1, 1, 1, 1, 1),\n" +
                        "(2, 2, 1, 1, 1),\n" +
                        "(3, 3, 1, 1, 1),\n" +
                        "(4, 4, 1, 0, 1),\n" +
                        "(5, 4, 2, 0, 1),\n" +
                        "(6, 5, 1, 1, 1),\n" +
                        "(7, 6, 1, 1, 1),\n" +
                        "(8, 4, 3, 1, 1);\n"
                        );
        whoisTemplate.execute("" +
                        "INSERT INTO `object_version` (`version_id`, `object_type`, `pkey`, `from_timestamp`, `to_timestamp`, `revision`)\n" +
                        "VALUES\n" +
                        "(1, 9, 'TEST-MNT', 1406014033, NULL, 1),\n" +
                        "(2, 10, 'TP1-TEST', 1406014033, NULL, 1),\n" +
                        "(3, 10, 'OP1-TEST', 1406014033, NULL, 1),\n" +
                        "(4, 18, 'ORG-TOL1-TEST', 1406014033, 1406446033, 1),\n" +
                        "(5, 9, 'ORG-MNT', 1406446033, NULL, 1),\n" +
                        "(6, 18, 'ORG-OTH1-TEST', 1406446033, NULL, 1),\n" +
                        "(7, 18, 'ORG-TOL1-TEST', 1406446033, 1407137233, 2),\n" +
                        "(8, 18, 'ORG-TOL1-TEST', 1407137233, NULL, 3);\n"
                        );
        whoisTemplate.execute("" +
                        "INSERT INTO `object_reference` (`version_id`, `object_type`, `pkey`, `ref_type`)\n" +
                        "VALUES\n" +
                        "(1, 9, 'TEST-MNT', 0),\n" +
                        "(1, 9, 'TEST-MNT', 1),\n" +
                        "(2, 9, 'TEST-MNT', 0),\n" +
                        "(3, 9, 'TEST-MNT', 0),\n" +
                        "(4, 10, 'TP1-TEST', 0),\n" +
                        "(4, 10, 'OP1-TEST', 0),\n" +
                        "(4, 9, 'TEST-MNT', 0),\n" +
                        "(4, 9, 'TEST-MNT', 0),\n" +
                        "(5, 10, 'TP1-TEST', 1),\n" +
                        "(5, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(5, 9, 'ORG-MNT', 0),\n" +
                        "(5, 9, 'ORG-MNT', 0),\n" +
                        "(5, 9, 'ORG-MNT', 1),\n" +
                        "(5, 9, 'ORG-MNT', 1),\n" +
                        "(6, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(6, 10, 'TP1-TEST', 1),\n" +
                        "(6, 9, 'TEST-MNT', 1),\n" +
                        "(6, 9, 'TEST-MNT', 1),\n" +
                        "(7, 10, 'TP1-TEST', 0),\n" +
                        "(7, 10, 'OP1-TEST', 0),\n" +
                        "(7, 9, 'TEST-MNT', 0),\n" +
                        "(7, 9, 'TEST-MNT', 0),\n" +
                        "(8, 10, 'TP1-TEST', 0),\n" +
                        "(8, 10, 'OP1-TEST', 0),\n" +
                        "(8, 9, 'TEST-MNT', 0),\n" +
                        "(8, 9, 'TEST-MNT', 0),\n" +
                        "(2, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(3, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(1, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(1, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(1, 10, 'TP1-TEST', 1),\n" +
                        "(1, 10, 'OP1-TEST', 1),\n" +
                        "(5, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(5, 10, 'TP1-TEST', 1),\n" +
                        "(6, 18, 'ORG-TOL1-TEST', 1),\n" +
                        "(6, 10, 'TP1-TEST', 1),\n" +
                        "(6, 9, 'TEST-MNT', 1),\n" +
                        "(6, 9, 'TEST-MNT', 1)," +
                        "(4, 9, 'ORG-MNT', 1),\n" +
                        "(7, 9, 'ORG-MNT', 1),\n" +
                        "(8, 9, 'ORG-MNT', 1),\n" +
                        "(4, 18, 'ORG-OTH1-TEST', 1),\n" +
                        "(7, 18, 'ORG-OTH1-TEST', 1),\n" +
                        "(8, 18, 'ORG-OTH1-TEST', 1);\n"
        );
    }



    @Test
    public void versionWithIncomingAndOutgoingReferences() {
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

        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(new LocalDateTime().plusDays(6));

        System.out.println(RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class));

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//
//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);
//
//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
//        assertThat(result.getReferencing().getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
//        assertThat(result.getReferencing().getWhoisObjects().get(1).getPrimaryKey().get(0).getValue(), is("OP1-TEST"));
//        assertThat(result.getReferencedBy().getWhoisObjects().get(0).getType(), is("MNTNER"));
//        assertThat(result.getReferencedBy().getWhoisObjects().get(1).getType(), is("ORGANISATION"));
    }
}
