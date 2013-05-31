package net.ripe.db.whois.spec.query

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message
import spock.lang.Ignore

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 31/05/2013
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
class GeneralQuerySpec extends BaseSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-mnt
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "RL": """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
        ]}

    def "query pkey only with -K, object returned, no %ERROR:101"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "override: override1")

        expect:
        // "ALLOC-UNS"
        queryObject("-rBG -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

        and:
        ! queryLineMatches("-K 192.0.0.0 - 192.255.255.255", "%ERROR:101")
        queryLineMatches("-K 192.0.0.0 - 192.255.255.255", "inetnum:\\s*192.0.0.0 - 192.255.255.255")
    }

}
