package net.ripe.db.whois.spec.integration

@org.junit.jupiter.api.Tag("IntegrationTest")
class AsBlockIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "RIPE-DBM-MNT":"""\
                mntner:         RIPE-DBM-MNT
                descr:          Mntner for creating new person objects.
                admin-c:        JS1-TEST
                tech-c:         JS1-TEST
                upd-to:         unread@ripe.net
                mnt-nfy:        unread@ripe.net
                auth:           SSO person@net.net
                notify:         unread@ripe.net
                mnt-by:         RIPE-DBM-MNT
                source:         TEST
                """,
                "AS222 - AS333":"""\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-DBM-MNT
                source:         TEST
                """,

                "RIPE-DBM-MNT1":"""\
                mntner:         RIPE-DBM-MNT1
                descr:          Mntner for creating new person objects.
                admin-c:        JS1-TEST
                tech-c:         JS1-TEST
                upd-to:         unread@ripe.net
                mnt-nfy:        unread@ripe.net
                auth:           SSO test@ripe.net
                notify:         unread@ripe.net
                mnt-by:         RIPE-DBM-MNT1
                source:         TEST
                """,

                "":"""\
                mntner:         EXAMPLE-MNT
                descr:          Sample maintainer for example
                admin-c:        JS1-TEST
                tech-c:         JS1-TEST
                upd-to:         john.smith@example.com
                mnt-nfy:        john.smith@example.com
                auth:           SSO person@net.net
                notify:         john.smith@example.com
                mnt-by:         RIPE-DBM-MNT
                source:         TEST # Filtered
                """,

                "Test-PN":"""\
                person:         John Smith
                address:        Example LTD
                                High street 12
                                St.Mery Mead
                                Essex, UK
                phone:          +44 1737 892 004
                e-mail:         john.smith@example.com
                nic-hdl:        JS1-TEST
                remarks:        *******************************
                remarks:        This object is only an example!
                remarks:        *******************************
                mnt-by:         EXAMPLE-MNT
                source:         TEST
        """
        ]
    }

    def "create as-block"() {
        when:
        def response = syncUpdate("""
                        as-block:       AS500 - AS600
                        descr:          ARIN ASN block
                        remarks:        These AS numbers are further assigned by ARIN
                        remarks:        to ARIN members and end-users in the ARIN region.
                        remarks:        Authoritative registration information for AS
                        remarks:        Numbers within this block remains in the ARIN
                        remarks:        whois database: whois.arin.net or
                        remarks:        web site: http://www.arin.net
                        remarks:        You may find aut-num objects for AS Numbers
                        remarks:        within this block in the RIPE Database where a
                        remarks:        routing policy is published in the RIPE Database
                        mnt-by:         RIPE-DBM-MNT
                        mnt-lower:      RIPE-DBM-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[as-block\] AS500 - AS600/
    }

    def "modify as-block"() {
        when:
        def response = syncUpdate("""
                        as-block:       AS222 - AS333
                        descr:          ARIN ASN block
                        remarks:        These AS numbers are further assigned by ARIN
                        remarks:        to ARIN members and end-users in the ARIN region.
                        remarks:        Authoritative registration information for AS
                        remarks:        Characters within this block remains in the ARIN
                        remarks:        whois database: whois.arin.net or
                        remarks:        web site: http://www.arin.net
                        remarks:        You may find aut-num objects for AS Numbers
                        remarks:        within this block in the RIPE Database where a
                        remarks:        routing policy is published in the RIPE Database
                        mnt-by:         RIPE-DBM-MNT
                        mnt-lower:      RIPE-DBM-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[as-block\] AS222 - AS333/
    }

    def "create as-block with invalid Maintainer"() {
        when:

        def response = syncUpdate("""
                        as-block:       AS500 - AS600
                        descr:          ARIN ASN block
                        remarks:        These AS numbers are further assigned by ARIN
                        remarks:        to ARIN members and end-users in the ARIN region.
                        remarks:        Authoritative registration information for AS
                        remarks:        Numbers within this block remains in the ARIN
                        remarks:        whois database: whois.arin.net or
                        remarks:        web site: http://www.arin.net
                        remarks:        You may find aut-num objects for AS Numbers
                        remarks:        within this block in the RIPE Database where a
                        remarks:        routing policy is published in the RIPE Database
                        mnt-by:         RIPE-DBM-MNT1
                        mnt-lower:      RIPE-DBM-MNT1
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_TEST_TEST_MNT)

        then:
        response =~ /Create FAILED: \[as-block\] AS500 - AS600/
        response =~ /As-block object are maintained by RIPE NCC/

    }

    def "delete as-block"() {
        when:

        def response = syncUpdate(
                fixtures["AS222 - AS333"].stripIndent(true) +
                "delete: some reason\n", null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/
        response =~ /Delete SUCCEEDED: \[as-block\] AS222 - AS333/
    }
}
