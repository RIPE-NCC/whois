package net.ripe.db.whois.spec.integration

import com.google.common.collect.Lists
import net.ripe.db.whois.common.source.Source
import net.ripe.db.whois.query.rpki.Roa

import static net.ripe.db.whois.query.rpki.TrustAnchor.ARIN

@org.junit.jupiter.api.Tag("IntegrationTest")
class SearchQuerySpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner:         UPD-MNT
            descr:          description
            admin-c:        TEST-RIPE
            mnt-by:         UPD-MNT
            upd-to:         dbtest@ripe.net
            org:            ORG-TOL1-TEST
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:         TEST
            """,
                "ADMIN-MNT": """\
            mntner:         ADMIN-MNT
            descr:          description
            admin-c:        TEST-RIPE
            mnt-by:         ADMIN-MNT
            upd-to:         dbtest@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:         TEST
            """,
                "ADMIN-PN": """\
            person:         Admin Person
            address:        Admin Road
            address:        Town
            address:        UK
            phone:          +44 282 411141
            nic-hdl:        TEST-RIPE
            mnt-by:         ADMIN-MNT
            source:         TEST
            """,
                "ORG1": """\
            organisation: ORG-TOL1-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      UPD-MNT
            mnt-by:       UPD-MNT
            source:       TEST
            """,
                "ROUTE": """\
            route:         193.4.0.0/16
            descr:          Route
            origin:         AS102
            mnt-by:         ADMIN-MNT
            source:         TEST
            """,
                "ROUTE6": """\
            route6:          2001:1578:0200::/40
            descr:           TEST-ROUTE6
            origin:          AS12726
            mnt-by:          ADMIN-MNT
            source:          TEST
            """
        ]
    }

    def setup() {
        whoisFixture.getSourceContext().setCurrent(Source.slave("TEST"))
    }

    def cleanup() {
        whoisFixture.getSourceContext().removeCurrentSource()
    }

    def "--no-referenced UPD-MNT"() {
      when:
        def response = query "--no-referenced UPD-MNT"

      then:
        response.contains("" +
                "% Information related to 'UPD-MNT'\n" +
                "\n" +
                "mntner:         UPD-MNT\n" +
                "descr:          description\n" +
                "admin-c:        TEST-RIPE\n" +
                "mnt-by:         UPD-MNT\n" +
                "org:            ORG-TOL1-TEST\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service version")
    }

    def "--no-personal UPD-MNT"() {
      when:
        def response = query "--no-personal UPD-MNT"

      then:
        response.contains("" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n" +
                "\n" +
                "% Note: --no-personal means ALL personal data has been filtered from this output.\n" +
                "\n" +
                "% Information related to 'UPD-MNT'\n" +
                "\n" +
                "mntner:         UPD-MNT\n" +
                "descr:          description\n" +
                "admin-c:        TEST-RIPE\n" +
                "mnt-by:         UPD-MNT\n" +
                "org:            ORG-TOL1-TEST\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "descr:          test org\n" +
                "address:        street 5\n" +
                "mnt-ref:        UPD-MNT\n" +
                "mnt-by:         UPD-MNT\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service")
    }

    def "--no-personal --select-types person TEST-RIPE"() {
      when:
        def response = query "--no-personal --select-types person TEST-RIPE"

      then:
        response.contains("" +
                "%ERROR:115: invalid search key\n" +
                "%\n" +
                "% Search key entered is not valid for the specified object type(s)")
    }


    def "roa-validation 193.4.0.0/16AS102 warn not showing up"() {
        when:
        rpkiDataProvider.setRoas(Lists.newArrayList(
                new Roa(6505, 16, "193.4.0.0/16", ARIN)
        ));
        def response = query("--roa-validation --select-types route 193.4.0.0/16AS102")

        then:
        response.contains("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See https://apps.db.ripe.net/docs/HTML-Terms-And-Conditions\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n" +
                "\n" +
                "% Information related to '193.4.0.0/16AS102'\n" +
                "\n" +
                "route:          193.4.0.0/16\n" +
                "descr:          Route\n" +
                "origin:         AS102\n" +
                "mnt-by:         ADMIN-MNT\n" +
                "source:         TEST\n" +
                "\n")
    }

    def "roa-validation 2001:1578:0200::/40AS12726 warn not showing up"() {
        when:
        rpkiDataProvider.setRoas(Lists.newArrayList(
                new Roa(6505, 40, "2001:1578:0200::/40", ARIN)
        ));
        def response = query("--roa-validation --select-types route6 2001:1578:0200::/40AS12726")

        then:
        response.contains("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See https://apps.db.ripe.net/docs/HTML-Terms-And-Conditions\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n" +
                "\n" +
                "% Information related to '2001:1578:200::/40AS12726'\n" +
                "\n" +
                "route6:         2001:1578:200::/40\n" +
                "descr:          TEST-ROUTE6\n" +
                "origin:         AS12726\n" +
                "mnt-by:         ADMIN-MNT\n" +
                "source:         TEST")
    }
}
