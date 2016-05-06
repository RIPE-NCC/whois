package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec


@org.junit.experimental.categories.Category(IntegrationTest.class)
class  LirOrganisationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
        "LIR-ORG": """\
            organisation:   AUTO-1
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
        """
        ]
    }

    def "modify portal attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096 modified
            phone:          +31111111111
            fax-no:         +31111111111
            e-mail:         nccmodified@ripe.net
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR2-MNT
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(5, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Organisation \"address:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
                "Organisation \"phone:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
                "Organisation \"fax-no:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
                "Organisation \"e-mail:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
                "Organisation \"mnt-by:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it."
        ]
    }

    def "modify portal attributes with ncc password should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096 modified
            phone:          +31111111111
            fax-no:         +31111111111
            e-mail:         nccmodified@ripe.net
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR2-MNT
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    //TODO: RM: abuse-mailbox and org should not be editable with lir mnt
    def "modify other lir-locked attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC) modified
            org-type:       OTHER
            org:            ORG-OTO1-TEST
            abuse-mailbox:  abuse@ripe.net
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password:       lir
            password:       owner3
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Organisation \"org-name:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
                "Organisation \"org-type:\" can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change it.",
        ]
    }

    def "modify other lir-locked attributes with override should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC) modified
            org-type:       OTHER
            org:            ORG-OTO1-TEST
            abuse-mailbox:  abuse@ripe.net
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            override:       denis,override1
        """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }
}

