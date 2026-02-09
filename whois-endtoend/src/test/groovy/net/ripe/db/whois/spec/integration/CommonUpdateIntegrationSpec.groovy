package net.ripe.db.whois.spec.integration
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
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
                source:  TEST
                """,
                "OWNER-MNT": """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                upd-to:      dbtest@ripe.net
                source:      TEST
                """
        ]
    }

    def "send object without source"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """
            mntner:      DEL-MNT
            auth:        SSO person@net.net
            """)

        then:
        response =~ /(?m)^Create FAILED: \[mntner\] DEL-MNT$/
        response =~ /(?m)^\*\*\*Error:   Mandatory attribute "source" is missing$/
    }

    def "send object with space in attribute key"() {
        when:
        def response = syncUpdate("""\
            mntner:      DEL-MNT
            source:      TEST
            mnt-by     : DEV-MNT
            """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ "\\*\\*\\*Error:   \"mnt-by     \" is not a known RPSL attribute"
    }

    def "delete object without source"() {
        when:
        def response = syncUpdate("""\
            mntner: DEV-TST-MNT
            delete: reason
            """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

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

            """)

        when:
        def response = syncUpdate update

        then:
        response =~ """
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            mtner: not-an-object
            """.stripIndent(true)
    }

    def "send object with space in type attribute name"() {
        when:
        def response = syncUpdate("""\
            mntner :      DEL-MNT
            delete:      reason
            source: TEST
            """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            mntner :      DEL-MNT
        """.stripIndent(true)
    }

    def "send object with unbalanced indent type not contains key"() {
        when:
        def response = syncUpdate new SyncUpdate(data: "address: test\n\n" +
                """\
               person:  First Person Error
               address: St James Street
               address: Burnley
               address: UK
               phone:   +44 282 420469
               nic-hdl: FPE1-TEST
               mnt-by:  OWNER-MNT
               source:  TEST
               """)
        then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            address: test

            person:  First Person Error
                           address: St James Street
                           address: Burnley
                           address: UK""".stripIndent(true)
    }

    def "send object with extra spaces before each line"() {
        def update = new SyncUpdate(rawData: "source: owner\n\n" + """
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
        """.stripIndent(true)
    }

    def "send object with too many scopes in apikey"() {
        when:
        def response = syncUpdate("""\
                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP2-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT)
        then:
        response.contains("""
                ***Error:   Authorisation for [person] TP2-TEST failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT
                """.stripIndent(true))
        response.contains("***Warning: Whois scopes can not be more than 2")
    }

    def "comment in source attribute"() {
        when:
        def response = syncUpdate("""\
                mntner:      OWNER-MNT
                descr:       has end of line comment on source
                admin-c:     TP1-TEST
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                upd-to:      dbtest@ripe.net
                source:      TEST #comment
                """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_OWNER_MNT)

        then:
        response =~ /End of line comments not allowed on "source:" attribute/
    }

    def "lowercase source with filtered comment"() {
        when:
        def response = syncUpdate("""\
                mntner:      OWNER-MNT
                descr:       has end of line comment on source
                admin-c:     TP1-TEST
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                upd-to:      dbtest@ripe.net
                source:      Test # Filtered
                """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_OWNER_MNT)

        then:
        response =~ /Cannot submit filtered whois output for updates/
    }

    def "too many references"() {
        when:
        def response = syncUpdate("""\
mntner:      OWNER-MNT
descr:       used to maintain other MNTNERs
admin-c:     TP1-TEST
auth:        SSO person@net.net
""" +
"mnt-by:      OWNER-MNT\n".repeat(101) +
"""upd-to:      dbtest@ripe.net
source:      TEST
""", null, false, getApiKeyDummy().BASIC_AUTH_PERSON_OWNER_MNT)

        then:
        response =~ /Too many references/
    }

    def "too many references and incorrect apikey"() {
        when:
        def response = syncUpdate("""\
mntner:      OWNER-MNT
descr:       used to maintain other MNTNERs
admin-c:     TP1-TEST
auth:        SSO person@net.net
""" +
"mnt-by:      OWNER-MNT\n".repeat(101) +
"""upd-to:      dbtest@ripe.net
source:      TEST
""", null, false, getApiKeyDummy().BASIC_AUTH_TEST_TEST_MNT)
        then:
       response =~ /Too many references/
       !(response =~ /Authorisation for \[mntner\] OWNER-MNT failed/)
    }

}
