package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.generated.AggrBndryParser;
import net.ripe.db.whois.common.generated.AggrMtdParser;
import net.ripe.db.whois.common.generated.ComponentsParser;
import net.ripe.db.whois.common.generated.ComponentsR6Parser;
import net.ripe.db.whois.common.generated.DefaultParser;
import net.ripe.db.whois.common.generated.ExportParser;
import net.ripe.db.whois.common.generated.ExportViaParser;
import net.ripe.db.whois.common.generated.FilterParser;
import net.ripe.db.whois.common.generated.IfaddrParser;
import net.ripe.db.whois.common.generated.ImportParser;
import net.ripe.db.whois.common.generated.ImportViaParser;
import net.ripe.db.whois.common.generated.InjectParser;
import net.ripe.db.whois.common.generated.InjectR6Parser;
import net.ripe.db.whois.common.generated.InterfaceParser;
import net.ripe.db.whois.common.generated.MpDefaultParser;
import net.ripe.db.whois.common.generated.MpExportParser;
import net.ripe.db.whois.common.generated.MpFilterParser;
import net.ripe.db.whois.common.generated.MpImportParser;
import net.ripe.db.whois.common.generated.MpPeerParser;
import net.ripe.db.whois.common.generated.MpPeeringParser;
import net.ripe.db.whois.common.generated.PeerParser;
import net.ripe.db.whois.common.generated.PeeringParser;
import net.ripe.db.whois.common.generated.V6FilterParser;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.common.rpsl.attrs.RangeOperation;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

// TODO: [AH] queries should NOT match AUTO- versions of keys, we should remove the AUTO- patterns from here
// TODO: [AH] fix capture groups (add '?:' where capture is not needed)
public interface AttributeSyntax extends Documented {
    AttributeSyntax ANY_SYNTAX = new AnySyntax();

    AttributeSyntax ADDRESS_PREFIX_RANGE_SYNTAX = new AttributeSyntaxParser(new AttributeParser.AddressPrefixRangeParser());

    AttributeSyntax ALIAS_SYNTAX = new AttributeSyntaxRegexp(254,
            Pattern.compile("(?i)^[A-Z0-9]([-A-Z0-9]*[A-Z0-9])?(\\.[A-Z0-9]([-A-Z0-9]*[A-Z0-9])?)*(\\.)?$"), "" +
            "Domain name as specified in RFC 1034 (point 5.2.1.2) with or\n" +
            "without trailing dot (\".\").  The total length should not exceed\n" +
            "254 characters (octets).\n");

    AttributeSyntax AS_BLOCK_SYNTAX = new AttributeSyntaxParser(new AttributeParser.AsBlockParser(), "" +
            "<as-number> - <as-number>\n");

    AttributeSyntax AS_NUMBER_SYNTAX = new AttributeSyntaxParser(new AttributeParser.AutNumParser(), "" +
            "An \"AS\" string followed by an integer in the range\n" +
            "from 0 to 4294967295\n");

    AttributeSyntax AS_SET_SYNTAX = new AttributeSyntaxParser(new AttributeParser.AsSetParser(), "" +
            "An as-set name is made up of letters, digits, the\n" +
            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
            "must start with \"as-\", and the last character of a name must\n" +
            "be a letter or a digit.\n" +
            "\n" +
            "An as-set name can also be hierarchical.  A hierarchical set\n" +
            "name is a sequence of set names and AS numbers separated by\n" +
            "colons \":\".  At least one component of such a name must be\n" +
            "an actual set name (i.e. start with \"as-\").  All the set\n" +
            "name components of a hierarchical as-name have to be as-set\n" +
            "names.\n");

    AttributeSyntax AGGR_BNDRY_SYNTAX = new AttributeSyntaxParser(new AggrBndryParser(), "" +
            "[<as-expression>]\n");

    AttributeSyntax AGGR_MTD_SYNTAX = new AttributeSyntaxParser(new AggrMtdParser(), "" +
            "inbound | outbound [<as-expression>]\n");

    AttributeSyntax AUTH_SCHEME_SYNTAX = new AttributeSyntaxRegexp(
            Pattern.compile("(?i)^(MD5-PW \\$1\\$[A-Z0-9./]{1,8}\\$[A-Z0-9./]{22}|PGPKEY-[A-F0-9]{8}|SSO [-@.\\w]{1,90}|X509-[1-9][0-9]{0,19}|AUTO-[1-9][0-9]*)$"), "" +
            "<auth-scheme> <scheme-info>       Description\n" +
            "\n" +
            "MD5-PW        encrypted           We strongly advise phrases longer\n" +
            "              password, produced  than 8 characters to be used,\n" +
            "              using the FreeBSD   avoiding the use of words or\n" +
            "              crypt_md5           combinations of words found in any\n" +
            "              algorithm           dictionary of any language.\n" +
            "\n" +
            "PGPKEY-<id>                       Strong scheme of authentication.\n" +
            "                                  <id> is the PGP key ID to be\n" +
            "                                  used for authentication. This string\n" +
            "                                  is the same one that is used in the\n" +
            "                                  corresponding key-cert object's\n" +
            "                                  \"key-cert:\" attribute.\n" +
            "\n" +
            "X509-<nnn>                        Strong scheme of authentication.\n" +
            "                                  <nnn> is the index number of the\n" +
            "                                  corresponding key-cert object's\n" +
            "                                  \"key-cert:\" attribute (X509-nnn).\n" +
            "\n" +
            "SSO           username            The username is the same as one used \n" +
            "                                  for a RIPE NCC Access account. This must \n" +
            "                                  be a valid username and is checked \n" +
            "                                  against the RIPE NCC Access user list.\n");

    AttributeSyntax CERTIF_SYNTAX = new AnySyntax("" +
            "The value of the public key should be supplied either using\n" +
            "multiple \"certif:\" attributes, or in one \"certif:\"\n" +
            "attribute. In the first case, this is easily done by\n" +
            "exporting the key from your local key ring in ASCII armored\n" +
            "format and prepending each line of the key with the string\n" +
            "\"certif:\". In the second case, line continuation should be\n" +
            "used to represent an ASCII armored format of the key. All\n" +
            "the lines of the exported key must be included; also the\n" +
            "begin and end markers and the empty line which separates the\n" +
            "header from the key body.\n");

    AttributeSyntax CHANGED_SYNTAX = new AttributeSyntaxParser(new AttributeParser.ChangedParser(), "" +
            "An e-mail address as defined in RFC 2822, followed by a date\n" +
            "in the format YYYYMMDD.\n");

    AttributeSyntax COUNTRY_CODE_SYNTAX = new AttributeSyntaxRegexp(Pattern.compile("(?i)^[a-z]{2}$"),
            "Valid two-letter ISO 3166 country code.");

    AttributeSyntax COMPONENTS_SYNTAX = new ComponentsSyntax();

    AttributeSyntax DEFAULT_SYNTAX = new AttributeSyntaxParser(new DefaultParser(), "" +
            "to <peering> [action <action>] [networks <filter>]");

    AttributeSyntax DOMAIN_SYNTAX = new AttributeSyntaxParser(new AttributeParser.DomainParser(), "" +
            "Domain name as specified in RFC 1034 (point 5.2.1.2) with or\n" +
            "without trailing dot (\".\").  The total length should not exceed\n" +
            "254 characters (octets).\n");

    AttributeSyntax DS_RDATA_SYNTAX = new AttributeSyntaxParser(new AttributeParser.DsRdataParser(), "" +
            "<Keytag> <Algorithm> <Digest type> <Digest>\n" +
            "\n" +
            "Keytag is represented by an unsigned decimal integer (0-65535).\n" +
            "\n" +
            "Algorithm is represented by an unsigned decimal integer (0-255).\n" +
            "\n" +
            "Digest type is represented by a unsigned decimal integer (0-255).\n" +
            "\n" +
            "Digest is a digest in hexadecimal representation (case insensitive). Its length varies for various digest types.\n" +
            "For digest type SHA-1 digest is represented by 20 octets (40 characters, plus possible spaces).\n" +
            "\n" +
            "For more details, see RFC4034.\n");

    AttributeSyntax EMAIL_SYNTAX = new AttributeSyntaxRegexp(80, Pattern.compile("(?i)^.+@([^.]+[.])+[^.]+$"),
            "An e-mail address as defined in RFC 2822.\n");

    AttributeSyntax EXPORT_COMPS_SYNTAX = new ExportCompsSyntax();

    AttributeSyntax EXPORT_SYNTAX = new AttributeSyntaxParser(new ExportParser(), "" +
            "[protocol <protocol-1>] [into <protocol-1>]\n" +
            "to <peering-1> [action <action-1>]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "to <peering-N> [action <action-N>]\n" +
            "announce <filter>\n");

    AttributeSyntax FILTER_SYNTAX = new AttributeSyntaxParser(new FilterParser(), "" +
            "Logical expression which when applied to a set of routes\n" +
            "returns a subset of these routes. Please refer to RFC 2622\n" +
            "for more information.\n");

    AttributeSyntax FILTER_SET_SYNTAX = new AttributeSyntaxParser(new AttributeParser.FilterSetParser(), "" +
            "A filter-set name is made up of letters, digits, the\n" +
            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
            "must start with \"fltr-\", and the last character of a name\n" +
            "must be a letter or a digit.\n" +
            "\n" +
            "A filter-set name can also be hierarchical.  A hierarchical\n" +
            "set name is a sequence of set names and AS numbers separated\n" +
            "by colons \":\".  At least one component of such a name must\n" +
            "be an actual set name (i.e. start with \"fltr-\").  All the\n" +
            "set name components of a hierarchical filter-name have to be\n" +
            "filter-set names.\n");

    AttributeSyntax FREE_FORM_SYNTAX = new AttributeSyntaxRegexp(Pattern.compile("(?s)^.*$"), "" +
            "A sequence of ASCII characters.\n");

    AttributeSyntax GENERATED_SYNTAX = new AnySyntax("" +
            "Attribute generated by server.");

    AttributeSyntax GEOLOC_SYNTAX = new GeolocSyntax();
    AttributeSyntax HOLES_SYNTAX = new RoutePrefixSyntax();
    AttributeSyntax IMPORT_SYNTAX = new AttributeSyntaxParser(new ImportParser(), "" +
            "[protocol <protocol-1>] [into <protocol-1>]\n" +
            "from <peering-1> [action <action-1>]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "from <peering-N> [action <action-N>]\n" +
            "accept <filter>\n");

    AttributeSyntax INET_RTR_SYNTAX = new AttributeSyntaxRegexp(254,
            Pattern.compile("(?i)^[A-Z0-9]([-_A-Z0-9]*[A-Z0-9])?(\\.[A-Z0-9]([-_A-Z0-9]*[A-Z0-9])?)*(\\.)?$"), "" +
            "Domain name as specified in RFC 1034 (point 5.2.1.2) with or\n" +
            "without trailing dot (\".\").  The total length should not exceed\n" +
            "254 characters (octets).\n");

    AttributeSyntax IFADDR_SYNTAX = new AttributeSyntaxParser(new IfaddrParser(), "" +
            "<ipv4-address> masklen <integer> [action <action>]");

    AttributeSyntax INJECT_SYNTAX = new InjectSyntax();

    AttributeSyntax INTERFACE_SYNTAX = new AttributeSyntaxParser(new InterfaceParser(), "" +
            "afi <afi> <ipv4-address> masklen <integer> [action <action>]\n" +
            "afi <afi> <ipv6-address> masklen <integer> [action <action>]\n" +
            "          [tunnel <remote-endpoint-address>,<encapsulation>]\n");

    AttributeSyntax IPV4_SYNTAX = new AttributeSyntaxParser(new AttributeParser.Ipv4ResourceParser(), "" +
            "<ipv4-address> - <ipv4-address>");

    AttributeSyntax IPV6_SYNTAX = new AttributeSyntaxParser(new AttributeParser.Ipv6ResourceParser(), "" +
            "<ipv6-address>/<prefix>");

    AttributeSyntax IRT_SYNTAX = new AttributeSyntaxRegexp(Pattern.compile("(?i)^irt-[A-Z0-9_-]*[A-Z0-9]$"), "" +
            "An irt name is made up of letters, digits, the character\n" +
            "underscore \"_\", and the character hyphen \"-\"; it must start\n" +
            "with \"irt-\", and the last character of a name must be a\n" +
            "letter or a digit.\n");

    AttributeSyntax KEY_CERT_SYNTAX = new AttributeSyntaxRegexp(
            Pattern.compile("(?i)^(PGPKEY-[A-F0-9]{8})|(X509-[1-9][0-9]*)|(AUTO-[1-9][0-9]*)$"), "" +
            "PGPKEY-<id>\n" +
            "\n" +
            "<id> is  the PGP key ID of the public key in 8-digit\n" +
            "hexadecimal format without \"0x\" prefix.");

    AttributeSyntax LANGUAGE_CODE_SYNTAX = new AttributeSyntaxRegexp(Pattern.compile("(?i)^[a-z]{2}$"), "" +
            "Valid two-letter ISO 639-1 language code.\n");

    AttributeSyntax MBRS_BY_REF_SYNTAX = new AnySyntax("" +
            "<mntner-name> | ANY\n");

    AttributeSyntax MEMBER_OF_SYNTAX = new MemberOfSyntax();

    AttributeSyntax MEMBERS_SYNTAX = new MembersSyntax(false);

    AttributeSyntax METHOD_SYNTAX = new AnySyntax("" +
            "Currently, only PGP keys are supported.\n");

    AttributeSyntax MNT_ROUTES_SYNTAX = new AttributeSyntaxParser(new AttributeParser.MntRoutesParser(), new Multiple(new HashMap<ObjectType, String>() {{
        put(ObjectType.AUT_NUM, "<mnt-name> [ { list of (<ipv4-address>/<prefix> or <ipv6-address>/<prefix>) } | ANY ]\n");
        put(ObjectType.INET6NUM, "<mnt-name> [ { list of <ipv6-address>/<prefix> } | ANY ]\n");
        put(ObjectType.INETNUM, "<mnt-name> [ { list of <address-prefix-range> } | ANY ]\n");
        put(ObjectType.ROUTE, "<mnt-name> [ { list of <address-prefix-range> } | ANY ]\n");
        put(ObjectType.ROUTE6, "<mnt-name> [ { list of <ipv6-address>/<prefix> } | ANY ]\n");
    }}));

    AttributeSyntax MP_DEFAULT_SYNTAX = new AttributeSyntaxParser(new MpDefaultParser(), "" +
            "to <peering> [action <action>] [networks <filter>]\n");

    AttributeSyntax MP_EXPORT_SYNTAX = new AttributeSyntaxParser(new MpExportParser(), "" +
            "[protocol <protocol-1>] [into <protocol-1>]\n" +
            "afi <afi-list>\n" +
            "to <peering-1> [action <action-1>]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "to <peering-N> [action <action-N>]\n" +
            "announce <filter>\n");

    AttributeSyntax EXPORT_VIA_SYNTAX = new AttributeSyntaxParser(new ExportViaParser(), "" +
            "[protocol <protocol-1>] [into <protocol-2>]   \n" +
            "afi <afi-list>\n" +
            "<peering-1>\n" +
            "to <peering-2> [action <action-1>; <action-2>; ... <action-N>;]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "<peering-3>\n" +
            "to <peering-M> [action <action-1>; <action-2>; ... <action-N>;]\n" +
            "announce <filter>\n");

    AttributeSyntax MP_FILTER_SYNTAX = new AttributeSyntaxParser(new MpFilterParser(), "" +
            "Logical expression which when applied to a set of multiprotocol\n" +
            "routes returns a subset of these routes. Please refer to RPSLng\n" +
            "Internet Draft for more information.\n");

    AttributeSyntax MP_IMPORT_SYNTAX = new AttributeSyntaxParser(new MpImportParser(), "" +
            "[protocol <protocol-1>] [into <protocol-1>]\n" +
            "afi <afi-list>\n" +
            "from <peering-1> [action <action-1>]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "from <peering-N> [action <action-N>]\n" +
            "accept (<filter>|<filter> except <importexpression>|\n" +
            "        <filter> refine <importexpression>)\n");

    AttributeSyntax IMPORT_VIA_SYNTAX = new AttributeSyntaxParser(new ImportViaParser(), "" +
            "[protocol <protocol-1>] [into <protocol-2>]\n" +
            "afi <afi-list>\n" +
            "<peering-1>\n" +
            "from <peering-2> [action <action-1>; <action-2>; ... <action-N>;]\n" +
            "    .\n" +
            "    .\n" +
            "    .\n" +
            "<peering-3>\n" +
            "from <peering-M> [action <action-1>; <action-2>; ... <action-N>;]\n" +
            "accept (<filter>|<filter> except <importexpression>|\n" +
            "        <filter> refine <importexpression>)\n");

    AttributeSyntax MP_MEMBERS_SYNTAX = new MembersSyntax(true);

    AttributeSyntax MP_PEER_SYNTAX = new AttributeSyntaxParser(new MpPeerParser(), new Multiple(new HashMap<ObjectType, String>() {{
        put(ObjectType.INET_RTR, "" +
                "<protocol> afi <afi> <ipv4- or ipv6- address> <options>\n" +
                "| <protocol> <inet-rtr-name> <options>\n" +
                "| <protocol> <rtr-set-name> <options>\n" +
                "| <protocol> <peering-set-name> <options>\n");

        put(ObjectType.PEERING_SET, "" +
                "afi <afi> <peering>\n");

    }}));

    AttributeSyntax MP_PEERING_SYNTAX = new AttributeSyntaxParser(new MpPeeringParser(), "" +
            "afi <afi> <peering>\n");

    AttributeSyntax NETNAME_SYNTAX = new AttributeSyntaxRegexp(80, Pattern.compile("(?i)^[A-Z]([A-Z0-9_-]*[A-Z0-9])?$"), "" +
            "Made up of letters, digits, the character underscore \"_\",\n" +
            "and the character hyphen \"-\"; the first character of a name\n" +
            "must be a letter, and the last character of a name must be a\n" +
            "letter or a digit.\n");

    AttributeSyntax NIC_HANDLE_SYNTAX = new AttributeSyntaxRegexp(30, Pattern.compile("(?i)^([A-Z]{2,4}([1-9][0-9]{0,5})?(-[A-Z]{2,10})?|AUTO-[1-9][0-9]*([A-Z]{2,4})?)$"), "" +
            "From 2 to 4 characters optionally followed by up to 6 digits\n" +
            "optionally followed by a source specification.  The first digit\n" +
            "must not be \"0\".  Source specification starts with \"-\" followed\n" +
            "by source name up to 9-character length.\n");

    AttributeSyntax NSERVER_SYNTAX = new AttributeSyntaxParser(new AttributeParser.NServerParser(), "" +
            "Nameserver name as specified in RFC 1034 with or without\n" +
            "trailing dot (\".\").  The total length should not exceed\n" +
            "254 characters (octets).\n" +
            "\n" +
            "The nameserver name may be optionally followed by IPv4 address\n" +
            "in decimal dotted quad form (e.g. 192.0.2.1) or IPv6 address\n" +
            "in lowercase canonical form (Section 2.2.1, RFC 4291).\n" +
            "\n" +
            "The nameserver name may be followed by an IP address only when\n" +
            "the name is inside of the domain being delegated.\n");

    AttributeSyntax NUMBER_SYNTAX = new AttributeSyntaxRegexp(Pattern.compile("^[0-9]+$"), "" +
            "Specifies a numeric value.\n");

    AttributeSyntax OBJECT_NAME_SYNTAX = new AttributeSyntaxParser(new AttributeParser.NameParser(), "" +
            "Made up of letters, digits, the character underscore \"_\",\n" +
            "and the character hyphen \"-\"; the first character of a name\n" +
            "must be a letter, and the last character of a name must be a\n" +
            "letter or a digit.  The following words are reserved by\n" +
            "RPSL, and they can not be used as names:\n" +
            "\n" +
            " any as-any rs-any peeras and or not atomic from to at\n" +
            " action accept announce except refine networks into inbound\n" +
            " outbound\n" +
            "\n" +
            "Names starting with certain prefixes are reserved for\n" +             // TODO: [ES] implement per type
            "certain object types.  Names starting with \"as-\" are\n" +
            "reserved for as set names.  Names starting with \"rs-\" are\n" +
            "reserved for route set names.  Names starting with \"rtrs-\"\n" +
            "are reserved for router set names. Names starting with\n" +
            "\"fltr-\" are reserved for filter set names. Names starting\n" +
            "with \"prng-\" are reserved for peering set names. Names\n" +
            "starting with \"irt-\" are reserved for irt names.\n");

    AttributeSyntax SOURCE_SYNTAX = new AttributeSyntaxRegexp(80,
            Pattern.compile("(?i)^[A-Z][A-Z0-9_-]*[A-Z0-9]$"), "" +
            "Made up of letters, digits, the character underscore \"_\",\n" +
            "and the character hyphen \"-\"; the first character of a\n" +
            "registry name must be a letter, and the last character of a\n" +
            "registry name must be a letter or a digit.");

    AttributeSyntax ORGANISATION_SYNTAX = new AttributeSyntaxRegexp(30,
            Pattern.compile("(?i)^(ORG-[A-Z]{2,4}([1-9][0-9]{0,5})?-[A-Z][A-Z0-9_-]*[A-Z0-9]|AUTO-[1-9][0-9]*([A-Z]{2,4})?)$"), "" +
            "The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits\n" +
            "followed by a source specification.  The first digit must not be \"0\".\n" +
            "Source specification starts with \"-\" followed by source name up to\n" +
            "9-character length.\n");

    AttributeSyntax ORG_NAME_SYNTAX = new AttributeSyntaxRegexp(
            Pattern.compile("(?i)^[\\]\\[A-Z0-9._\"*()@,&:!'`+\\/-]{1,64}( [\\]\\[A-Z0-9._\"*()@,&:!'`+\\/-]{1,64}){0,29}$"), "" +
            "A list of words separated by white space.  A word is made up of letters,\n" +
            "digits, the character underscore \"_\", and the character hyphen \"-\";\n" +
            "the first character of a word must be a letter or digit; the last\n" +
            "character of a word must be a letter, digit or a dot.\n");

    AttributeSyntax ORG_TYPE_SYNTAX = new OrgTypeSyntax();

    AttributeSyntax PEER_SYNTAX = new AttributeSyntaxParser(new PeerParser(), "" +
            "<protocol> <ipv4-address> <options>\n" +
            "| <protocol> <inet-rtr-name> <options>\n" +
            "| <protocol> <rtr-set-name> <options>\n" +
            "| <protocol> <peering-set-name> <options>\n");

    AttributeSyntax PEERING_SYNTAX = new AttributeSyntaxParser(new PeeringParser(), "" +
            "<peering>\n");

    AttributeSyntax PERSON_ROLE_NAME_SYNTAX = new PersonRoleSyntax();

    AttributeSyntax POEM_SYNTAX = new AttributeSyntaxRegexp(80,
            Pattern.compile("(?i)^POEM-[A-Z0-9][A-Z0-9_-]*$"), "" +
            "POEM-<string>\n" +
            "\n" +
            "<string> can include alphanumeric characters, and \"_\" and\n" +
            "\"-\" characters.\n");

    AttributeSyntax POETIC_FORM_SYNTAX = new AttributeSyntaxRegexp(80,
            Pattern.compile("(?i)^FORM-[A-Z0-9][A-Z0-9_-]*$"), "" +
            "FORM-<string>\n" +
            "\n" +
            "<string> can include alphanumeric characters, and \"_\" and\n" +
            "\"-\" characters.\n");

    AttributeSyntax PINGABLE_SYNTAX = new AttributeSyntaxParser(new AttributeParser.IPAddressParser(), "" +
            "<ipv4-address> as defined in RFC2622\n" +
            "| <ipv6-address> as defined in RFC4012\n");

    AttributeSyntax PHONE_SYNTAX = new AttributeSyntaxRegexp(30,
            Pattern.compile("" +
                    "(?i)^" +
                    "[+][0-9. -]+" +                   // "normal" phone numbers
                    "(?:[(][0-9. -]+[)][0-9. -]+)?" +  // a possible '(123)' at the end
                    "(?:ext[.][0-9. -]+)?" +           // a possible 'ext. 123' at the end
                    "$"), "" +
            "Contact telephone number. Can take one of the forms:\n" +
            "\n" +
            "'+' <integer-list>\n" +
            "'+' <integer-list> \"(\" <integer-list> \")\" <integer-list>\n" +
            "'+' <integer-list> ext. <integer list>\n" +
            "'+' <integer-list> \"(\" integer list \")\" <integer-list> ext. <integer-list>\n");

    AttributeSyntax ROUTE_SET_SYNTAX = new AttributeSyntaxParser(new AttributeParser.RouteSetParser(), "" +
            "An route-set name is made up of letters, digits, the\n" +
            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
            "must start with \"rs-\", and the last character of a name must\n" +
            "be a letter or a digit.\n" +
            "\n" +
            "A route-set name can also be hierarchical.  A hierarchical\n" +
            "set name is a sequence of set names and AS numbers separated\n" +
            "by colons \":\".  At least one component of such a name must\n" +
            "be an actual set name (i.e. start with \"rs-\").  All the set\n" +
            "name components of a hierarchical route-name have to be\n" +
            "route-set names.\n");

    AttributeSyntax RTR_SET_SYNTAX = new AttributeSyntaxParser(new AttributeParser.RtrSetParser(), "" +
            "A router-set name is made up of letters, digits, the\n" +
            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
            "must start with \"rtrs-\", and the last character of a name\n" +
            "must be a letter or a digit.\n" +
            "\n" +
            "A router-set name can also be hierarchical.  A hierarchical\n" +
            "set name is a sequence of set names and AS numbers separated\n" +
            "by colons \":\".  At least one component of such a name must\n" +
            "be an actual set name (i.e. start with \"rtrs-\").  All the\n" +
            "set name components of a hierarchical router-set name have\n" +
            "to be router-set names.\n");

    AttributeSyntax PEERING_SET_SYNTAX = new AttributeSyntaxParser(new AttributeParser.PeeringSetParser(), "" +
            "A peering-set name is made up of letters, digits, the\n" +
            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
            "must start with \"prng-\", and the last character of a name\n" +
            "must be a letter or a digit.\n" +
            "\n" +
            "A peering-set name can also be hierarchical.  A hierarchical\n" +
            "set name is a sequence of set names and AS numbers separated\n" +
            "by colons \":\".  At least one component of such a name must\n" +
            "be an actual set name (i.e. start with \"prng-\").  All the\n" +
            "set name components of a hierarchical peering-set name have\n" +
            "to be peering-set names.\n");

    AttributeSyntax ROUTE_SYNTAX = new AttributeSyntaxParser(new AttributeParser.RouteResourceParser(), "" +
            "An address prefix is represented as an IPv4 address followed\n" +
            "by the character slash \"/\" followed by an integer in the\n" +
            "range from 0 to 32.  The following are valid address\n" +
            "prefixes: 128.9.128.5/32, 128.9.0.0/16, 0.0.0.0/0; and the\n" +
            "following address prefixes are invalid: 0/0, 128.9/16 since\n" +
            "0 or 128.9 are not strings containing four integers.\n");

    AttributeSyntax ROUTE6_SYNTAX = new AttributeSyntaxParser(new AttributeParser.Route6ResourceParser(), "" +
            "<ipv6-address>/<prefix>\n");

    AttributeSyntax STATUS_SYNTAX = new StatusSyntax();

    class AttributeSyntaxRegexp implements AttributeSyntax {
        private final Integer maxLength;
        private final Pattern matchPattern;
        private final String description;

        AttributeSyntaxRegexp(final Pattern matchPattern, final String description) {
            this(null, matchPattern, description);
        }

        AttributeSyntaxRegexp(final Integer maxLength, final Pattern matchPattern, final String description) {
            this.maxLength = maxLength;
            this.matchPattern = matchPattern;
            this.description = description;
        }

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            final boolean lengthOk = maxLength == null || value.length() <= maxLength;
            final boolean matches = matchPattern.matcher(value).matches();

            return lengthOk && matches;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return description;
        }
    }

    class AnySyntax implements AttributeSyntax {
        private final String description;

        public AnySyntax() {
            this("");
        }

        public AnySyntax(final String description) {
            this.description = description;
        }

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            return true;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return description;
        }
    }

    class RoutePrefixSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case ROUTE:
                    return IPV4_SYNTAX.matches(objectType, value);
                case ROUTE6:
                    return IPV6_SYNTAX.matches(objectType, value);
                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            switch (objectType) {
                case ROUTE:
                    return "" +
                            "An address prefix is represented as an IPv4 address followed\n" +
                            "by the character slash \"/\" followed by an integer in the\n" +
                            "range from 0 to 32.  The following are valid address\n" +
                            "prefixes: 128.9.128.5/32, 128.9.0.0/16, 0.0.0.0/0; and the\n" +
                            "following address prefixes are invalid: 0/0, 128.9/16 since\n" +
                            "0 or 128.9 are not strings containing four integers.";
                case ROUTE6:
                    return "" +
                            "<ipv6-address>/<prefix>";
                default:
                    return "";
            }
        }
    }

    class GeolocSyntax implements AttributeSyntax {
        private static final Pattern GEOLOC_PATTERN = Pattern.compile("^[+-]?(\\d*\\.?\\d+)\\s+[+-]?(\\d*\\.?\\d+)$");

        private static final double LATITUDE_RANGE = 90.0;
        private static final double LONGITUDE_RANGE = 180.0;

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            final Matcher matcher = GEOLOC_PATTERN.matcher(value);
            if (!matcher.matches()) {
                return false;
            }

            if (Double.compare(LATITUDE_RANGE, Double.parseDouble(matcher.group(1))) < 0) {
                return false;
            }

            if (Double.compare(LONGITUDE_RANGE, Double.parseDouble(matcher.group(2))) < 0) {
                return false;
            }

            return true;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return "" +
                    "Location coordinates of the resource. Can take one of the following forms:\n" +
                    "\n" +
                    "[-90,90][-180,180]\n";
        }
    }

    class MemberOfSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case AUT_NUM:
                    return AS_SET_SYNTAX.matches(objectType, value);
                case ROUTE:
                case ROUTE6:
                    return ROUTE_SET_SYNTAX.matches(objectType, value);
                case INET_RTR:
                    return RTR_SET_SYNTAX.matches(objectType, value);
                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            switch (objectType) {
                case AUT_NUM:
                    return "" +
                            "An as-set name is made up of letters, digits, the\n" +
                            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
                            "must start with \"as-\", and the last character of a name must\n" +
                            "be a letter or a digit.\n" +
                            "\n" +
                            "An as-set name can also be hierarchical.  A hierarchical set\n" +
                            "name is a sequence of set names and AS numbers separated by\n" +
                            "colons \":\".  At least one component of such a name must be\n" +
                            "an actual set name (i.e. start with \"as-\").  All the set\n" +
                            "name components of a hierarchical as-name have to be as-set\n" +
                            "names.\n";

                case ROUTE:
                    return "" +
                            "An route-set name is made up of letters, digits, the\n" +
                            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
                            "must start with \"rs-\", and the last character of a name must\n" +
                            "be a letter or a digit.\n" +
                            "\n" +
                            "A route-set name can also be hierarchical.  A hierarchical\n" +
                            "set name is a sequence of set names and AS numbers separated\n" +
                            "by colons \":\".  At least one component of such a name must\n" +
                            "be an actual set name (i.e. start with \"rs-\").  All the set\n" +
                            "name components of a hierarchical route-name have to be\n" +
                            "route-set names.\n";

                case ROUTE6:
                    return "" +
                            "An route-set name is made up of letters, digits, the\n" +
                            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
                            "must start with \"rs-\", and the last character of a name must\n" +
                            "be a letter or a digit.\n" +
                            "\n" +
                            "A route-set name can also be hierarchical.  A hierarchical\n" +
                            "set name is a sequence of set names and AS numbers separated\n" +
                            "by colons \":\".  At least one component of such a name must\n" +
                            "be an actual set name (i.e. start with \"rs-\").  All the set\n" +
                            "name components of a hierarchical route-name have to be\n" +
                            "route-set names.\n";

                case INET_RTR:
                    return "" +
                            "A router-set name is made up of letters, digits, the\n" +
                            "character underscore \"_\", and the character hyphen \"-\"; it\n" +
                            "must start with \"rtrs-\", and the last character of a name\n" +
                            "must be a letter or a digit.\n" +
                            "\n" +
                            "A router-set name can also be hierarchical.  A hierarchical\n" +
                            "set name is a sequence of set names and AS numbers separated\n" +
                            "by colons \":\".  At least one component of such a name must\n" +
                            "be an actual set name (i.e. start with \"rtrs-\").  All the\n" +
                            "set name components of a hierarchical router-set name have\n" +
                            "to be router-set names.\n";
                default:
                    return "";
            }
        }
    }

    class MembersSyntax implements AttributeSyntax {
        private final boolean allowIpv6;

        MembersSyntax(final boolean allowIpv6) {
            this.allowIpv6 = allowIpv6;
        }

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case AS_SET:
                    final boolean asNumberSyntax = AS_NUMBER_SYNTAX.matches(objectType, value);
                    final boolean asSetSyntax = AS_SET_SYNTAX.matches(objectType, value);

                    return asNumberSyntax || asSetSyntax;

                case ROUTE_SET:
                    if (ROUTE_SET_SYNTAX.matches(objectType, value)) {
                        return true;
                    }

                    if (AS_NUMBER_SYNTAX.matches(objectType, value) || AS_SET_SYNTAX.matches(objectType, value)) {
                        return true;
                    }

                    if (ADDRESS_PREFIX_RANGE_SYNTAX.matches(objectType, value)) {
                        final AddressPrefixRange apr = AddressPrefixRange.parse(value);
                        if ((apr.getIpInterval() instanceof Ipv4Resource) || (allowIpv6 && apr.getIpInterval() instanceof Ipv6Resource)) {
                            return true;
                        }
                    }

                    return validateRouteSetWithRange(objectType, value);

                case RTR_SET:
                    return allowIpv6 && IPV6_SYNTAX.matches(objectType, value) ||
                            INET_RTR_SYNTAX.matches(objectType, value) ||
                            RTR_SET_SYNTAX.matches(objectType, value) ||
                            IPV4_SYNTAX.matches(objectType, value);

                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            switch (objectType) {
                case AS_SET:
                    return "" +
                            "list of\n" +
                            "<as-number> or\n" +
                            "<as-set-name>\n";
                case ROUTE_SET:
                    if (allowIpv6) {
                        return "" +
                                "list of\n" +
                                "<address-prefix-range> or\n" +
                                "<route-set-name> or\n" +
                                "<route-set-name><range-operator>.\n";
                    } else {
                        return "" +
                                "list of\n" +
                                "<ipv4-address-prefix-range> or\n" +
                                "<route-set-name> or\n" +
                                "<route-set-name><range-operator>.\n";
                    }

                case RTR_SET:
                    return allowIpv6 ? "" +
                            "list of\n" +
                            "<inet-rtr-name> or\n" +
                            "<rtr-set-name> or\n" +
                            "<ipv4-address> or\n" +
                            "<ipv6-address>\n"
                            : "" +
                            "list of\n" +
                            "<inet-rtr-name> or\n" +
                            "<rtr-set-name> or\n" +
                            "<ipv4-address>\n";

                default:
                    return "";
            }
        }

        private boolean validateRouteSetWithRange(ObjectType objectType, String value) {
            final int rangeOperationIdx = value.lastIndexOf('^');
            if (rangeOperationIdx == -1) {
                return false;
            }

            final String routeSet = value.substring(0, rangeOperationIdx);
            final boolean routeSetSyntaxResult = ROUTE_SET_SYNTAX.matches(objectType, routeSet);
            if (!routeSetSyntaxResult) {
                return routeSetSyntaxResult;
            }

            final String rangeOperation = value.substring(rangeOperationIdx);
            try {
                RangeOperation.parse(rangeOperation, 0, 128);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }


    class OrgTypeSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            return OrgType.getFor(value) != null;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            final StringBuilder builder = new StringBuilder();
            builder.append("org-type can have one of these values:\n\n");

            for (final OrgType orgType : OrgType.values()) {
                builder.append("o '")
                        .append(orgType)
                        .append("' ")
                        .append(orgType.getInfo())
                        .append("\n");
            }

            builder.append("\n");
            return builder.toString();
        }
    }

    class PersonRoleSyntax implements AttributeSyntax {
        private static final Pattern PATTERN = Pattern.compile("(?i)^[A-Z][A-Z0-9\\\\.`'_-]{0,63}(?: [A-Z0-9\\\\.`'_-]{1,64}){0,9}$");
        private static final Splitter SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            if (!PATTERN.matcher(value).matches()) {
                return false;
            }

            int nrNamesStartingWithLetter = 0;
            for (final String name : SPLITTER.split(value)) {
                if (Character.isLetter(name.charAt(0))) {
                    nrNamesStartingWithLetter++;

                    if (nrNamesStartingWithLetter == 2) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return "" +
                    "Must have at least 2 words beginning with a letter.\n" +
                    "Each word consists of letters, digits and the following symbols:\n" +
                    "    .`'_-\n";

        }
    }

    class StatusSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case INETNUM:
                    try {
                        InetnumStatus.getStatusFor(ciString(value));
                        return true;
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                case INET6NUM:
                    try {
                        Inet6numStatus.getStatusFor(ciString(value));
                        return true;
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            final StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append("Status can have one of these values:\n\n");

            switch (objectType) {
                case INETNUM:
                    for (final InetnumStatus status : InetnumStatus.values()) {
                        descriptionBuilder.append("o ").append(status).append('\n');
                    }

                    return descriptionBuilder.toString();
                case INET6NUM:
                    for (final Inet6numStatus status : Inet6numStatus.values()) {
                        descriptionBuilder.append("o ").append(status).append('\n');
                    }

                    return descriptionBuilder.toString();
                default:
                    return "";
            }
        }
    }

    class ComponentsSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case ROUTE:
                    return new AttributeSyntaxParser(new ComponentsParser()).matches(objectType, value);
                case ROUTE6:
                    return new AttributeSyntaxParser(new ComponentsR6Parser()).matches(objectType, value);
                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return "" +
                    "[ATOMIC] [[<filter>] [protocol <protocol> <filter> ...]]\n" +
                    "\n" +
                    "<protocol> is a routing routing protocol name such as\n" +
                    "BGP4, OSPF or RIP\n" +
                    "\n" +
                    "<filter> is a policy expression\n";
        }
    }


    class ExportCompsSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case ROUTE:
                    return new AttributeSyntaxParser(new FilterParser()).matches(objectType, value);
                case ROUTE6:
                    return new AttributeSyntaxParser(new V6FilterParser()).matches(objectType, value);
                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            switch (objectType) {
                case ROUTE:
                    return "" +
                            "Logical expression which when applied to a set of routes\n" +
                            "returns a subset of these routes. Please refer to RFC 2622\n" +
                            "for more information.";
                case ROUTE6:
                    return "" +
                            "Logical expression which when applied to a set of routes\n" +
                            "returns a subset of these routes. Please refer to RFC 2622\n" +
                            "and RPSLng I-D for more information.";
                default:
                    return "";
            }
        }
    }

    class InjectSyntax implements AttributeSyntax {
        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            switch (objectType) {
                case ROUTE:
                    return new AttributeSyntaxParser(new InjectParser()).matches(objectType, value);

                case ROUTE6:
                    return new AttributeSyntaxParser(new InjectR6Parser()).matches(objectType, value);

                default:
                    return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return "" +
                    "[at <router-expression>]\n" +
                    "[action <action>]\n" +
                    "[upon <condition>]\n";
        }
    }

    class AttributeSyntaxParser implements AttributeSyntax {
        private final AttributeParser attributeParser;
        private final Documented description;

        public AttributeSyntaxParser(final AttributeParser attributeParser) {
            this(attributeParser, "");
        }

        public AttributeSyntaxParser(final AttributeParser attributeParser, final String description) {
            this(attributeParser, new Single(description));
        }

        public AttributeSyntaxParser(final AttributeParser attributeParser, final Documented description) {
            this.attributeParser = attributeParser;
            this.description = description;
        }

        @Override
        public boolean matches(final ObjectType objectType, final String value) {
            try {
                attributeParser.parse(value);
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }

        @Override
        public String getDescription(final ObjectType objectType) {
            return description.getDescription(objectType);
        }
    }

    boolean matches(ObjectType objectType, String value);
}
