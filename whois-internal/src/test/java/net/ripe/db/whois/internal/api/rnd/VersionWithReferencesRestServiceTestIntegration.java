package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;

@Category(IntegrationTest.class)
public class VersionWithReferencesRestServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setUp() throws Exception {
        testDateTimeProvider.reset();
        databaseHelper.setupWhoisDatabase(whoisTemplate);
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
        /*
            The following statements create 3 objects in DB:
            mntner TEST-MNT
                    mnt-by: TEST-MNT

            person TP1-TEST
                    mnt-by: TEST-MNT

            organisation: ORG-AB1-TEST
                    mnt-by: TEST-MNT
                    admin-c:TP1-TEST

            the organisation has been updated with only the address attribute changed.
         */
        whoisTemplate.execute("" +
                        "INSERT INTO `last` (`object_id`, `sequence_id`, `timestamp`, `object_type`, `object`, `pkey`) " +
                        "VALUES " +
                        "        (1, 1, 1406808443, 9, X'6D6E746E65723A202020202020202020544553542D4D4E540A64657363723A202020202020202020204D61696E7461696E65720A617574683A202020202020202020202053534F20706572736F6E406E65742E6E65740A617574683A20202020202020202020204D44352D5057202431246439664B65547232245369375975644E66347255476D5237316E2F63716B2F2023746573740A6D6E742D62793A202020202020202020544553542D4D4E540A726566657272616C2D62793A20202020544553542D4D4E540A7570642D746F3A2020202020202020206E6F7265706C7940726970652E6E65740A6368616E6765643A20202020202020206E6F7265706C7940726970652E6E65742032303132303130310A736F757263653A202020202020202020544553540A', 'TEST-MNT'), " +
                        "        (2, 1, 1406894843, 10, X'706572736F6E3A2020202020202020205465737420506572736F6E0A6E69632D68646C3A20202020202020205450312D544553540A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'TP1-TEST'), " +
                        "        (3, 2, 1407240443, 18, X'6F7267616E69736174696F6E3A2020204F52472D4142312D544553540A6F72672D6E616D653A2020202020202041636D6520636172706574730A6F72672D747970653A202020202020204F544845520A616464726573733A20202020202020206E657720616464726573730A61646D696E2D633A20202020202020205450312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-AB1-TEST');"
        );
        whoisTemplate.execute("" +
                        "INSERT INTO `history` (`object_id`, `sequence_id`, `timestamp`, `object_type`, `object`, `pkey`) " +
                        "VALUES " +
                        "        (3, 1, 1406894843, 18, X'6F7267616E69736174696F6E3A2020204F52472D4142312D544553540A6F72672D6E616D653A2020202020202041636D6520636172706574730A6F72672D747970653A202020202020204F544845520A616464726573733A20202020202020207374726565740A61646D696E2D633A20202020202020205450312D544553540A652D6D61696C3A20202020202020202074657374406465762E6E65740A6D6E742D62793A202020202020202020544553542D4D4E540A6368616E6765643A20202020202020207465737440746573742E6E65740A736F757263653A202020202020202020544553540A', 'ORG-AB1-TEST');"
        );
        whoisTemplate.execute("" +
                        "INSERT INTO `serials` (`serial_id`, `object_id`, `sequence_id`, `atlast`, `operation`) " +
                        "VALUES " +
                        "        (1, 1, 1, 1, 1), " +
                        "        (2, 2, 1, 1, 1), " +
                        "        (3, 3, 1, 0, 1), " +
                        "        (4, 3, 2, 1, 1);"
        );
        whoisTemplate.execute("" +
                        "INSERT INTO `object_version` (`id`, `pkey`, `object_type`, `from_timestamp`, `to_timestamp`, `revision`) " +
                        "VALUES " +
                        "        (1, 'TEST-MNT', 9, 1406808443, NULL, 1), " +
                        "        (2, 'TP1-TEST', 10, 1406894843, NULL, 1), " +
                        "        (3, 'ORG-AB1-TEST', 18, 1406894843, 1407240443, 1), " +
                        "        (4, 'ORG-AB1-TEST', 18, 1407240443, NULL, 2);"
        );
        whoisTemplate.execute("" +
                        "INSERT INTO `object_reference` (`id`, `from_version`, `to_version`) " +
                        "VALUES " +
                        "        (1, 1, 1), " +
                        "        (2, 2, 1), " +
                        "        (3, 3, 1), " +
                        "        (4, 3, 2), " +
                        "        (5, 4, 1), " +
                        "        (6, 4, 2);"
        );
    }

    @Test
    public void versionWithIncomingAndOutgoingReferences() {
        System.out.println(RestTest.target(getPort(), "api/rnd/test/organisation/ORG-AB1-TEST/versions/1", null, apiKey)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class));
        System.out.println("\n==========================\n");
        System.out.println(RestTest.target(getPort(), "api/rnd/test/mntner/TEST-MNT/versions/1", null, apiKey)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class));

//        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .get(WhoisResources.class);

//        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
//        assertThat(result.getReferencing().getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
//        assertThat(result.getReferencing().getWhoisObjects().get(1).getPrimaryKey().get(0).getValue(), is("OP1-TEST"));
//        assertThat(result.getReferencedBy().getWhoisObjects().get(0).getType(), is("MNTNER"));
//        assertThat(result.getReferencedBy().getWhoisObjects().get(1).getType(), is("ORGANISATION"));
    }
}
