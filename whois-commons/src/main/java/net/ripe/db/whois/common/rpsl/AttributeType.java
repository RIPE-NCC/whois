package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeSyntax.*;
import static net.ripe.db.whois.common.rpsl.AttributeValueType.LIST_VALUE;

public enum AttributeType implements Documented {
    ABUSE_MAILBOX(new Builder("abuse-mailbox", "am")
            .doc("Specifies the e-mail address to which abuse complaints should be sent. " +
                    "This attribute should only be used in the ROLE object. It will be deprecated from any other object. " +
                    "Adding this attribute to a ROLE object, then referencing it in an \"abuse-c:\" attribute of an ORGANISATION object, " +
                    "will remove any query limits for the ROLE object. These ROLE objects are considered to include only commercial data.")
            .syntax(EMAIL_SYNTAX)),

    ABUSE_C(new Builder("abuse-c", "au")
            .doc("References an abuse contact. " +
                    "This can only be a ROLE object containing an \"abuse-mailbox:\" attribute. " +
                    "Making this reference will remove any query limits for the ROLE object. " +
                    "These ROLE objects are considered to include only commercial data.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.ROLE)),

    ADDRESS(new Builder("address", "ad")
            .doc("Full postal address of a contact")
            .syntax(FREE_FORM_SYNTAX)),

    ADMIN_C(new Builder("admin-c", "ac")
            .doc("References an on-site administrative contact.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.PERSON, ObjectType.ROLE)),

    AGGR_BNDRY(new Builder("aggr-bndry", "ab")
            .doc("Defines a set of ASes, which form the aggregation boundary.")
            .syntax(AGGR_BNDRY_SYNTAX)),

    AGGR_MTD(new Builder("aggr-mtd", "ag")
            .doc("Specifies how the aggregate is generated.")
            .syntax(AGGR_MTD_SYNTAX)),

    ALIAS(new Builder("alias", "az")
            .doc("The canonical DNS name for the router.")
            .syntax(ALIAS_SYNTAX)),

    ASSIGNMENT_SIZE(new Builder("assignment-size", "ae")
            .doc("Specifies the size of blocks assigned to end users from this aggregated inet6num assignment.")
            .syntax(NUMBER_SYNTAX)),

    AS_BLOCK(new Builder("as-block", "ak")
            .doc("Range of AS numbers.")
            .syntax(AS_BLOCK_SYNTAX)),

    AS_NAME(new Builder("as-name", "aa")
            .doc("A descriptive name associated with an AS.")
            .syntax(OBJECT_NAME_SYNTAX)),

    AS_SET(new Builder("as-set", "as")
            .doc("Defines the name of the set.")
            .syntax(AS_SET_SYNTAX)),

    AUTH(new Builder("auth", "at")
            .doc("Defines an authentication scheme to be used.")
            .syntax(AUTH_SCHEME_SYNTAX)
            .references(ObjectType.KEY_CERT)),

    AUTHOR(new Builder("author", "ah")
            .doc("References a poem author.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.PERSON, ObjectType.ROLE)),

    AUT_NUM(new Builder("aut-num", "an")
            .doc("The autonomous system number.")
            .syntax(AS_NUMBER_SYNTAX)),

    CERTIF(new Builder("certif", "ce")
            .doc("Contains the public key.")
            .syntax(CERTIF_SYNTAX)),

    CHANGED(new Builder("changed", "ch")
            .doc("Specifies who submitted the update, and when the object was updated. " +
                    "This attribute is filtered from the default whois output.")
            .syntax(CHANGED_SYNTAX)),

    COMPONENTS(new Builder("components", "co")
            .doc("The \"components:\" attribute defines what component routes are used to form the aggregate.")
            .syntax(COMPONENTS_SYNTAX)),

    COUNTRY(new Builder("country", "cy")
            .doc("Identifies the country.")
            .syntax(COUNTRY_CODE_SYNTAX)),

    DEFAULT(new Builder("default", "df")
            .doc("Specifies default routing policies.")
            .syntax(DEFAULT_SYNTAX)),

    DESCR(new Builder("descr", "de")
            .doc("A short decription related to the object.")
            .syntax(FREE_FORM_SYNTAX)),

    DOMAIN(new Builder("domain", "dn")
            .doc("Domain name.")
            .syntax(DOMAIN_SYNTAX)),

    DS_RDATA(new Builder("ds-rdata", "ds")
            .doc("DS records for this domain.")
            .syntax(DS_RDATA_SYNTAX)),

    ENCRYPTION(new Builder("encryption", "en")
            .doc("References a key-cert object representing a CSIRT public key used " +
                    "to encrypt correspondence sent to the CSIRT.")
            .syntax(KEY_CERT_SYNTAX)
            .references(ObjectType.KEY_CERT)),

    EXPORT(new Builder("export", "ex")
            .doc("Specifies an export policy expression.")
            .syntax(EXPORT_SYNTAX)),

    EXPORT_COMPS(new Builder("export-comps", "ec")
            .doc("Defines the set's policy filter, a logical expression which when applied to a set of " +
                    "routes returns a subset of these routes.")
            .syntax(EXPORT_COMPS_SYNTAX)),

    E_MAIL(new Builder("e-mail", "em")
            .doc("The e-mail address of a person, role, organisation or irt team. " +
                    "This attribute is filtered from the default whois output when at least one of the objects " +
                    "returned by the query contains an abuse-mailbox attribute.")
            .syntax(EMAIL_SYNTAX)),

    FAX_NO(new Builder("fax-no", "fx")
            .doc("The fax number of a contact.")
            .syntax(PHONE_SYNTAX)),

    FILTER(new Builder("filter", "fi")
            .doc("Defines the set's policy filter.")
            .syntax(FILTER_SYNTAX)),

    FILTER_SET(new Builder("filter-set", "fs")
            .doc("Defines the name of the filter.")
            .syntax(FILTER_SET_SYNTAX)),

    FINGERPR(new Builder("fingerpr", "fp")
            .doc("A fingerprint of a key certificate generated by the database.")
            .syntax(GENERATED_SYNTAX)),

    FORM(new Builder("form", "fr")
            .doc("Specifies the identifier of a registered poem type.")
            .syntax(POETIC_FORM_SYNTAX)
            .references(ObjectType.POETIC_FORM)
            .listValue()),

    GEOLOC(new Builder("geoloc", "gl")
            .doc("The location coordinates for the resource.")
            .syntax(GEOLOC_SYNTAX)),

    HOLES(new Builder("holes", "ho")
            .doc("Lists the component address prefixes that are not reachable through the aggregate route" +
                    "(perhaps that part of the address space is unallocated).")
            .syntax(HOLES_SYNTAX)
            .listValue()),

    IFADDR(new Builder("ifaddr", "if")
            .doc("Specifies an interface address within an Internet router.")
            .syntax(IFADDR_SYNTAX)),

    IMPORT(new Builder("import", "ip")
            .doc("Specifies import policy expression.")
            .syntax(IMPORT_SYNTAX)),

    INET6NUM(new Builder("inet6num", "i6")
            .doc("Specifies a range of IPv6 addresses in prefix notation.")
            .syntax(IPV6_SYNTAX)),

    INETNUM(new Builder("inetnum", "in")
            .doc("Specifies a range of IPv4 that inetnum object presents. " +
                    "The ending address should be greater than the starting one.")
            .syntax(IPV4_SYNTAX)),

    INET_RTR(new Builder("inet-rtr", "ir")
            .doc("Fully qualified DNS name of the inet-rtr without trailing \".\".")
            .syntax(INET_RTR_SYNTAX)),

    INJECT(new Builder("inject", "ij")
            .doc("Specifies which routers perform the aggregation and when they perform it.")
            .syntax(INJECT_SYNTAX)),

    INTERFACE(new Builder("interface", "ie")
            .doc("Specifies a multiprotocol interface address within an Internet router.")
            .syntax(INTERFACE_SYNTAX)),

    IRT(new Builder("irt", "it")
            .doc("Specifies the name of the irt object. The name should start with the prefix \"IRT-\", " +
                    "reserved for this type of object.")
            .syntax(IRT_SYNTAX)),

    IRT_NFY(new Builder("irt-nfy", "iy")
            .doc("Specifies the e-mail address to be notified when a reference to the irt object is added or removed.")
            .syntax(EMAIL_SYNTAX)),

    KEY_CERT(new Builder("key-cert", "kc")
            .doc("Defines the public key stored in the database.")
            .syntax(KEY_CERT_SYNTAX)),

    LANGUAGE(new Builder("language", "ln")
            .doc("Identifies the language.")
            .syntax(LANGUAGE_CODE_SYNTAX)),

    LOCAL_AS(new Builder("local-as", "la")
            .doc("Specifies the autonomous system that operates the router.")
            .syntax(AS_NUMBER_SYNTAX)),

    MBRS_BY_REF(new Builder("mbrs-by-ref", "mr")
            .doc("This attribute can be used in all \"set\" objects; it allows indirect population of a set. " +
                    "If this attribute is used, the set also includes objects of the corresponding type " +
                    "(aut-num objects for as-set, for example) that are protected by one of these maintainers " +
                    "and whose \"member-of:\" attributes refer to the name of the set. " +
                    "If the value of a \"mbrs-by-ref:\" attribute is ANY, any object of the corresponding type " +
                    "referring to the set is a member of the set. If the \"mbrs-by-ref:\" attribute is missing, " +
                    "the set is defined explicitly by the \"members:\" attribute.")
            .syntax(MBRS_BY_REF_SYNTAX)
            .references(ObjectType.MNTNER)
            .listValue()),

    MEMBERS(new Builder("members", "ms")
            .doc("Lists the members of the set.")
            .syntax(MEMBERS_SYNTAX)
            .listValue()), // No reference checking should be performed for members!

    MEMBER_OF(new Builder("member-of", "mo")
            .doc("This attribute can be used in the route, aut-num and inet-rtr classes. " +
                    "The value of the \"member-of:\" attribute identifies a set object that this object wants " +
                    "to be a member of. This claim, however, should be acknowledged by a " +
                    "respective \"mbrs-by-ref:\" attribute in the referenced object.")
            .syntax(MEMBER_OF_SYNTAX)
            .references(ObjectType.AS_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET)
            .listValue()),

    METHOD(new Builder("method", "mh")
            .doc("Defines the type of the public key.")
            .syntax(METHOD_SYNTAX)),

    MNTNER(new Builder("mntner", "mt")
            .doc("A unique identifier of the mntner object.")
            .syntax(OBJECT_NAME_SYNTAX)),

    MNT_BY(new Builder("mnt-by", "mb")
            .doc("Specifies the identifier of a registered mntner object used for authorisation of operations " +
                    "performed with the object that contains this attribute.")
            .syntax(OBJECT_NAME_SYNTAX)
            .references(ObjectType.MNTNER)
            .listValue()),

    MNT_DOMAINS(new Builder("mnt-domains", "md")
            .doc("Specifies the identifier of a registered mntner object used for reverse domain authorisation. " +
                    "Protects domain objects. The authentication method of this maintainer object will be used for " +
                    "any encompassing reverse domain object.")
            .syntax(OBJECT_NAME_SYNTAX)
            .references(ObjectType.MNTNER)
            .listValue()),

    MNT_IRT(new Builder("mnt-irt", "mi")
            .doc("May appear in an inetnum or inet6num object. It points to an irt object representing a " +
                    "Computer Security Incident Response Team (CSIRT) that handles security incidents for " +
                    "the address space specified by the inetnum or inet6num object.")
            .syntax(IRT_SYNTAX)
            .references(ObjectType.IRT)
            .listValue()),

    MNT_LOWER(new Builder("mnt-lower", "ml")
            .doc("Specifies the identifier of a registered mntner object used for hierarchical authorisation. " +
                    "Protects creation of objects directly (one level) below in the hierarchy of an object type. " +
                    "The authentication method of this maintainer object will then be used upon creation of any " +
                    "object directly below the object that contains the \"mnt-lower:\" attribute.")
            .syntax(OBJECT_NAME_SYNTAX)
            .references(ObjectType.MNTNER)
            .listValue()),

    MNT_NFY(new Builder("mnt-nfy", "mn")
            .doc("Specifies the e-mail address to be notified when an object protected by a mntner is successfully updated.")
            .syntax(EMAIL_SYNTAX)),

    MNT_REF(new Builder("mnt-ref", "mz")
            .doc("Specifies the maintainer objects that are entitled to add references " +
                    "to the organisation object from other objects.")
            .syntax(OBJECT_NAME_SYNTAX)
            .references(ObjectType.MNTNER)
            .listValue()),

    MNT_ROUTES(new Builder("mnt-routes", "mu")
            .doc(new Documented.Multiple(new HashMap<ObjectType, String>() {{
                put(ObjectType.AUT_NUM, "" +
                        "This attribute references a maintainer object which is used in\n" +
                        "determining authorisation for the creation of route6 objects.\n" +
                        "This entry is for the mnt-routes attribute of aut-num class.\n" +
                        "After the reference to the maintainer, an optional list of\n" +
                        "prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                        "follow. The default, when no additional set items are\n" +
                        "specified, is \"ANY\" or all more specifics.");


                put(ObjectType.INET6NUM, "" +
                        "This attribute references a maintainer object which is used in\n" +
                        "determining authorisation for the creation of route6 objects.\n" +
                        "This entry is for the mnt-routes attribute of route6 and inet6num classes.\n" +
                        "After the reference to the maintainer, an optional list of\n" +
                        "prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                        "follow. The default, when no additional set items are\n" +
                        "specified, is \"ANY\" or all more specifics.");

                put(ObjectType.INETNUM, "" +
                        "This attribute references a maintainer object which is used in\n" +
                        "determining authorisation for the creation of route objects.\n" +
                        "After the reference to the maintainer, an optional list of\n" +
                        "prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                        "follow. The default, when no additional set items are\n" +
                        "specified, is \"ANY\" or all more specifics. Please refer to\n" +
                        "RFC-2622 for more information.");

                put(ObjectType.ROUTE, "" +
                        "This attribute references a maintainer object which is used in\n" +
                        "determining authorisation for the creation of route objects.\n" +
                        "After the reference to the maintainer, an optional list of\n" +
                        "prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                        "follow. The default, when no additional set items are\n" +
                        "specified, is \"ANY\" or all more specifics. Please refer to\n" +
                        "RFC-2622 for more information.");

                put(ObjectType.ROUTE6, "" +
                        "This attribute references a maintainer object which is used in\n" +
                        "determining authorisation for the creation of route6 objects.\n" +
                        "This entry is for the mnt-routes attribute of route6 and inet6num classes.\n" +
                        "After the reference to the maintainer, an optional list of\n" +
                        "prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                        "follow. The default, when no additional set items are\n" +
                        "specified, is \"ANY\" or all more specifics.");

            }}))
            .syntax(MNT_ROUTES_SYNTAX)
            .references(ObjectType.MNTNER)),

    MP_DEFAULT(new Builder("mp-default", "ma")
            .doc("Specifies default multiprotocol routing policies.")
            .syntax(MP_DEFAULT_SYNTAX)),

    MP_EXPORT(new Builder("mp-export", "me")
            .doc("Specifies a multiprotocol export policy expression.")
            .syntax(MP_EXPORT_SYNTAX)),

    MP_FILTER(new Builder("mp-filter", "mf")
            .doc("Defines the set's multiprotocol policy filter.")
            .syntax(MP_FILTER_SYNTAX)),

    MP_IMPORT(new Builder("mp-import", "my")
            .doc("Specifies multiprotocol import policy expression.")
            .syntax(MP_IMPORT_SYNTAX)),

    MP_MEMBERS(new Builder("mp-members", "mm")
            .doc("Lists the multiprotocol members of the set.")
            .syntax(MP_MEMBERS_SYNTAX).listValue()),

    MP_PEER(new Builder("mp-peer", "mp")
            .doc(new Multiple(new HashMap<ObjectType, String>() {{
                put(ObjectType.INET_RTR, "Details of any (interior or exterior) multiprotocol router peerings.");
                put(ObjectType.PEERING_SET, "Defines a multiprotocol peering that can be used for importing or exporting routes.");
            }}))
            .syntax(MP_PEER_SYNTAX)),

    MP_PEERING(new Builder("mp-peering", "mg")
            .doc("Defines a multiprotocol peering that can be used for importing or exporting routes.")
            .syntax(MP_PEERING_SYNTAX)),

    NETNAME(new Builder("netname", "na")
            .doc("The name of a range of IP address space.")
            .syntax(NETNAME_SYNTAX)),

    NIC_HDL(new Builder("nic-hdl", "nh")
            .doc("Specifies the NIC handle of a role or person object. When creating an object, one can also " +
                    "specify an \"AUTO\" NIC handle by setting the value of the attribute to \"AUTO-1\" " +
                    "or AUTO-1<Initials>. In such case the database will assign the NIC handle automatically.")
            .syntax(NIC_HANDLE_SYNTAX)),

    NOTIFY(new Builder("notify", "ny")
            .doc("Specifies the e-mail address to which notifications of changes to an object should be sent. " +
                    "This attribute is filtered from the default whois output.")
            .syntax(EMAIL_SYNTAX)),

    NSERVER(new Builder("nserver", "ns")
            .doc("Specifies the nameservers of the domain.")
            .syntax(NSERVER_SYNTAX)),

    ORG(new Builder("org", "og")
            .doc("Points to an existing organisation object representing the entity that holds the resource.")
            .syntax(ORGANISATION_SYNTAX)
            .references(ObjectType.ORGANISATION)),

    ORG_NAME(new Builder("org-name", "on")
            .doc("Specifies the name of the organisation that this organisation object represents in the whois" +
                    "database. This is an ASCII-only text attribute. The restriction is because this attribute is" +
                    "a look-up key and the whois protocol does not allow specifying character sets in queries. " +
                    "The user can put the name of the organisation in non-ASCII character sets in " +
                    "the \"descr:\" attribute if required.")
            .syntax(ORG_NAME_SYNTAX)),

    ORG_TYPE(new Builder("org-type", "ot")
            .doc("Specifies the type of the organisation.")
            .syntax(ORG_TYPE_SYNTAX)),

    ORGANISATION(new Builder("organisation", "oa")
            .doc("Specifies the ID of an organisation object. When creating an object, one has to specify " +
                    "an \"AUTO\" ID by setting the value of the attribute to \"AUTO-1\" or \"AUTO-1<letterCombination>\", " +
                    "so the database will assign the ID automatically.")
            .syntax(ORGANISATION_SYNTAX)),

    ORIGIN(new Builder("origin", "or")
            .doc("Specifies the AS that originates the route." +
                    "The corresponding aut-num object should be registered in the database.")
            .syntax(AS_NUMBER_SYNTAX)
            .references(ObjectType.AUT_NUM)),

    OWNER(new Builder("owner", "ow")
            .doc("Specifies the owner of the public key.")
            .syntax(GENERATED_SYNTAX)),

    PEER(new Builder("peer", "pe")
            .doc("Details of any (interior or exterior) router peerings.")
            .syntax(PEER_SYNTAX)),

    PEERING(new Builder("peering", "pg")
            .doc("Defines a peering that can be used for importing or exporting routes.")
            .syntax(PEERING_SYNTAX)),

    PEERING_SET(new Builder("peering-set", "ps")
            .doc("Specifies the name of the peering-set.")
            .syntax(PEERING_SET_SYNTAX)),

    PERSON(new Builder("person", "pn")
            .doc("Specifies the full name of an administrative, technical or zone contact person for " +
                    "other objects in the database." +
                    "Person name cannot contain titles such as \"Dr.\", \"Prof.\", \"Mv.\", \"Ms.\", \"Mr.\", etc." +
                    "It is composed of alphabetic characters.")
            .syntax(PERSON_ROLE_NAME_SYNTAX)),

    PHONE(new Builder("phone", "ph")
            .doc("Specifies a telephone number of the contact.")
            .syntax(PHONE_SYNTAX)),

    PING_HDL(new Builder("ping-hdl", "pc")
            .doc("References a person or role capable of responding to queries concerning the IP address(es) " +
                    "specified in the 'pingable' attribute.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.PERSON, ObjectType.ROLE)),

    PINGABLE(new Builder("pingable", "pa")
            .doc("Allows a network operator to advertise an IP address of a node that should be reachable from outside " +
                    "networks. This node can be used as a destination address for diagnostic tests. " +
                    "The IP address must be within the address range of the prefix containing this attribute.")
            .syntax(PINGABLE_SYNTAX)),

    POEM(new Builder("poem", "po")
            .doc("Specifies the title of the poem.")
            .syntax(POEM_SYNTAX)),

    POETIC_FORM(new Builder("poetic-form", "pf")
            .doc("Specifies the poem type.")
            .syntax(POETIC_FORM_SYNTAX)),

    REFERRAL_BY(new Builder("referral-by", "rb")
            .doc("This attribute is required in the maintainer object. It may never be altered after the addition " +
                    "of the maintainer. This attribute refers to the maintainer that created this maintainer. " +
                    "It may be multiple if more than one signature appeared on the transaction creating the object.")
            .syntax(OBJECT_NAME_SYNTAX)
            .references(ObjectType.MNTNER)),

    REF_NFY(new Builder("ref-nfy", "rn")
            .doc("Specifies the e-mail address to be notified when a reference to the organisation object is added " +
                    "or removed. This attribute is filtered from the default whois output when at least one of the " +
                    "objects returned by the query contains an abuse-mailbox attribute.")
            .syntax(EMAIL_SYNTAX)),

    REMARKS(new Builder("remarks", "rm")
            .doc("Contains remarks.")
            .syntax(FREE_FORM_SYNTAX)),

    ROLE(new Builder("role", "ro")
            .doc("Specifies the full name of a role entity, e.g. RIPE DBM.")
            .syntax(PERSON_ROLE_NAME_SYNTAX)),

    ROUTE(new Builder("route", "rt")
            .doc("Specifies the prefix of the interAS route. Together with the \"origin:\" attribute, " +
                    "constitutes a primary key of the route object.")
            .syntax(ROUTE_SYNTAX)),

    ROUTE6(new Builder("route6", "r6")
            .doc("Specifies the IPv6 prefix of the interAS route. Together with the \"origin:\" attribute," +
                    "constitutes a primary key of the route6 object.")
            .syntax(ROUTE6_SYNTAX)),

    ROUTE_SET(new Builder("route-set", "rs")
            .doc("Specifies the name of the route set. It is a primary key for the route-set object.")
            .syntax(ROUTE_SET_SYNTAX)),

    RTR_SET(new Builder("rtr-set", "is")
            .doc("Defines the name of the rtr-set.")
            .syntax(RTR_SET_SYNTAX)),

    SIGNATURE(new Builder("signature", "sg")
            .doc("References a key-cert object representing a CSIRT public key used by the team to sign their correspondence.")
            .syntax(KEY_CERT_SYNTAX)
            .references(ObjectType.KEY_CERT)),

    SOURCE(new Builder("source", "so")
            .doc("Specifies the registry where the object is registered. Should be \"RIPE\" for the RIPE Database.")
            .syntax(SOURCE_SYNTAX)),

    STATUS(new Builder("status", "st")
            .doc("Specifies the status of the address range represented by inetnum or inet6num object.")
            .syntax(STATUS_SYNTAX)),

    TECH_C(new Builder("tech-c", "tc")
            .doc("References a technical contact.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.PERSON, ObjectType.ROLE)),

    TEXT(new Builder("text", "tx")
            .doc("Text of the poem. Must be humorous, but not malicious or insulting.")
            .syntax(FREE_FORM_SYNTAX)),

    UPD_TO(new Builder("upd-to", "dt")
            .doc("Specifies the e-mail address to be notified when an object protected by a mntner is unsuccessfully updated.")
            .syntax(EMAIL_SYNTAX)),

    ZONE_C(new Builder("zone-c", "zc")
            .doc("References a zone contact.")
            .syntax(NIC_HANDLE_SYNTAX)
            .references(ObjectType.PERSON, ObjectType.ROLE));

    private static final Map<CIString, AttributeType> TYPE_NAMES = Maps.newHashMapWithExpectedSize(AttributeType.values().length);

    static {
        for (final AttributeType type : AttributeType.values()) {
            TYPE_NAMES.put(ciString(type.getName()), type);
            TYPE_NAMES.put(ciString(type.getFlag()), type);
        }
    }

    private static class Builder {
        private final String name;
        private final String flag;
        private Documented description;
        private AttributeSyntax syntax = AttributeSyntax.ANY_SYNTAX;
        private AttributeValueType valueType = AttributeValueType.SINGLE_VALUE;
        private Set<ObjectType> references = Collections.emptySet();

        private Builder(final String name, final String flag) {
            this.name = name;
            this.flag = flag;
        }

        private Builder doc(final String description) {
            this.description = new Single(description);
            return this;
        }

        private Builder doc(final Documented description) {
            this.description = description;
            return this;
        }

        private Builder syntax(final AttributeSyntax attributeSyntax) {
            this.syntax = attributeSyntax;
            return this;
        }

        private Builder listValue() {
            this.valueType = LIST_VALUE;
            return this;
        }

        private Builder references(final ObjectType... objectTypes) {
            this.references = Collections.unmodifiableSet(Sets.newEnumSet(Lists.newArrayList(objectTypes), ObjectType.class));
            return this;
        }
    }

    private final String name;
    private final String flag;
    private final Documented description;
    private final AttributeSyntax syntax;
    private final AttributeValueType valueType;
    private final Set<ObjectType> references;

    private AttributeType(final Builder builder) {
        this.name = builder.name;
        this.flag = builder.flag;
        this.description = builder.description;
        this.syntax = builder.syntax;
        this.valueType = builder.valueType;
        this.references = builder.references;
    }

    public String getName() {
        return this.name;
    }

    public String getFlag() {
        return flag;
    }

    boolean isListValue() {
        return valueType.equals(LIST_VALUE);
    }

    AttributeSyntax getSyntax() {
        return syntax;
    }

    public boolean isValidValue(final ObjectType objectType, final CIString value) {
        return isValidValue(objectType, value.toString());
    }

    public boolean isValidValue(final ObjectType objectType, final String value) {
        return syntax.matches(objectType, value);
    }

    Iterable<String> splitValue(final String value) {
        return valueType.getValues(value);
    }

    public Set<ObjectType> getReferences() {
        return references;
    }

    public Set<ObjectType> getReferences(final CIString value) {
        if (this == AUTH && value.startsWith(ciString("MD5-PW"))) {
            return Collections.emptySet();
        }

        return references;
    }

    @Override
    public String getDescription(final ObjectType objectType) {
        return description.getDescription(objectType);
    }

    public static AttributeType getByName(final String name) throws IllegalArgumentException {
        final AttributeType attributeType = getByNameOrNull(name);
        if (attributeType == null) {
            throw new IllegalArgumentException("Attribute type " + name + " not found");
        }

        return attributeType;
    }

    @CheckForNull
    public static AttributeType getByNameOrNull(final String name) {
        String nameOrNull = name;
        if (nameOrNull.length() == 3 && nameOrNull.charAt(0) == '*') {
            nameOrNull = nameOrNull.substring(1);
        }

        return TYPE_NAMES.get(ciString(nameOrNull));
    }
}
