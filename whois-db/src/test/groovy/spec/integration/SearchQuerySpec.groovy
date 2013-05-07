package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.source.Source

@org.junit.experimental.categories.Category(IntegrationTest.class)
class SearchQuerySpec extends BaseSpec {
    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner:         UPD-MNT
            descr:          description
            admin-c:        TEST-RIPE
            mnt-by:         UPD-MNT
            referral-by:    ADMIN-MNT
            upd-to:         dbtest@ripe.net
            org:            ORG-TOL1-TEST
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed:        dbtest@ripe.net 20120707
            source:         TEST
            """,
                "ADMIN-MNT": """\
            mntner:         ADMIN-MNT
            descr:          description
            admin-c:        TEST-RIPE
            mnt-by:         ADMIN-MNT
            referral-by:    ADMIN-MNT
            upd-to:         dbtest@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed:        dbtest@ripe.net 20120707
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
            changed:        dbtest@ripe.net 20120101
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
            changed:      dbtest@ripe.net 20120505
            source:       TEST
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
                "referral-by:    ADMIN-MNT\n" +
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
                "referral-by:    ADMIN-MNT\n" +
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
}
