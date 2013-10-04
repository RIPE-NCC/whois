package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class CommonUpdateIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
                "OWNER-MNT": """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                referral-by: OWNER-MNT
                upd-to:      dbtest@ripe.net
                changed:     dbtest@ripe.net
                source:      TEST
                """
        ]
    }

    def "send object without source"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """
            mntner:      DEL-MNT
            password:    password
            """)

        then:
        response =~ /(?m)^Create FAILED: \[mntner\] DEL-MNT$/
        response =~ /(?m)^\*\*\*Error:   Mandatory attribute "source" is missing$/
    }

    def "send object with space in attribute key"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """
            mntner:      DEL-MNT
            source:      TEST
            mnt-by     : DEV-MNT
            password:    password
            """)

        then:
        response =~ "\\*\\*\\*Error:   \"mnt-by     \" is not a known RPSL attribute"
    }

    def "delete object without source"() {
        given:
        def update = new SyncUpdate(data: """\
            mntner: DEV-TST-MNT
            delete: reason
            password: password
            """)

        when:
        def response = syncUpdate update

        then:
        response =~ /(?m)^Delete FAILED: \[mntner\] DEV-TST-MNT$/
        response =~ /(?m)^\*\*\*Error:   Object \[mntner\] DEV-TST-MNT does not exist in the database$/
    }

    def "delete object with NEW keyword"() {
        given:
        def update = new SyncUpdate(forceNew: true, data: """
            mntner: DEV-TST-MNT
            source: TEST
            delete: reason
            password: password
            """)

        when:
        def response = syncUpdate update

        then:
        response =~ /\*\*\*Error:   DELETE is not allowed when keyword NEW is specified/
    }

    def "delete object that looks like RPSL object but is not"() {
        given:
        def update = new SyncUpdate(data: """
            mtner: not-an-object
            source: TEST
            delete: reason

            mntner: TST-MNT
            source: TEST
            delete: reason
            remarks: Some remark

            password: password ## dfsk

            """)

        when:
        def response = syncUpdate update

        then:
        response =~ """
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            mtner: not-an-object
            """.stripIndent()
    }

    def "send object with space in type attribute name"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """
            mntner :      DEL-MNT
            delete:      reason
            password:    password
            source: TEST
            """)

        then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            mntner :      DEL-MNT
        """.stripIndent()
    }

    def "send object with unbalanced indent type not contains key"() {
        when:
        def response = syncUpdate new SyncUpdate(data: "password: owner\n\n" +
                """\
               person:  First Person Error
               address: St James Street
               address: Burnley
               address: UK
               phone:   +44 282 420469
               nic-hdl: FPE1-TEST
               mnt-by:  OWNER-MNT
               changed: dbtest@ripe.net 20121016
               source:  TEST
               """)
        then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            person:  First Person Error
                           address: St James Street
                           address: Burnley
                           address: UK""".stripIndent()
    }

    def "send object with extra spaces before each line"() {
        def update = new SyncUpdate(rawData: "password: owner\n\n" + """
               mntner:  DEV-MNT
               source:  TEST
               """);
        when:
        def response = syncUpdate update
        then:
        response =~ """
            The following object\\(s\\) were found to have ERRORS:

            ---
            Create FAILED: \\[mntner\\] DEV-MNT source: TEST
        """.stripIndent()
    }

    def "send object with too many passwords"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP2-TEST
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                password: pw1
                password: pw2
                password: pw3
                password: pw4
                password: pw5
                password: pw6
                password: pw7
                password: pw8
                password: pw9
                password: pw10
                password: pw11
                password: pw12
                password: pw13
                password: pw14
                password: pw15
                password: pw16
                password: pw17
                password: pw18
                password: pw19
                password: pw20
                password: pw21
                password: pw22
                password: pw23
                password: pw24
                password: pw25
                """)
        then:
        response.contains("***Error:   Too many passwords specified")
    }

    def "non-ascii password"() {
        when:
        def response = syncUpdate new SyncUpdate(data:
                getFixtures().get("OWNER-MNT").stripIndent()
                        + "remarks: changed\n"
                        + "password: C'était\n")
        then:
        response =~ "\\*\\*\\*Error:   Authorisation for \\[mntner\\] OWNER-MNT failed"
    }

    def "comment in source attribute"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """
                mntner:      OWNER-MNT
                descr:       has end of line comment on source
                admin-c:     TP1-TEST
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                referral-by: OWNER-MNT
                upd-to:      dbtest@ripe.net
                changed:     dbtest@ripe.net
                source:      TEST #comment
                password:    owner
                """.stripIndent())

        then:
        response =~ /End of line comments not allowed on "source:" attribute/
    }
}
