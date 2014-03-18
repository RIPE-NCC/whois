package net.ripe.db.whois.spec.query

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(EndToEndTest.class)
class BasicQuerySpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "INETNUM": """\
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
                "INET6NUM": """\
                inet6num:     2001:2002:2003::/64
                netname:      Netname
                descr:        Description
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                status:       OTHER
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "ROUTE": """\
                route:        99.13.0.0/16
                descr:        Route
                origin:       AS10000
                org:          ORG-LIR1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      noreply@ripe.net 20120101
                source:       TEST
                """,
                "ROUTE6": """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         RIPE-NCC-HM-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
                "ROLE": """\
                role:    Abuse Me
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: AB-TEST
                tech-c:  AB-TEST
                nic-hdl: AB-TEST
                abuse-mailbox: abuse@test.net
                mnt-by:  TST-MNT2
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
                "DOMAIN": """\
                domain:         192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         RIPE-NCC-END-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
                "ROUTESET": """\
                route-set:    AS200:RS-CUSTOMERS
                members:      47.247.0.0/16,
                              rs-customers:AS123234:rs-test
                mp-members:   2001:1578::/32,
                              192.233.33.0/24^+,
                              AS123:rs-customers
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                notify:       dbtest@ripe.net
                mnt-by:       RIPE-NCC-END-MNT
                changed:      dbtest@ripe.net 20120101
                source:       TEST
                """
        ]
    }

    // -x, --exact

    def "--exact inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--exact 192.0.0.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--exact inetnum not found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObjectNotFound("--exact 192.0.0.0/16", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--exact inet6num found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--exact 2001:2002:2003::/64", "inet6num", "2001:2002:2003::/64")
    }

    def "--exact inet6num not found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObjectNotFound("--exact 2001:2002:2003::/48", "inet6num", "2001:2002:2003::/64")
    }

    def "--exact route found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObject("--exact 99.13.0.0/16AS10000", "route", "99.13.0.0/16")
    }

    def "--exact route not found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObjectNotFound("--exact 99.13.0.0/24", "route", "99.13.0.0/24AS10000")
    }

    def "--exact route6 found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--exact 2001:600::/32AS10000", "route6", "2001:600::/32")
    }

    def "--exact route6 not found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObjectNotFound("--exact 2001:600::/48", "route6", "2001:600::/48")
    }

    // -l, --one-less

    def "--one-less inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--one-less 192.0.0.0/16", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--one-less inetnum not found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObjectNotFound("--one-less 0/0", "inetnum", "0/0")
    }

    def "--one-less inet6num found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--one-less 2001:2002:2003::/96", "inet6num", "2001:2002:2003::/64")
    }

    def "--one-less inet6num not found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObjectNotFound("--one-less 0::/0", "inet6num", "0::/0")
    }

    def "--one-less route found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObject("--one-less 99.13.0.0/32", "route", "99.13.0.0/16")
    }

    def "--one-less route6 found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--one-less 2001:600::/64AS10000", "route6", "2001:600::/32")
    }

    // -L, --all-less

    def "--all-less inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--all-less 192.0/32", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--all-less inet6num found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--all-less 2001:2002:2003::/128", "inet6num", "::/0")
        queryObject("--all-less 2001:2002:2003::/128", "inet6num", "2001:2002:2003::/64")
    }

    def "--all-less route found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObject("--all-less 99.13.0.0/32", "route", "99.13.0.0/16")
        queryObject("--all-less 99.13.0.0/32", "route", "99.0.0.0/8")
    }

    def "--all-less route6 found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--all-less 2001:600::/48", "route6", "2001:600::/32")
    }

    // -m, --one-more

    def "--one-more inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--one-more 192.0.0.0 - 194.0.0.0", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--one-more inet6num found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--one-more 2001::/16", "inet6num", "2001:2002:2003::/64")
    }

    def "--one-more route found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObject("--one-more 99.13.0.0/8", "route", "99.13.0.0/16")
    }

    def "--one-more route6 found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--one-more 2001:600::/16", "route6", "2001:600::/32")
    }

    // -M, --all-more

    def "--all-more inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--all-more 192.0.0.0 - 194.0.0.0", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "--all-more inet6num found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--all-more 2001:2002::/24", "inet6num", "2001:2002:2003::/64")
    }

    def "--all-more route found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))

      expect:
        queryObject("--all-more 99.0.0.0 - 100.0.0.0", "route", "99.0.0.0/8")
        queryObject("--all-more 99.0.0.0 - 100.0.0.0", "route", "99.13.0.0/16")
    }

    def "--all-more route6 found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--all-more 2001:600::/16", "route6", "2001:600::/32")
    }


    // -C, --no-irt

    def "--no-irt"() {
      given:
        databaseHelper.addObject("" +
                "irt:          irt-test\n" +
                "address:      RIPE NCC\n" +
                "e-mail:       irt-dbtest@ripe.net\n" +
                "auth:         PGPKEY-D83C3FBD\n" +
                "auth:         MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED UNSPECIFIED\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-mnt\n" +
                "mnt-irt:      irt-test\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST")

      expect:
        query("--no-irt 192.0/8") == query("192.0/8")
    }


    // -c, --irt

    def "--irt found irt"() {
      given:
        databaseHelper.addObject("" +
                "irt:          irt-test\n" +
                "address:      RIPE NCC\n" +
                "e-mail:       irt-dbtest@ripe.net\n" +
                "auth:         PGPKEY-D83C3FBD\n" +
                "auth:         MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED UNSPECIFIED\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-mnt\n" +
                "mnt-irt:      irt-test\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST")

      expect:
        queryObject("--irt 192.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("--irt 192.0/8", "irt", "irt-test")
        queryObject("--irt 192.0/8", "organisation", "ORG-LIR1-TEST")
        queryObject("--irt 192.0/8", "person", "Test Person")
    }

    def "--irt no irt found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--irt 192.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("--irt 192.0/8", "irt", "irt-test")
        queryObject("--irt 192.0/8", "organisation", "ORG-LIR1-TEST")
        queryObject("--irt 192.0/8", "person", "Test Person")
    }

    // -b, --abuse-contact  - see AbuseQuerySpec

    // -d, --reverse-domain

    def "--reverse-domain --all-less domain found"() {
      given:
        databaseHelper.addObject(getTransient("DOMAIN"))
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--reverse-domain --all-less 192.0/32", "domain", "192.in-addr.arpa")
        queryObject("--reverse-domain --all-less 192.0/32", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("--reverse-domain --all-less 192.0/32", "inetnum", "0.0.0.0 - 255.255.255.255")
    }

    def "--reverse-domain --one-less domain"() {
      given:
        databaseHelper.addObject(getTransient("DOMAIN"))
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--reverse-domain --one-less 192.0/32", "domain", "192.in-addr.arpa")
        queryObject("--reverse-domain --one-less 192.0/32", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    // -F, --brief

    def "--brief inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--brief 192.0/8", "in", "192.0.0.0 - 192.255.255.255")
        queryObject("--brief 192.0/8", "na", "TEST-NET-NAME")
        queryObject("--brief 192.0/8", "de", "TEST network")
        queryObject("--brief 192.0/8", "cy", "NL")
        queryObject("--brief 192.0/8", "og", "ORG-LIR1-TEST")
        queryObject("--brief 192.0/8", "ac", "TP1-TEST")
        queryObject("--brief 192.0/8", "tc", "TP1-TEST")
        queryObject("--brief 192.0/8", "st", "ALLOCATED UNSPECIFIED")
        queryObject("--brief 192.0/8", "mb", "RIPE-NCC-HM-MNT")
        queryObject("--brief 192.0/8", "ml", "LIR-mnt")
        queryObject("--brief 192.0/8", "so", "TEST # Filtered")

        queryObjectNotFound("--brief 192.0/8", "oa", "ORG-LIR1-TEST") // should it not return the related objects? probably, wait with fixing until it's been suggested to drop this flag altogether

    }

    // -K, --primary-keys

    def "--primary-keys role not found"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObjectNotFound("--primary-keys AB-TEST", "role", "AB-TEST")
    }

    def "--primary-keys inetnum found"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--primary-keys 192.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("--primary-keys 192.0/8", "netname", "TEST-NET-NAME")
    }

    def "--primary-keys route-set found"() {
      given:
        databaseHelper.addObject(getTransient("ROUTESET"))

      expect:
        queryObject("--primary-keys AS200:RS-CUSTOMERS", "route-set", "AS200:RS-CUSTOMERS")
        queryObject("--primary-keys AS200:RS-CUSTOMERS", "members", "47.247.0.0/16,\n                rs-customers:AS123234:rs-test")
        queryObject("--primary-keys AS200:RS-CUSTOMERS", "mp-members", "2001:1578::/32,\n" )
        queryObjectNotFound("--primary-keys AS200:RS-CUSTOMERS", "descr", "test route-set")
    }

    // -G, --no-grouping

    def "--no-grouping route"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE"))
        databaseHelper.updateObject("" +
                "route:       99.0.0.0/8\n" +
                "descr:       parent route object\n" +
                "origin:      AS1000\n" +
                "org:         ORG-LIR1-TEST\n" +
                "mnt-by:      PARENT-MB-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST\n")

      expect:
        queryObject("--no-grouping --all-less 99.13.0.0/32", "route", "99.13.0.0/16")
        queryObject("--no-grouping --all-less 99.13.0.0/32", "route", "99.0.0.0/8")
        queryCountObjects("--no-grouping --all-less 99.13.0.0/32") == 5
        !(query("--no-grouping --all-less 99.13.0.0/32") =~ "% Information related to '99.0.0.0/8AS1000'")
    }

    // -B, --no-filtering

    def "--no-filtering notify & changed preserved"() {
      given:
        databaseHelper.addObject(getTransient("ROUTESET"))

      expect:
        queryObject("--no-filtering AS200:RS-CUSTOMERS", "route-set", "AS200:RS-CUSTOMERS")
        queryObject("--no-filtering AS200:RS-CUSTOMERS", "notify", "dbtest@ripe.net")
        queryObject("--no-filtering AS200:RS-CUSTOMERS", "changed", "dbtest@ripe.net 20120101")
    }

    def "--no-filtering email & changed preserved"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObject("--no-filtering AB-TEST", "role", "Abuse Me")
        queryObject("--no-filtering AB-TEST", "nic-hdl", "AB-TEST")
        queryObject("--no-filtering AB-TEST", "e-mail", "dbtest@ripe.net")
        queryObject("--no-filtering AB-TEST", "changed", "dbtest@ripe.net 20121016")
    }

    // --valid-syntax, --no-valid-syntax

    def "--valid-syntax invalid object"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObjectNotFound("--valid-syntax 2001:2002:2003::/64", "inet6num", "2001:2002:2003::/64")
        queryObject("--valid-syntax 2001:2002:2003::/64", "organisation", "ORG-LIR1-TEST")
        queryObject("--valid-syntax 2001:2002:2003::/64", "person", "Test Person")
    }

    def "--valid-syntax valid object"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObject("--valid-syntax AB-TEST", "role", "Abuse Me")
    }

    def "--no-valid-syntax invalid object"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--no-valid-syntax 2001:2002:2003::/64", "inet6num", "2001:2002:2003::/64")
        queryObjectNotFound("--no-valid-syntax 2001:2002:2003::/64", "organisation", "ORG-LIR1-TEST")
        queryObjectNotFound("--no-valid-syntax 2001:2002:2003::/64", "person", "Test Person")
    }

    def "--no-valid-syntax valid object"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObjectNotFound("--no-valid-syntax AB-TEST", "role", "Abuse Me")
    }

    // -r, --no-referenced

    def "--no-referenced removes referenced object"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--no-referenced 192.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("--no-referenced 192.0/8", "person", "Test Person")
    }

    // --no-personal

    def "--no-personal querying role object"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObjectNotFound("--no-personal AB-TEST", "role", "AB-TEST")
    }

    def "--no-personal querying person object"() {
      given:

      expect:
        queryObjectNotFound("--no-personal TP1-TEST", "person", "Test Person")
        queryObject("TP1-TEST", "person", "Test Person")
    }

    def "--no-personal referenced person removed"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObjectNotFound("--no-personal 192.0/8", "person", "Test Person")
        queryObject("192.0/8", "person", "Test Person")
    }

    def "--no-personal no referenced object in result"() {
      given:
        databaseHelper.addObject(getTransient("ROUTE6"))

      expect:
        queryObject("--no-personal 2001:600::/32AS10000", "route6", "2001:600::/32")
        queryCountObjects("--no-personal 2001:600::/32AS10000") == 1
    }

    // --show-personal

    def "--show-personal"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      when:
        def showPersonal = query("--show-personal 192.0/8")
        def woShowPersonal = query("192.0/8")

      then:
        showPersonal == woShowPersonal

    }

    // -T, --select-types

    def "--select-types found"() {
      given:
        databaseHelper.addObject(getTransient("INET6NUM"))

      expect:
        queryObject("--select-types inet6num 2001:2002:2003::/64", "inet6num", "2001:2002:2003::/64")
        queryObject("--select-types inet6num 2001:2002:2003::/64", "organisation", "ORG-LIR1-TEST")
        queryObject("--select-types inet6num 2001:2002:2003::/64", "person", "Test Person")
    }

    def "--select-types not found"() {
      given:
        databaseHelper.addObject(getTransient("ROLE"))

      expect:
        queryObjectNotFound("--select-types person AB-TEST", "nic-hdl", "AB-TEST")
    }

    // --resource see GrsQuerySpec

    // -q
    def "-q sources"() {
      expect:
        query("-q sources") =~ "\nTEST:3:N:0-0\n"
    }

    def "-q version"() {
      expect:
        query("-q version") =~ "% whois-server-0.1-ENDTOEND"
    }

    def "-q types"() {
      expect:
        query("-q types") =~ "" +
                "inetnum\n" +
                "inet6num\n" +
                "as-block\n" +
                "aut-num\n" +
                "as-set\n" +
                "route\n" +
                "route6\n" +
                "route-set\n" +
                "inet-rtr\n" +
                "filter-set\n" +
                "peering-set\n" +
                "rtr-set\n" +
                "domain\n" +
                "poetic-form\n" +
                "poem\n" +
                "mntner\n" +
                "irt\n" +
                "key-cert\n" +
                "organisation\n" +
                "role\n" +
                "person"
    }

    // --list-sources

    def "--list-sources"() {
      expect:
        query("--list-sources") =~ "TEST:3:N:0-0"
    }

    // --version

    def "--version"() {
      expect:
        query("--version") =~ "% whois-server-0.1-ENDTOEND"
    }

    // --types

    def "--types"() {
      expect:
        query("--types") =~ "" +
                "inetnum\n" +
                "inet6num\n" +
                "as-block\n" +
                "aut-num\n" +
                "as-set\n" +
                "route\n" +
                "route6\n" +
                "route-set\n" +
                "inet-rtr\n" +
                "filter-set\n" +
                "peering-set\n" +
                "rtr-set\n" +
                "domain\n" +
                "poetic-form\n" +
                "poem\n" +
                "mntner\n" +
                "irt\n" +
                "key-cert\n" +
                "organisation\n" +
                "role\n" +
                "person"
    }

    // --template

    def "--template mntner"() {
      expect:
        query("--template mntner").contains("" +
                "mntner:         [mandatory]  [single]     [primary/lookup key]\n" +
                "descr:          [mandatory]  [multiple]   [ ]\n" +
                "org:            [optional]   [multiple]   [inverse key]\n" +
                "admin-c:        [mandatory]  [multiple]   [inverse key]\n" +
                "tech-c:         [optional]   [multiple]   [inverse key]\n" +
                "upd-to:         [mandatory]  [multiple]   [inverse key]\n" +
                "mnt-nfy:        [optional]   [multiple]   [inverse key]\n" +
                "auth:           [mandatory]  [multiple]   [inverse key]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "abuse-mailbox:  [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "referral-by:    [mandatory]  [single]     [ ]\n" +
                "changed:        [mandatory]  [multiple]   [ ]\n" +
                "source:         [mandatory]  [single]     [ ]")
    }

    // --verbose

    def "--verbose person"() {
      expect:
        query("--verbose person").contains(
                "The person class:\n" +
                "\n" +
                "      A person object contains information about technical or\n" +
                "      administrative contact responsible for the object where it is\n" +
                "      referenced.  Once the object is created, the value of the\n" +
                "      \"person:\" attribute cannot be changed.\n" +
                "\n" +
                "person:         [mandatory]  [single]     [lookup key]\n" +
                "address:        [mandatory]  [multiple]   [ ]\n" +
                "phone:          [mandatory]  [multiple]   [ ]\n" +
                "fax-no:         [optional]   [multiple]   [ ]")
    }

    // -V, --client

    def "--client fred,192.0/8 192.0.0.0"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        query("--client fred,192.0.0.0 192.0.0.0") =~ "%ERROR:203: you are not allowed to act as a proxy"
    }

    def "--client fred"() {
      given:
        databaseHelper.addObject(getTransient("INETNUM"))

      expect:
        queryObject("--client fred 192.0/8", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("--client fred 192.0/8", "organisation", "ORG-LIR1-TEST")
        queryObject("--client fred 192.0/8", "person", "Test Person")
    }


    // --list-versions, --diff-versions and show-version see VersionHistorySpec
}
