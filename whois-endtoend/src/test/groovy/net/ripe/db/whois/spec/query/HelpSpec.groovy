package net.ripe.db.whois.spec.query

import net.ripe.db.whois.query.domain.QueryMessages
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

class HelpSpec extends BaseQueryUpdateSpec {

    def header = """\
% This is the RIPE Database query service.
% The objects are in RPSL format.
%
% The RIPE Database is subject to Terms and Conditions.
% See http://www.ripe.net/db/support/db-terms-conditions.pdf

"""

    def expectedResponse = header + """\
% NAME
%     whois query server
%
% DESCRIPTION
%     The following options are available:
%"""


    def "help"() {
      when:
        def help = query "heLP"

      then:
        help.contains(expectedResponse)
        grepQueryLog(/PW-QRY-INFO <0\+0\+0>  \d+ms \[127\.0\.0\.1\] --  heLP$/)
    }

    def "HELP"() {
      when:
        def help = query "HELP"

      then:
        help.contains(expectedResponse)
    }

    def "hElP"() {
      when:
        def help = query "hElP"

      then:
        help.contains(expectedResponse)
    }

    def "help 193.0.0.1"() {
      when:
        def help = query "help 193.0.0.1"

      then:
        help =~ header
        help.contains(QueryMessages.noResults("TEST").toString())
    }

    def "help MNTNER"() {
      when:
        def help = query "help MNTNER"

      then:
        help =~ header
        help.contains(QueryMessages.noResults("TEST").toString())
    }

    def "query help"() {
      when:
        def help = query "query help"

      then:
        help =~ header
        help.contains(QueryMessages.noResults("TEST").toString())
    }
}
