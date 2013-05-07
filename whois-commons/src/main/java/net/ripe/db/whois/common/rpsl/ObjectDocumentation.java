package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Maps;

import java.util.Map;

class ObjectDocumentation {
    private static final Map<ObjectType, String> DOCUMENTATION = Maps.newEnumMap(ObjectType.class);

    static {
        DOCUMENTATION.put(ObjectType.AS_BLOCK, "" +
                "      An as-block object is needed to delegate a range of AS numbers \n" +
                "      to a given repository.  This object may be used for authorisation \n" +
                "      of the creation of aut-num objects within the range specified \n" +
                "      by the \"as-block:\" attribute.\n");

        DOCUMENTATION.put(ObjectType.AS_SET, "" +
                "      An as-set object defines a set of aut-num objects. The\n" +
                "      attributes of the as-set class are shown in Figure 1.2.2.  The\n" +
                "      \"as-set:\" attribute defines the name of the set. It is an RPSL\n" +
                "      name that starts with \"as-\". The \"members:\" attribute lists the\n" +
                "      members of the set.  The \"members:\" attribute is a list of AS\n" +
                "      numbers, or other as-set names.\n");

        DOCUMENTATION.put(ObjectType.AUT_NUM, "" +
                "      An object of the aut-num class is a database representation of \n" +
                "      an Autonomous System (AS), which is a group of IP networks operated \n" +
                "      by one or more network operators that has a single and clearly \n" +
                "      defined external routing policy.\n");

        DOCUMENTATION.put(ObjectType.DOMAIN, "" +
                "      A domain object represents a Top Level Domain (TLD) or\n" +
                "      other domain registrations. It is also used for Reverse\n" +
                "      Delegations.\n");

        DOCUMENTATION.put(ObjectType.FILTER_SET, "" +
                "      A filter-set object defines a set of routes that are matched by\n" +
                "      its filter.  The \"filter-set:\" attribute defines the name of\n" +
                "      the filter.  It is an RPSL name that starts with \"fltr-\".  The\n" +
                "      \"filter:\" attribute defines the set's policy filter.   A policy\n" +
                "      filter is a logical expression which when applied to a set of\n" +
                "      routes returns a subset of these routes.\n");

        DOCUMENTATION.put(ObjectType.INET6NUM, "" +
                "      An inet6num object contains information on allocations\n" +
                "      and assignments of IPv6 address space.\n");

        DOCUMENTATION.put(ObjectType.INETNUM, "" +
                "      An inetnum object contains information on allocations and\n" +
                "      assignments of IPv4 address space.\n");

        DOCUMENTATION.put(ObjectType.INET_RTR, "" +
                "      Routers are specified using the inet-rtr class.  The \"inet-rtr:\"\n" +
                "      attribute is a valid DNS name of the router described. Each\n" +
                "      \"alias:\" attribute, if present, is a canonical DNS name for the\n" +
                "      router.  The \"local-as:\" attribute specifies the AS number of\n" +
                "      the AS that owns/operates this router.\n");

        DOCUMENTATION.put(ObjectType.IRT, "" +
                "      An irt object is used to define a Computer Security Incident\n" +
                "      Response Team (CSIRT).\n");

        DOCUMENTATION.put(ObjectType.KEY_CERT, "" +
                "      A key-cert object is a database public key certificate \n" +
                "      that is stored on the server and may be used with a mntner \n" +
                "      object for authentication when performing updates. \n" +
                "      Currently only PGP/GnuPG keys are supported.\n");

        DOCUMENTATION.put(ObjectType.MNTNER, "" +
                "      Objects in the RIPE Database may be protected using mntner\n" +
                "      (pronounced \"maintainer\") objects.  A mntner object specifies\n" +
                "      authentication information required to authorise creation,\n" +
                "      deletion or modification of the objects protected by the mntner.\n");

        DOCUMENTATION.put(ObjectType.ORGANISATION, "" +
                "      The organisation class provides information identifying \n" +
                "      an organisation such as a company, charity or university,\n" +
                "      that is a holder of a network resource whose data is stored \n" +
                "      in the whois database.\n");

        DOCUMENTATION.put(ObjectType.PEERING_SET, "" +
                "      A peering-set object defines a set of peerings that are listed \n" +
                "      in its \"peering:\" attributes.  The \"peering-set:\" attribute \n" +
                "      defines the name of the set. \n");

        DOCUMENTATION.put(ObjectType.PERSON, "" +
                "      A person object contains information about technical or\n" +
                "      administrative contact responsible for the object where it is\n" +
                "      referenced.  Once the object is created, the value of the\n" +
                "      \"person:\" attribute cannot be changed.\n");

        DOCUMENTATION.put(ObjectType.POEM, "" +
                "      A poem object contains poems that are submitted by users.\n");

        DOCUMENTATION.put(ObjectType.POETIC_FORM, "" +
                "      A poetic-form object defines the supported poem types. \n");

        DOCUMENTATION.put(ObjectType.ROLE, "" +
                "      The role class is similar to the person class.  However, instead\n" +
                "      of describing a human being, it describes a role performed by\n" +
                "      one or more human beings.  Examples include help desks, network\n" +
                "      monitoring centres, system administrators, etc.  A role object\n" +
                "      is particularly useful since often a person performing a role\n" +
                "      may change; however the role itself remains. The \"nic-hdl:\"\n" +
                "      attributes of the person and role classes share the same name\n" +
                "      space. Once the object is created, the value of the \"role:\"\n" +
                "      attribute cannot be changed.\n");

        DOCUMENTATION.put(ObjectType.ROUTE, "" +
                "      Each interAS route (also referred to as an interdomain route) \n" +
                "      originated by an AS is specified using a route object. The \"route:\" \n" +
                "      attribute is the address prefix of the route and the \"origin:\" \n" +
                "      attribute is the AS number of the AS that originates the route \n" +
                "      into the interAS routing system. \n");

        DOCUMENTATION.put(ObjectType.ROUTE6, "" +
                "      Each interAS route (also referred to as an interdomain route)\n" +
                "      in IPv6 domain originated by an AS is specified using a route6 \n" +
                "      object. The \"route6:\" attribute is the address prefix of the \n" +
                "      route and the \"origin:\" attribute is the AS number of the AS \n" +
                "      that originates the route into the interAS routing system.\n");

        DOCUMENTATION.put(ObjectType.ROUTE_SET, "" +
                "      A route-set object defines a set of routes that can be\n" +
                "      represented by route objects or by address prefixes. In the\n" +
                "      first case, the set is populated by means of the \"mbrs-by-ref:\"\n" +
                "      attribute, in the latter, the members of the set are explicitly\n" +
                "      listed in the \"members:\" attribute. The \"members:\" attribute is\n" +
                "      a list of address prefixes or other route-set names.  Note that\n" +
                "      the route-set class is a set of route prefixes, not of database\n" +
                "      route objects.\n");

        DOCUMENTATION.put(ObjectType.RTR_SET, "" +
                "      A rtr-set object defines a set of routers. A set may be described \n" +
                "      by the \"members:\" attribute, which is a list of inet-rtr names, \n" +
                "      IPv4 addresses or other rtr-set names. A set may also be populated \n" +
                "      by means of the \"mbrs-by-ref:\" attribute, in which case it is \n" +
                "      represented by inet-rtr objects.\n");
    }

    public static String getDocumentation(final ObjectType objectType) {
        final String s = DOCUMENTATION.get(objectType);
        if (s == null) {
            return "";
        }

        return s;
    }
}
