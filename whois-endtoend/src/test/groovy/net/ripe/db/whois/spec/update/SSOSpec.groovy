package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import spock.lang.Ignore

// TODO: [AH] switch this to IntegrationTest once we got the crowd server dummy instead of the real thing in testlab/prepdev
@org.junit.experimental.categories.Category(EndToEndTest.class)
class SSOSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "NO-SSO-MNT": """\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST
                """,
                "ONE-SSO-MNT": """\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST
                """,
        ]
    }

    def "add SSO to mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        print(fullObj)
    }

    //@Ignore
    def "replace pw with SSO in mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        print(fullObj)
    }

    def "add SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    //@Ignore
    def "replace pw with SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    def "replace SSO with new SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    def "remove SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    //@Ignore
    def "remove pw from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    //@Ignore
    def "remove pw, replace SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    def "remove pw, remove SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["Mandatory attribute \"auth\" is missing"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    def "add 2 SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "SSO person@net.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        print(fullObj)
    }

    def "add invalid SSO to mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] NO-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
    }

    def "add invalid SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO unknown@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
    }

    def "replace SSO with invalid SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
    }

    def "create mntner with pw and SSO"() {
        given:

        expect:
        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        print(fullObj)
    }

    def "create mntner with pw and invalid SSO"() {
        given:

        expect:
        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      NO-SSO-MNT
                referral-by: NO-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST

                password: sso
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[mntner] NO-SSO-MNT"}
        ack.errorMessagesFor("Create", "[mntner] NO-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")
    }

    def "delete mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                referral-by: ONE-SSO-MNT
                changed:     dbtest@ripe.net 20010601
                source:      TEST
                delete:   test

                password: sso
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[mntner] ONE-SSO-MNT"}

        queryObjectNotFound("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT")
    }

}
