package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 17/12/2013
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
class SSOSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "NO-SSO-MNT": """\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$ekjY/4Nb\$Jb.THskSsMVVLX5NnU7T80  #nosso
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST
                """,
        ]
    }

    def "add sso to mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: nosso")

        expect:
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$ekjY/4Nb\$Jb.THskSsMVVLX5NnU7T80  #nosso
                auth:        SSO dbase@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: nosso
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
    }

}
