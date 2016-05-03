package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec


@org.junit.experimental.categories.Category(IntegrationTest.class)
class  LirOrganisationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["LIR-ORG": """\
            organisation:   ORG-RIEN1-RIPE
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096
            address:        1016 EB
            address:        Amsterdam
            address:        NETHERLANDS
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            admin-c:        TP1-RIPE
            admin-c:        TP1-RIPE
            abuse-c:        TP1-RIPE
            mnt-ref:        RIPE-NCC-MNT
            mnt-ref:        RIPE-NCC-HM-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
        """
        ]
    }


    def "modify lir attributes with lir password should not be possible"() {
        given:
        syncUpdate(getTransient("LIR-ORG") + "password: hm\npassword: lir")

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-RIPE", "organisation", "ORG-RIEN1-RIPE")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-RIPE
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations
            address:        P.O. Box 10096
            address:        1016 EB
            address:        Amsterdam
            address:        NETHERLANDS
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            admin-c:        TP1-RIPE
            admin-c:        TP1-RIPE
            abuse-c:        TP1-RIPE
            mnt-ref:        RIPE-NCC-MNT
            mnt-ref:        RIPE-NCC-HM-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password: lir
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-RIPE" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-RIPE") == [
                "Referenced organisation can only be changed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to change this reference."]


    }



}

