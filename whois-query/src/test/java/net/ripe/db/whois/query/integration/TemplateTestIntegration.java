package net.ripe.db.whois.query.integration;


import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class TemplateTestIntegration extends AbstractQueryIntegrationTest {

    @BeforeEach
    public void startupWhoisServer() {
        queryServer.start();
    }

    @AfterEach
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void check_template() {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "-t route");
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "route:          [mandatory]  [single]     [primary/lookup key]\n" +
                "descr:          [optional]   [multiple]   [ ]\n" +
                "origin:         [mandatory]  [single]     [primary/inverse key]\n" +
                "pingable:       [optional]   [multiple]   [ ]\n" +
                "ping-hdl:       [optional]   [multiple]   [inverse key]\n" +
                "holes:          [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [multiple]   [inverse key]\n" +
                "member-of:      [optional]   [multiple]   [inverse key]\n" +
                "inject:         [optional]   [multiple]   [ ]\n" +
                "aggr-mtd:       [optional]   [single]     [ ]\n" +
                "aggr-bndry:     [optional]   [single]     [ ]\n" +
                "export-comps:   [optional]   [single]     [ ]\n" +
                "components:     [optional]   [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "created:        [generated]  [single]     [ ]\n" +
                "last-modified:  [generated]  [single]     [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service"));
    }

    @Test
    public void check_verbose() {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "-v route6");
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "The route6 class:\n" +
                "\n" +
                "      Each interAS route (also referred to as an interdomain route)\n" +
                "      in IPv6 domain originated by an AS is specified using a route6 \n" +
                "      object. The \"route6:\" attribute is the address prefix of the \n" +
                "      route and the \"origin:\" attribute is the AS number of the AS \n" +
                "      that originates the route into the interAS routing system.\n" +
                "\n" +
                "route6:         [mandatory]  [single]     [primary/lookup key]\n" +
                "descr:          [optional]   [multiple]   [ ]\n" +
                "origin:         [mandatory]  [single]     [primary/inverse key]\n" +
                "pingable:       [optional]   [multiple]   [ ]\n" +
                "ping-hdl:       [optional]   [multiple]   [inverse key]\n" +
                "holes:          [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [multiple]   [inverse key]\n" +
                "member-of:      [optional]   [multiple]   [inverse key]\n" +
                "inject:         [optional]   [multiple]   [ ]\n" +
                "aggr-mtd:       [optional]   [single]     [ ]\n" +
                "aggr-bndry:     [optional]   [single]     [ ]\n" +
                "export-comps:   [optional]   [single]     [ ]\n" +
                "components:     [optional]   [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "created:        [generated]  [single]     [ ]\n" +
                "last-modified:  [generated]  [single]     [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "The content of the attributes of the route6 class are defined below:\n" +
                "\n" +
                "route6\n"));

        assertThat(response, containsString("" +
                "pingable\n" +
                "\n" +
                "   Allows a network operator to advertise an IP address of a node that\n" +
                "   should be reachable from outside networks. This node can be used as a\n" +
                "   destination address for diagnostic tests. The IP address must be\n" +
                "   within the address range of the prefix containing this attribute.\n" +
                "\n" +
                "     \n"));
    }

    @Test
    public void verbose_description() {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "-v inetnum");
        assertThat(response, containsString("""
                The inetnum class:

                      An inetnum object contains information on allocations and
                      assignments of IPv4 address space.

                inetnum:        [mandatory]  [single]     [primary/lookup key]
                netname:        [mandatory]  [single]     [lookup key]
                descr:          [optional]   [multiple]   [ ]
                country:        [mandatory]  [multiple]   [ ]
                geofeed:        [optional]   [single]     [ ]
                geoloc:         [optional]   [single]     [ ]
                prefixlen:      [optional]   [single]     [ ]
                language:       [optional]   [multiple]   [ ]
                org:            [optional]   [single]     [inverse key]
                sponsoring-org: [optional]   [single]     [inverse key]
                admin-c:        [mandatory]  [multiple]   [inverse key]
                tech-c:         [mandatory]  [multiple]   [inverse key]
                abuse-c:        [optional]   [single]     [inverse key]
                status:         [mandatory]  [single]     [ ]
                assignment-size:[optional]   [single]     [ ]
                remarks:        [optional]   [multiple]   [ ]
                notify:         [optional]   [multiple]   [inverse key]
                mnt-by:         [mandatory]  [multiple]   [inverse key]
                mnt-lower:      [optional]   [multiple]   [inverse key]
                mnt-domains:    [optional]   [multiple]   [inverse key]
                mnt-routes:     [optional]   [multiple]   [inverse key]
                mnt-irt:        [optional]   [multiple]   [inverse key]
                created:        [generated]  [single]     [ ]
                last-modified:  [generated]  [single]     [ ]
                source:         [mandatory]  [single]     [ ]

                The content of the attributes of the inetnum class are defined below:

                inetnum

                   Specifies a range of IPv4 that inetnum object presents. The ending
                   address should be greater than the starting one.

                     <ipv4-address> - <ipv4-address>

                netname

                   The name of a range of IP address space.

                     Made up of letters, digits, the character underscore "_",
                     and the character hyphen "-"; the first character of a name
                     must be a letter, and the last character of a name must be a
                     letter or a digit.

                descr

                   A short description related to the object.

                     A sequence of ASCII characters.

                country

                   Identifies the country.

                     Valid two-letter ISO 3166 country code.

                geofeed

                   A URL referencing a CSV file containing geolocation data for the
                   resource.

                     Geofeed is a self-published format for IP geolocation data.
                     A URL referencing a CSV file (described by RFC8805) containing
                     geolocation data for the resource.
                     The URL must be valid and it must specify the HTTPS protocol.

                geoloc

                   The location coordinates for the resource.

                     Location coordinates of the resource, in decimal degrees notation.
                     Format is latitude followed by longitude, separated by a space.
                     Latitude ranges from [-90,+90] and longitude from [-180,+180]

                prefixlen

                   A URL referencing a CSV file containing prefix length data for the
                   resource.

                     A URL referencing a CSV file (described by draft-ietf-opsawg-prefix-lengths) containing
                     additional metadata about how the prefix is used or subdivided.
                     The URL must be valid and it must specify the HTTPS protocol.

                language

                   Identifies the language.

                     Valid two-letter ISO 639-1 language code.

                org

                   Points to an existing organisation object representing the entity that
                   holds the resource.

                     The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits
                     followed by a source specification.  The first digit must not be "0".
                     Source specification starts with "-" followed by source name up to
                     9-character length.

                sponsoring-org

                   Points to an existing organisation object representing the sponsoring
                   organisation responsible for the resource.

                     The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits
                     followed by a source specification.  The first digit must not be "0".
                     Source specification starts with "-" followed by source name up to
                     9-character length.

                admin-c

                   References an on-site administrative contact.

                     From 2 to 4 characters optionally followed by up to 6 digits
                     optionally followed by a source specification.  The first digit
                     must not be "0".  Source specification starts with "-" followed
                     by source name up to 9-character length.

                tech-c

                   References a technical contact.

                     From 2 to 4 characters optionally followed by up to 6 digits
                     optionally followed by a source specification.  The first digit
                     must not be "0".  Source specification starts with "-" followed
                     by source name up to 9-character length.

                abuse-c

                   References an abuse contact. This can only be a ROLE object containing
                   an "abuse-mailbox:" attribute. Making this reference will remove any
                   query limits for the ROLE object. These ROLE objects are considered to
                   include only commercial data.

                     From 2 to 4 characters optionally followed by up to 6 digits
                     optionally followed by a source specification.  The first digit
                     must not be "0".  Source specification starts with "-" followed
                     by source name up to 9-character length.

                status

                   Specifies the status of the resource.

                     Status can have one of these values:
                    \s
                     o ALLOCATED PA
                     o ALLOCATED UNSPECIFIED
                     o LIR-PARTITIONED PA
                     o SUB-ALLOCATED PA
                     o AGGREGATED-BY-LIR
                     o ALLOCATED-ASSIGNED PA
                     o ASSIGNED PA
                     o ASSIGNED PI
                     o ASSIGNED ANYCAST
                     o LEGACY

                assignment-size

                   Specifies the size of blocks assigned to end users from this aggregated inet(6)num assignment.
                   The maximum assignment size for inetnum is 32 and for inet6num is 128

                     Specifies a numeric value.

                remarks

                   Contains remarks.

                     A sequence of ASCII characters.

                notify

                   Specifies the e-mail address to which notifications of changes to an
                   object should be sent. This attribute is filtered from the default
                   whois output.

                     An e-mail address as defined in RFC 2822.

                mnt-by

                   Specifies the identifier of a registered mntner object used for
                   authorisation of operations performed with the object that contains
                   this attribute.

                     Made up of letters, digits, the character underscore "_",
                     and the character hyphen "-"; the first character of a name
                     must be a letter, and the last character of a name must be a
                     letter or a digit.  The following words are reserved by
                     RPSL, and they can not be used as names:
                    \s
                      any as-any rs-any peeras and or not atomic from to at
                      action accept announce except refine networks into inbound
                      outbound
                    \s
                     Names starting with certain prefixes are reserved for
                     certain object types.  Names starting with "as-" are
                     reserved for as set names.  Names starting with "rs-" are
                     reserved for route set names.  Names starting with "rtrs-"
                     are reserved for router set names. Names starting with
                     "fltr-" are reserved for filter set names. Names starting
                     with "prng-" are reserved for peering set names. Names
                     starting with "irt-" are reserved for irt names.

                mnt-lower

                   Specifies the identifier of a registered mntner object used for
                   hierarchical authorisation. Protects creation of objects directly (one
                   level) below in the hierarchy of an object type. The authentication
                   method of this maintainer object will then be used upon creation of
                   any object directly below the object that contains the "mnt-lower:"
                   attribute.

                     Made up of letters, digits, the character underscore "_",
                     and the character hyphen "-"; the first character of a name
                     must be a letter, and the last character of a name must be a
                     letter or a digit.  The following words are reserved by
                     RPSL, and they can not be used as names:
                    \s
                      any as-any rs-any peeras and or not atomic from to at
                      action accept announce except refine networks into inbound
                      outbound
                    \s
                     Names starting with certain prefixes are reserved for
                     certain object types.  Names starting with "as-" are
                     reserved for as set names.  Names starting with "rs-" are
                     reserved for route set names.  Names starting with "rtrs-"
                     are reserved for router set names. Names starting with
                     "fltr-" are reserved for filter set names. Names starting
                     with "prng-" are reserved for peering set names. Names
                     starting with "irt-" are reserved for irt names.

                mnt-domains

                   Specifies the identifier of a registered mntner object used for
                   reverse domain authorisation. Protects domain objects. The
                   authentication method of this maintainer object will be used for any
                   encompassing reverse domain object.

                     Made up of letters, digits, the character underscore "_",
                     and the character hyphen "-"; the first character of a name
                     must be a letter, and the last character of a name must be a
                     letter or a digit.  The following words are reserved by
                     RPSL, and they can not be used as names:
                    \s
                      any as-any rs-any peeras and or not atomic from to at
                      action accept announce except refine networks into inbound
                      outbound
                    \s
                     Names starting with certain prefixes are reserved for
                     certain object types.  Names starting with "as-" are
                     reserved for as set names.  Names starting with "rs-" are
                     reserved for route set names.  Names starting with "rtrs-"
                     are reserved for router set names. Names starting with
                     "fltr-" are reserved for filter set names. Names starting
                     with "prng-" are reserved for peering set names. Names
                     starting with "irt-" are reserved for irt names.

                mnt-routes

                   This attribute references a maintainer object which is used in
                   determining authorisation for the creation of route objects.
                   After the reference to the maintainer, an optional list of
                   prefix ranges inside of curly braces or the keyword "ANY" may
                   follow. The default, when no additional set items are
                   specified, is "ANY" or all more specifics. Please refer to
                   RFC-2622 for more information.

                     <mnt-name> [ { list of <address-prefix-range> } | ANY ]

                mnt-irt

                   May appear in an inetnum or inet6num object. It points to an irt
                   object representing a Computer Security Incident Response Team (CSIRT)
                   that handles security incidents for the address space specified by the
                   inetnum or inet6num object.

                     An irt name is made up of letters, digits, the character
                     underscore "_", and the character hyphen "-"; it must start
                     with "irt-", and the last character of a name must be a
                     letter or a digit.

                created

                   This attributes reflects when the object was created in
                   ISO8601 format (yyyy-MM-dd'T'HH:mm:ssZ).

                     Attribute generated by server.

                last-modified

                   This attributes reflects when the object was last changed in
                   ISO8601 format (yyyy-MM-dd'T'HH:mm:ssZ).

                     Attribute generated by server.

                source

                   Specifies the registry where the object is registered. Should be
                   "RIPE" for the RIPE Database.

                     Made up of letters, digits, the character underscore "_",
                     and the character hyphen "-"; the first character of a
                     registry name must be a letter, and the last character of a
                     registry name must be a letter or a digit."""));
    }


    @Test
    public void verbose_description_autnum() {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "-v aut-num");
        assertThat(response, containsString("" +
                "The aut-num class:\n" +
                "\n" +
                "      An object of the aut-num class is a database representation of \n" +
                "      an Autonomous System (AS), which is a group of IP networks operated \n" +
                "      by one or more network operators that has a single and clearly \n" +
                "      defined external routing policy.\n" +
                "\n" +
                "aut-num:        [mandatory]  [single]     [primary/lookup key]\n" +
                "as-name:        [mandatory]  [single]     [ ]\n" +
                "descr:          [optional]   [multiple]   [ ]\n" +
                "member-of:      [optional]   [multiple]   [inverse key]\n" +
                "import-via:     [optional]   [multiple]   [ ]\n" +
                "import:         [optional]   [multiple]   [ ]\n" +
                "mp-import:      [optional]   [multiple]   [ ]\n" +
                "export-via:     [optional]   [multiple]   [ ]\n" +
                "export:         [optional]   [multiple]   [ ]\n" +
                "mp-export:      [optional]   [multiple]   [ ]\n" +
                "default:        [optional]   [multiple]   [ ]\n" +
                "mp-default:     [optional]   [multiple]   [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [single]     [inverse key]\n" +
                "sponsoring-org: [optional]   [single]     [inverse key]\n" +
                "admin-c:        [mandatory]  [multiple]   [inverse key]\n" +
                "tech-c:         [mandatory]  [multiple]   [inverse key]\n" +
                "abuse-c:        [optional]   [single]     [inverse key]\n" +
                "status:         [generated]  [single]     [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "created:        [generated]  [single]     [ ]\n" +
                "last-modified:  [generated]  [single]     [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "The content of the attributes of the aut-num class are defined below:\n" +
                "\n" +
                "aut-num\n" +
                "\n" +
                "   The autonomous system number.\n" +
                "\n" +
                "     An \"AS\" string followed by an integer in the range\n" +
                "     from 0 to 4294967295\n" +
                "\n" +
                "as-name\n" +
                "\n" +
                "   A descriptive name associated with an AS.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.  The following words are reserved by\n" +
                "     RPSL, and they can not be used as names:\n" +
                "     \n" +
                "      any as-any rs-any peeras and or not atomic from to at\n" +
                "      action accept announce except refine networks into inbound\n" +
                "      outbound\n" +
                "     \n" +
                "     Names starting with certain prefixes are reserved for\n" +
                "     certain object types.  Names starting with \"as-\" are\n" +
                "     reserved for as set names.  Names starting with \"rs-\" are\n" +
                "     reserved for route set names.  Names starting with \"rtrs-\"\n" +
                "     are reserved for router set names. Names starting with\n" +
                "     \"fltr-\" are reserved for filter set names. Names starting\n" +
                "     with \"prng-\" are reserved for peering set names. Names\n" +
                "     starting with \"irt-\" are reserved for irt names.\n" +
                "\n" +
                "descr\n" +
                "\n" +
                "   A short description related to the object.\n" +
                "\n" +
                "     A sequence of ASCII characters.\n" +
                "\n" +
                "member-of\n" +
                "\n" +
                "   This attribute can be used in the route, aut-num and inet-rtr classes.\n" +
                "   The value of the \"member-of:\" attribute identifies a set object that\n" +
                "   this object wants to be a member of. This claim, however, should be\n" +
                "   acknowledged by a respective \"mbrs-by-ref:\" attribute in the\n" +
                "   referenced object.\n" +
                "\n" +
                "     An as-set name is made up of letters, digits, the\n" +
                "     character underscore \"_\", and the character hyphen \"-\"; it\n" +
                "     must start with \"as-\", and the last character of a name must\n" +
                "     be a letter or a digit.\n" +
                "     \n" +
                "     An as-set name can also be hierarchical.  A hierarchical set\n" +
                "     name is a sequence of set names and AS numbers separated by\n" +
                "     colons \":\".  At least one component of such a name must be\n" +
                "     an actual set name (i.e. start with \"as-\").  All the set\n" +
                "     name components of a hierarchical as-name have to be as-set\n" +
                "     names.\n" +
                "\n" +
                "import-via\n" +
                "\n" +
                "   Specifies an import policy expression targeted at a non-adjacent\n" +
                "   network.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-2>]\n" +
                "     afi <afi-list>\n" +
                "     <peering-1>\n" +
                "     from <peering-2> [action <action-1>; <action-2>; ... <action-N>;]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     <peering-3>\n" +
                "     from <peering-M> [action <action-1>; <action-2>; ... <action-N>;]\n" +
                "     accept (<filter>|<filter> except <importexpression>|\n" +
                "             <filter> refine <importexpression>)\n" +
                "\n" +
                "import\n" +
                "\n" +
                "   Specifies import policy expression.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-1>]\n" +
                "     from <peering-1> [action <action-1>]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     from <peering-N> [action <action-N>]\n" +
                "     accept <filter>\n" +
                "\n" +
                "mp-import\n" +
                "\n" +
                "   Specifies multiprotocol import policy expression.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-1>]\n" +
                "     afi <afi-list>\n" +
                "     from <peering-1> [action <action-1>]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     from <peering-N> [action <action-N>]\n" +
                "     accept (<filter>|<filter> except <importexpression>|\n" +
                "             <filter> refine <importexpression>)\n" +
                "\n" +
                "export-via\n" +
                "\n" +
                "   Specifies an export policy expression targeted at a non-adjacent\n" +
                "   network.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-2>]   \n" +
                "     afi <afi-list>\n" +
                "     <peering-1>\n" +
                "     to <peering-2> [action <action-1>; <action-2>; ... <action-N>;]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     <peering-3>\n" +
                "     to <peering-M> [action <action-1>; <action-2>; ... <action-N>;]\n" +
                "     announce <filter>\n" +
                "\n" +
                "export\n" +
                "\n" +
                "   Specifies an export policy expression.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-1>]\n" +
                "     to <peering-1> [action <action-1>]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     to <peering-N> [action <action-N>]\n" +
                "     announce <filter>\n" +
                "\n" +
                "mp-export\n" +
                "\n" +
                "   Specifies a multiprotocol export policy expression.\n" +
                "\n" +
                "     [protocol <protocol-1>] [into <protocol-1>]\n" +
                "     afi <afi-list>\n" +
                "     to <peering-1> [action <action-1>]\n" +
                "         .\n" +
                "         .\n" +
                "         .\n" +
                "     to <peering-N> [action <action-N>]\n" +
                "     announce <filter>\n" +
                "\n" +
                "default\n" +
                "\n" +
                "   Specifies default routing policies.\n" +
                "\n" +
                "     to <peering> [action <action>] [networks <filter>]\n" +
                "\n" +
                "mp-default\n" +
                "\n" +
                "   Specifies default multiprotocol routing policies.\n" +
                "\n" +
                "     to <peering> [action <action>] [networks <filter>]\n" +
                "\n" +
                "remarks\n" +
                "\n" +
                "   Contains remarks.\n" +
                "\n" +
                "     A sequence of ASCII characters.\n" +
                "\n" +
                "org\n" +
                "\n" +
                "   Points to an existing organisation object representing the entity that\n" +
                "   holds the resource.\n" +
                "\n" +
                "     The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits\n" +
                "     followed by a source specification.  The first digit must not be \"0\".\n" +
                "     Source specification starts with \"-\" followed by source name up to\n" +
                "     9-character length.\n" +
                "\n" +
                "sponsoring-org\n" +
                "\n" +
                "   Points to an existing organisation object representing the sponsoring\n" +
                "   organisation responsible for the resource.\n" +
                "\n" +
                "     The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits\n" +
                "     followed by a source specification.  The first digit must not be \"0\".\n" +
                "     Source specification starts with \"-\" followed by source name up to\n" +
                "     9-character length.\n" +
                "\n" +
                "admin-c\n" +
                "\n" +
                "   References an on-site administrative contact.\n" +
                "\n" +
                "     From 2 to 4 characters optionally followed by up to 6 digits\n" +
                "     optionally followed by a source specification.  The first digit\n" +
                "     must not be \"0\".  Source specification starts with \"-\" followed\n" +
                "     by source name up to 9-character length.\n" +
                "\n" +
                "tech-c\n" +
                "\n" +
                "   References a technical contact.\n" +
                "\n" +
                "     From 2 to 4 characters optionally followed by up to 6 digits\n" +
                "     optionally followed by a source specification.  The first digit\n" +
                "     must not be \"0\".  Source specification starts with \"-\" followed\n" +
                "     by source name up to 9-character length.\n" +
                "\n" +
                "abuse-c\n" +
                "\n" +
                "   References an abuse contact. This can only be a ROLE object containing\n" +
                "   an \"abuse-mailbox:\" attribute. Making this reference will remove any\n" +
                "   query limits for the ROLE object. These ROLE objects are considered to\n" +
                "   include only commercial data.\n" +
                "\n" +
                "     From 2 to 4 characters optionally followed by up to 6 digits\n" +
                "     optionally followed by a source specification.  The first digit\n" +
                "     must not be \"0\".  Source specification starts with \"-\" followed\n" +
                "     by source name up to 9-character length.\n" +
                "\n" +
                "status\n" +
                "\n" +
                "   Specifies the status of the resource.\n" +
                "\n" +
                "     Status can have one of these values:\n" +
                "     \n" +
                "     o ASSIGNED\n" +
                "     o LEGACY\n" +
                "     o OTHER\n" +
                "\n" +
                "notify\n" +
                "\n" +
                "   Specifies the e-mail address to which notifications of changes to an\n" +
                "   object should be sent. This attribute is filtered from the default\n" +
                "   whois output.\n" +
                "\n" +
                "     An e-mail address as defined in RFC 2822.\n" +
                "\n" +
                "mnt-by\n" +
                "\n" +
                "   Specifies the identifier of a registered mntner object used for\n" +
                "   authorisation of operations performed with the object that contains\n" +
                "   this attribute.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.  The following words are reserved by\n" +
                "     RPSL, and they can not be used as names:\n" +
                "     \n" +
                "      any as-any rs-any peeras and or not atomic from to at\n" +
                "      action accept announce except refine networks into inbound\n" +
                "      outbound\n" +
                "     \n" +
                "     Names starting with certain prefixes are reserved for\n" +
                "     certain object types.  Names starting with \"as-\" are\n" +
                "     reserved for as set names.  Names starting with \"rs-\" are\n" +
                "     reserved for route set names.  Names starting with \"rtrs-\"\n" +
                "     are reserved for router set names. Names starting with\n" +
                "     \"fltr-\" are reserved for filter set names. Names starting\n" +
                "     with \"prng-\" are reserved for peering set names. Names\n" +
                "     starting with \"irt-\" are reserved for irt names.\n" +
                "\n" +
                "created\n" +
                "\n" +
                "   This attributes reflects when the object was created in\n" +
                "   ISO8601 format (yyyy-MM-dd'T'HH:mm:ssZ).\n" +
                "\n" +
                "     Attribute generated by server.\n" +
                "\n" +
                "last-modified\n" +
                "\n" +
                "   This attributes reflects when the object was last changed in\n" +
                "   ISO8601 format (yyyy-MM-dd'T'HH:mm:ssZ).\n" +
                "\n" +
                "     Attribute generated by server.\n" +
                "\n" +
                "source\n" +
                "\n" +
                "   Specifies the registry where the object is registered. Should be\n" +
                "   \"RIPE\" for the RIPE Database.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a\n" +
                "     registry name must be a letter, and the last character of a\n" +
                "     registry name must be a letter or a digit."));
    }



}
