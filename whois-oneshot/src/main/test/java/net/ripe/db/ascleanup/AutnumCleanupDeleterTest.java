package net.ripe.db.ascleanup;

import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.rpsl.AttributeType.IMPORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore("Mocking has a problem, it needs a constructor with authoritative resource and to be the one with the longest argument list")
public class AutnumCleanupDeleterTest {
    //Please delete the tests only after you delete the corresponding code.
    @Mock
    AuthoritativeResource authoritativeResource;

    @InjectMocks
    AutnumCleanupDeleter subject;

    @Before
    public void setup() {
        subject.setAuthoritativeResource(authoritativeResource);
        when(authoritativeResource.getAutNums()).thenReturn(ciSet("AS1", "AS2", "AS3", "AS123", "AS13310"));
    }

    @Test
    public void members_withSoughtValueAtEndOfCSList() {
        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS456,AS345, AS123\n"));
        assertThat(result.getValue(), is("AS456,AS345"));
    }

    @Test
    public void members_startOfRange() {
        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS123:AS-KLP\n"));
        assertThat(result.getValue(), is("AS123:AS-KLP\n"));
    }

    @Test
    public void members_startOfRange2() {

        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS13310, AS13310:AS-EXT     # UkrInterNet\n"));
        assertThat(result.getValue(), is("AS13310:AS-EXT     # UkrInterNet\n"));
    }

    @Test
    public void members_endOfRange() {

        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS999:AS-KLP:AS123\n"));
        assertThat(result.getValue(), is("AS999:AS-KLP:AS123\n"));
    }


    @Test
    public void members_withSoughtValueInComment() {
        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS-KLP             #AS123"));
        assertThat(result.getValue(), is("AS-KLP             #AS123"));
    }

    @Test
    public void members_withSoughtValueInMiddleOfCSList() {
        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS456,AS123, AS999\n"));
        assertThat(result.getValue(), is("AS456, AS999\n"));
    }


    @Test
    public void members_withSoughtValueInStartOfCSList() {
        final RpslAttribute result = subject.cleanupMembersAttribute(new RpslAttribute(AttributeType.MEMBERS, "AS123,AS345, AS456\n"));
        assertThat(result.getValue(), is("AS345, AS456\n"));
    }

    @Test
    public void export_standard_asset() {
        final RpslAttribute result = subject.cleanupExportAttribute(new RpslAttribute(AttributeType.EXPORT, "to AS123 announce AS-TEST"));
        assertThat(result, is(nullValue()));
    }

    @Test
    public void export_standard_any() {
        final RpslAttribute result = subject.cleanupExportAttribute(new RpslAttribute(AttributeType.EXPORT, "to AS123 announce ANY"));
        assertThat(result, is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void export_too_complicated() {
        final RpslAttribute rpslAttribute = new RpslAttribute(AttributeType.EXPORT, "to AS123 10.10.10.10 at 10.10.10.10 announce AS-TEST");
        final RpslAttribute result = subject.cleanupExportAttribute(rpslAttribute);
    }
//
//    @Test
//    public void export_standardWithNewline() {
//        final RpslAttribute result = subject.cleanupExportAttribute("          to AS123\n                 announce ANY", Collections.singleton("AS123"));
//        assertThat(result, is(nullValue()));
//    }
//
//    @Test
//    public void export_soughtValueInSSList() {
//        final RpslAttribute result = subject.cleanupExportAttribute("to AS444 announce AS100 AS123 AS444 ", Collections.singleton("AS123"));
//        assertThat(result.getValue(), is("to AS444 announce AS100 AS444 "));
//    }
//
//    @Test
//    public void export_standardAfterAnnounce() {
//        final RpslAttribute result = subject.cleanupExportAttribute("to AS456 announce AS123", Collections.singleton("AS123"));
//        assertThat(result, is(nullValue()));
//    }
//
//    @Test
//    public void export_soughtValueWithComment() {
//        final RpslAttribute result = subject.cleanupExportAttribute("to   AS123 announce AS444   ### STAR21", Collections.singleton("AS123"));
//        assertThat(result, is(nullValue()));
//    }

    @Test
    public void import_value_stays_the_same() {
        assertThat(import_testHelper_empty_found("from AS100 10.0.0.0 at 10.0.0.0 action pref=100; accept AS200"), is(new RpslAttribute(IMPORT, "from AS100 10.0.0.0 at 10.0.0.0 action pref=100; accept AS200")));
        assertThat(import_testHelper_empty_found("from AS1:AS-CUSTOMERS AND AS100 accept <AS34827>"), is(new RpslAttribute(IMPORT, "from AS1:AS-CUSTOMERS AND AS100 accept <AS34827>")));
        assertThat(import_testHelper_empty_found("from AS1:AS-CUSTOMERS accept <AS34827>"), is(new RpslAttribute(IMPORT, "from AS1:AS-CUSTOMERS accept <AS34827>")));
        assertThat(import_testHelper_empty_found("from AS100 accept ANY"), is(new RpslAttribute(IMPORT, "from AS100 accept ANY")));
    }

    @Test
    public void import_cleanup_from_autnum_simple() {
        assertThat(import_testHelper("from AS100 accept AS1"), is(nullValue()));
        assertThat(import_testHelper("from AS1 accept ANY"), is(nullValue()));
        assertThat(import_testHelper("from \nAS1\naccept ANY"), is(nullValue()));
        assertThat(import_testHelper("from AS1 accept AS41158:AS-CUSTOMER"), is(nullValue()));
        assertThat(import_testHelper("from AS100:AS200 accept AS1"), is(nullValue()));
        assertThat(import_testHelper("from \nAS1\naccept ANY"), is(nullValue()));
        assertThat(import_testHelper("from \nas1\taccept AS100"), is(nullValue()));
        assertThat(import_testHelper("from AS1 accept AS100"), is(nullValue()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_with_pref() {
        import_testHelper("from \nAS1 action pref=100;\naccept AS100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_from_range_with_or() {
        import_testHelper("from AS1 OR AS100:AS-TP accept <AS34827>");
    }

    @Test
    public void import_cleanup_with_ip() {
        assertThat(import_testHelper("from AS100 10.0.0.0 at 10.0.0.0 \n accept AS100"), isA(RpslAttribute.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_from_range_with_gateway() {
        import_testHelper("from AS1 10.0.0.0 at 10.0.0.0 action pref=100; accept AS100\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_with_AND() {
        import_testHelper("from AS1 AND AS34827 accept <AS34827>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_with_OR() {
        import_testHelper("from AS1 OR AS100 accept <AS34827>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_with_array() {
        import_testHelper("{\n from AS1 accept AS7;\nfrom AS2 accept AS8;\n }");
    }

    @Test(expected = IllegalArgumentException.class)
    public void import_cleanup_with_two_froms() {
        import_testHelper("from \nAS100\naccept; from AS1000; from AS1 accept");
    }

    @Test
    public void export_value_stays_the_same() {
        assertThat(export_testHelper("to AS6901 announce AS12540"), isA(RpslAttribute.class));
        assertThat(export_testHelper("to AS8776\n                 announce ANY"), isA(RpslAttribute.class));
        assertThat(export_testHelper("to   AS15837 193.189.82.6 at 193.189.82.4 action med=0; to   AS15837 announce AS-TGCTOKLEYREX"), isA(RpslAttribute.class));
    }

    @Test
    public void export_cleanup_from_autnum_simple() {
        assertThat(export_testHelper("to AS1 announce AS12540"), is(nullValue()));
        assertThat(export_testHelper("to AS2\n                 announce ANY"), is(nullValue()));
        assertThat(export_testHelper("to AS6901 announce AS3"), is(nullValue()));
        assertThat(export_testHelper("to ANY\n                 announce AS2"), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void export_cleanup_from_range_with_or() {
        import_testHelper("to   AS15837 193.189.82.6 at 193.189.82.4 action med=0; to   AS2 announce AS-TGCTOKLEYREX");
    }

    private RpslAttribute export_testHelper(String value) {
        return subject.cleanupAttribute(new RpslAttribute(AttributeType.EXPORT, value));
    }

    private RpslAttribute import_testHelper(String value) {
        return subject.cleanupAttribute(new RpslAttribute(AttributeType.IMPORT, value));
    }

    private RpslAttribute import_testHelper_empty_found(String value) {
        return subject.cleanupImportAttribute(new RpslAttribute(AttributeType.IMPORT, value));
    }
}
