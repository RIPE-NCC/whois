package net.ripe.db.whois.spec.query


import net.ripe.db.whois.query.QueryMessages
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.jupiter.api.Tag("IntegrationTest")
class HierarchicalSpec extends BaseQueryUpdateSpec {
    def header = """\
% This is the RIPE Database query service.
% The objects are in RPSL format.
%
% The RIPE Database is subject to Terms and Conditions.
% See https://apps.db.ripe.net/docs/HTML-Terms-And-Conditions

"""

    def "M 0/0"() {
      when:
        def everything = query "-M 0/0"

      then:
        everything =~ header
        everything.contains(QueryMessages.illegalRange().toString())
    }

    def "-m 0::/0"() {
      when:
        def everything = query "-m 0::/0"

      then:
        everything =~ header
        everything.contains(QueryMessages.illegalRange().toString())
    }

    def "-m ::0/0"() {
      when:
        def everything = query "-m ::0/0"

      then:
        everything =~ header
        everything.contains(QueryMessages.illegalRange().toString())
    }

    def "-m 0::0/0"() {
      when:
        def everything = query "-m 0::0/0d"

      then:
        everything =~ header
        everything.contains(QueryMessages.uselessIpFlagPassed().toString())
    }
}
