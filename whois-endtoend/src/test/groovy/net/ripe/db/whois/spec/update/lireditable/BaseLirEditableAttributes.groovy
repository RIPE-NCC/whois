package net.ripe.db.whois.spec.update.lireditable


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class BaseLirEditableAttributes extends BaseQueryUpdateSpec {

    def createMandatory(String type,
                        String keyValue,
                        String statusValue,
                        String ripeMntnerValue) {
        return """\
                ${type}: ${keyValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${statusValue}
                mnt-by:       ${ripeMntnerValue}
                mnt-by:       LIR-MNT
                source:       TEST
                """.stripIndent(true)
    }

    def createExtra(String type,
                    String keyValue,
                    String statusValue,
                    String ripeMntnerValue) {
        return """\
                ${type}: ${keyValue}
                netname:      TEST-NET-NAME
                descr:        some description  # extra
                country:      NL
                geoloc:       0.0 0.0           # extra
                language:     NL                # extra
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${statusValue}
                mnt-by:       ${ripeMntnerValue}
                mnt-by:       LIR-MNT
                remarks:      a new remark      # extra
                notify:       notify@ripe.net   # extra
                mnt-lower:    LIR-MNT           # extra
                mnt-routes:   OWNER-MNT         # extra
                mnt-domains:  DOMAINS-MNT       # extra
                mnt-irt:      IRT-TEST          # extra
                source:       TEST
                """.stripIndent(true)
    }

    def createRipeNccMntner(String type,
                            String keyValue,
                            String statusValue,
                            String ripeMntnerValue) {
        return """\
                ${type}: ${keyValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${statusValue}
                mnt-by:       ${ripeMntnerValue}
                mnt-by:       LIR-MNT
                mnt-lower:    ${ripeMntnerValue}  # ripe-ncc-mnt
                mnt-routes:   ${ripeMntnerValue}  # ripe-ncc-mnt
                mnt-domains:  ${ripeMntnerValue}  # ripe-ncc-mnt
                source:       TEST
                """.stripIndent(true)
    }

    def createExtraRipeNccMntner(String type,
                                 String keyValue,
                                 String statusValue,
                                 String ripeMntnerValue) {
        return """\
                ${type}: ${keyValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${statusValue}
                mnt-by:       ${ripeMntnerValue}
                mnt-by:       LIR-MNT
                mnt-lower:    ${ripeMntnerValue}  # ripe-ncc-mnt
                mnt-lower:    LIR-MNT          # extra
                mnt-routes:   ${ripeMntnerValue}  # ripe-ncc-mnt
                mnt-routes:   OWNER-MNT        # extra
                mnt-domains:  ${ripeMntnerValue}  # ripe-ncc-mnt
                mnt-domains:  DOMAINS-MNT      # extra
                source:       TEST
                """.stripIndent(true)
    }

    def createIrtTest() {
        return """\
                irt:          IRT-TEST
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest@ripe.net
                notify:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """.stripIndent(true)
    }

    def createIrt2Test() {
        return """\
                irt:          IRT-2-TEST
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest@ripe.net
                notify:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """.stripIndent(true)
    }

    def createDomains2Mnt() {
        return """\
                mntner:      DOMAINS2-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_domains@ripe.net
                mnt-nfy:     mntnfy_domains@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$anTWxMgQ\$8aBWq5u5ZFHLA5aeZsSxG0  #domains
                mnt-by:      DOMAINS2-MNT
                source:      TEST
                """.stripIndent(true)
    }
}
