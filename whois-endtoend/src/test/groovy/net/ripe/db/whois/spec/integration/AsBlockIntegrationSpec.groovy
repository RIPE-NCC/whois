package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
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
                auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                notify:         unread@ripe.net
                mnt-by:         RIPE-DBM-MNT
                changed:        ripe-dbm@ripe.net 20080806
                changed:        ripe-dbm@ripe.net 20080909
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
                changed:        snigdha.girdhar@gmail.com 20120505
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
                auth:           MD5-PW \$1\$kBdYtA4E\$EBAWVrVm9yBiLzPhAEQH21. # test
                notify:         unread@ripe.net
                mnt-by:         RIPE-DBM-MNT1
                changed:        ripe-dbm@ripe.net 20080806
                changed:        ripe-dbm@ripe.net 20080909
                source:         TEST
                """,

                "":"""\
                mntner:         EXAMPLE-MNT
                descr:          Sample maintainer for example
                admin-c:        JS1-TEST
                tech-c:         JS1-TEST
                upd-to:         john.smith@example.com
                mnt-nfy:        john.smith@example.com
                auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN6.
                notify:         john.smith@example.com
                abuse-mailbox:  abuse@example.com
                mnt-by:         RIPE-DBM-MNT
                referral-by:    RIPE-DBM-MNT
                changed:        john.smith@example.com 20051104
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
                abuse-mailbox:  abuse@example.com
                changed:        john.smith@example.com 20051104
                changed:        john.smith@example.com 20051105
                source:         TEST
        """
        ]
    }

    def "create as-block"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        changed:        snigdha.girdhar@gmail.com
                        mnt-lower:      RIPE-DBM-MNT
                        source:         TEST
                        password:       update
                        """.stripIndent())
        when:
        def response = syncUpdate(update);

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[as-block\] AS500 - AS600/
    }

    def "modify as-block"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        changed:        snigdha.girdhar@gmail.com
                        mnt-lower:      RIPE-DBM-MNT
                        source:         TEST
                        password:       update
                        """.stripIndent())
        when:
        def response = syncUpdate(update)

        then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[as-block\] AS222 - AS333/
    }

    def "create as-block with invalid Maintainer"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        changed:        snigdha.girdhar@gmail.com
                        mnt-lower:      RIPE-DBM-MNT1
                        source:         TEST
                        password:       test
                        """.stripIndent())
        when:
        def response = syncUpdate(update);

        then:
        response =~ /Create FAILED: \[as-block\] AS500 - AS600/
        response =~ /As-block object are maintained by RIPE NCC/

    }

    def "delete as-block"() {
        given:
        def update = new SyncUpdate(data: "" +
                fixtures["AS222 - AS333"].stripIndent() +
                "delete: some reason\n" +
                "password: update")

        when:
        def response = syncUpdate update

        then:
        response =~ /SUCCESS/
        response =~ /Delete SUCCEEDED: \[as-block\] AS222 - AS333/

    }
}
