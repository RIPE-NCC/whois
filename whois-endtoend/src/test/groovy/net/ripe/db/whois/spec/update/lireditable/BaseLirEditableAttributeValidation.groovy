package net.ripe.db.whois.spec.update.lireditable

import spock.lang.Ignore;

@Ignore("base class don't run tests here directly")
class BaseLirEditableAttributeValidation extends BaseLirEditableAttributes {

    //  MODIFY resource attributes by LIR

    def "modify resource, cannot change org and status (lir-locked) attributes by lir"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR2-TEST 
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${differentStatus}  
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Referenced organisation can only be changed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to change this reference.",
                "status value cannot be changed, you must delete and re-create the object"
        ]
    }

    def "modify resource, cannot delete (some) mandatory lir-unlocked attributes by lir"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        //        org:          ORG-LIR1-TEST # cannot delete, but warning is NOT presented!!
        //        country:      NL            # cannot delete
        //        admin-c:      TP1-TEST      # cannot delete
        //        tech-c:       TP1-TEST      # cannot delete
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Mandatory attribute \"country\" is missing",
                "Mandatory attribute \"admin-c\" is missing",
                "Mandatory attribute \"tech-c\" is missing"]
    }

    def "modify resource, change lir-locked (status) attributes with rs password"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${differentStatus} 
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "status value cannot be changed, you must delete and re-create the object"
        ]
    }

    def "modify resource, add 'single' attributes with rs password"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                netname:      TEST-NET-NAME-2      # added
                country:      NL
                org:          ORG-LIR1-TEST
                org:          ORG-LIR2-TEST        # added
                sponsoring-org: ORG-LIR1-TEST      # added
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                status:       ${differentStatus}   # added
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Attribute \"netname\" appears more than once",
                "Attribute \"org\" appears more than once",
                "Attribute \"status\" appears more than once"
        ]
    }
}
