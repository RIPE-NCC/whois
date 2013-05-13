package net.ripe.db.whois.query.integration;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class TagsTestIntegration extends AbstractWhoisIntegrationTest {
    @Autowired
    private IpTreeUpdater ipTreeUpdater;

    @Before
    public void setup() {
        final RpslObject inetnum = RpslObject.parse("inetnum:18.0.0.0 - 18.255.255.255\nnetname: NN\nmnt-by: RIPE-NCC-HM-MNT\nadmin-c:TP1-TEST\nstatus:ASSIGNED PI");
        final RpslObject root = RpslObject.parse("inetnum:0.0.0.0 - 255.255.255.255\nnetname: NN\nmnt-by: RIPE-NCC-HM-MNT");
        final RpslObject rsMaintainer = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\norg: ORG-TEST1-TEST\nadmin-c:TP1-TEST");
        final RpslObject org = RpslObject.parse("organisation: ORG-TEST1-TEST\nadmin-c:TP1-TEST");
        final RpslObject person = RpslObject.parse("person: Test Person\naddress: Street\nphone: +31324243\nnic-hdl:TP1-TEST\nmnt-by:RIPE-NCC-HM-MNT\nchanged:test@ripe.net\nsource:TEST");
        final RpslObject unusedMnt = RpslObject.parse("mntner: UNUSED-MNT\nmnt-by: UNUSED-MNT\nadmin-c: TP1-TEST");
        final RpslObject mtagMnt = RpslObject.parse("mntner: MTAG-MNT\nmnt-by: MTAG-MNT\nadmin-c: TP1-TEST\norg: ORG-TEST1-TEST");

        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(inetnum, root, rsMaintainer, org, person, unusedMnt, mtagMnt));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(unusedMnt).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(org).getObjectId(), "unref", "13");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(person).getObjectId(), "foo", "Some Data");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(mtagMnt).getObjectId(), "bar", "bar");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(mtagMnt).getObjectId(), "unref", "unref");

        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @After
    public void shutdown() {
        queryServer.stop();
    }

    @Test
    public void notaginfo_displays_no_info() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--no-taginfo UNUSED-MNT");
        assertThat(response, not(containsString("Unreferenced")));
    }

    @Test
    public void single_dash_notaginfo_works_too() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-no-taginfo UNUSED-MNT");
        assertThat(response, not(containsString("Unreferenced")));
    }

    @Test
    public void taginfo_is_off_per_default() {
        final String response = DummyWhoisClient.query(QueryServer.port, "UNUSED-MNT");
        assertThat(response, not(containsString("Unreferenced")));
    }

    @Test
    public void no_unref_info_for_referenced_object() {
        final String response = DummyWhoisClient.query(QueryServer.port, "RIPE-NCC-HM-MNT");
        assertThat(response, not(containsString("Unreferenced")));
    }

    @Test
    public void show_taginfo_for_unreferenced_object() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--show-taginfo UNUSED-MNT");
        assertThat(response, containsString("Unreferenced"));
    }

    @Test
    public void show_taginfo_for_referenced_object() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--show-taginfo RIPE-NCC-HM-MNT");
        assertThat(response, not(containsString("Unreferenced# 'RIPE-NCC-HM-MNT'")));
    }

    @Test
    public void no_taginfo_help() {
        final String response = DummyWhoisClient.query(QueryServer.port, "help");
        assertThat(response, not(containsString("" +
                "%     --no-taginfo\n" +
                "%           Switches off tagging information.\n")));
    }

    @Test
    public void show_taginfo_help() {
        final String response = DummyWhoisClient.query(QueryServer.port, "help");
        assertThat(response, not(containsString("" +
                "%     --show-taginfo\n" +
                "%           Switches on tagging information.\n")));
    }

    @Test
    public void show_taginfo_and_no_taginfo_shows_default_behaviour() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--no-taginfo --show-taginfo UNUSED-MNT");
        assertThat(response, containsString("ERROR:109: invalid combination of flags passed"));
    }


    @Test
    public void filterTag_include_applies() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B --filter-tag-include unref UNUSED-MNT");
        System.out.println(response);
        assertThat(response, containsString("mntner:         UNUSED-MNT"));
        assertThat(response, not(containsString("person:         Test Person")));
        assertThat(response, containsString("" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITH tag(s): unref\n" +
                "\n" +
                "% Information related to 'UNUSED-MNT'"));
    }

    @Test
    public void filterTag_include_does_not_apply() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B --filter-tag-include unref TP1-TEST");
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITH tag(s): unref\n" +
                "\n" +
                "% Information related to 'TP1-TEST'\n" +
                "\n" +
                "%ERROR:101: no entries found\n" +
                "%\n" +
                "% No entries found in source TEST.\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service version 0.1-TEST (UNDEFINED)\n" +
                "\n" +
                "\n"));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterTag_include_no_argument() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-include UNUSED-MNT");
        assertThat(response, containsString("ERROR:106: no search key specified"));
    }

    @Test
    public void filterTag_include_unknown_tag() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-include incorrect UNUSED-MNT");
        assertThat(response, containsString("" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITH tag(s): incorrect"));
    }

    @Test
    public void filterTag_exclude_applies() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude unref UNUSED-MNT");
        assertThat(response, not(containsString("mntner:         UNUSED-MNT")));
        assertThat(response, containsString("person:         Test Person"));
        assertThat(response, containsString("% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITHOUT tag(s): unref"));
    }

    @Test
    public void filterTag_exclude_does_not_apply() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude unref TP1-TEST");
        assertThat(response, containsString("person:         Test Person"));
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n" +
                "\n" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITHOUT tag(s): unref\n" +
                "\n" +
                "% Information related to 'TP1-TEST'\n" +
                "\n" +
                "person:         Test Person\n" +
                "address:        Street\n" +
                "phone:          +31324243\n" +
                "nic-hdl:        TP1-TEST\n" +
                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service version 0.1-TEST (UNDEFINED)\n" +
                "\n" +
                "\n"));
    }

    @Test
    public void filterTag_exclude_no_argument() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude UNUSED-MNT");
        assertThat(response, containsString("ERROR:106: no search key specified"));
    }

    @Test
    public void filterTag_exclude_nonapplicable_argument() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude incorrect UNUSED-MNT");
        assertThat(response, containsString("mntner:         UNUSED-MNT"));
        assertThat(response, containsString("person:         Test Person"));
        assertThat(response, containsString(
                "% Note: tag filtering is enabled,\n" +
                        "%       Only showing objects WITHOUT tag(s): incorrect"));
    }

    @Test
    public void filterNote_include_on_top_for_longer_list_of_related_objects() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B --filter-tag-include unref RIPE-NCC-HM-MNT");
        assertThat(response, containsString("" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITH tag(s): unref\n" +
                "\n" +
                "% Information related to 'RIPE-NCC-HM-MNT'"));
        assertThat(response, containsString("organisation:   ORG-TEST1-TEST"));
        assertThat(response, not(containsString("mntner:         RIPE-NCC-HM-MNT")));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterNote_exclude_on_top_for_longer_list_of_related_objects() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B --filter-tag-exclude unref RIPE-NCC-HM-MNT");
        assertThat(response, containsString("" +
                "% Note: tag filtering is enabled,\n" +
                "%       Only showing objects WITHOUT tag(s): unref\n" +
                "\n" +
                "% Information related to 'RIPE-NCC-HM-MNT'"));
        assertThat(response, containsString("mntner:         RIPE-NCC-HM-MNT"));
        assertThat(response, containsString("person:         Test Person"));
        assertThat(response, not(containsString("organisation:   ORG-TEST1-TEST")));
    }

    @Test
    public void filterTag_include_and_exclude_cannot_be_combined_with_the_same_argument() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude unref --filter-tag-include unref UNUSED-MNT");
        assertThat(response, containsString("%ERROR:109: invalid combination of flags passed\n" +
                "%\n" +
                "% The flags \"--filter-tag-include (unref)\" and \"--filter-tag-exclude (unref)\" cannot be used together."));
    }

    @Test
    public void filterTag_include_and_exclude_none_apply() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude lame --filter-tag-include placeholder RIPE-NCC-HM-MNT");
        assertThat(response, not(containsString("organisation:   ORG-TEST1-TEST")));
        assertThat(response, not(containsString("mntner:         RIPE-NCC-HM-MNT")));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterTag_include_and_exclude_both_apply_on_different_objects() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude foo --filter-tag-include unref RIPE-NCC-HM-MNT");
        assertThat(response, containsString(
                "% Note: tag filtering is enabled,\n" +
                        "%       Only showing objects WITH tag(s): unref\n" +
                        "%       Only showing objects WITHOUT tag(s): foo"));
        assertThat(response, containsString("organisation:   ORG-TEST1-TEST"));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterTag_include_and_exclude_both_apply_on_same_objects() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude unref --filter-tag-include bar MTAG-MNT");
        assertThat(response, not(containsString("mntner:         MTAG-MNT")));
        assertThat(response, not(containsString("organisation:   ORG-TEST1-TEST")));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterTag_include_and_exclude_only_include_apply() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude lame --filter-tag-include unref RIPE-NCC-HM-MNT");
        assertThat(response, containsString(
                "% Note: tag filtering is enabled,\n" +
                        "%       Only showing objects WITH tag(s): unref\n" +
                        "%       Only showing objects WITHOUT tag(s): lame"));
        assertThat(response, containsString("organisation:   ORG-TEST1-TEST"));
        assertThat(response, not(containsString("mntner:         RIPE-NCC-HM-MNT")));
        assertThat(response, not(containsString("person:         Test Person")));
    }

    @Test
    public void filterTag_include_and_exclude_only_exclude_apply() {
        final String response = DummyWhoisClient.query(QueryServer.port, "--filter-tag-exclude unref --filter-tag-include placeholder RIPE-NCC-HM-MNT");
        assertThat(response, not(containsString("organisation:   ORG-TEST1-TEST")));
        assertThat(response, not(containsString("mntner:         RIPE-NCC-HM-MNT")));
        assertThat(response, not(containsString("person:         Test Person")));
    }
}
