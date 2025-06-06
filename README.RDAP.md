RIPE NCC RDAP Implementation
-----------------------------
Read more about the RDAP specification in the RFC documents on the IETF site: https://datatracker.ietf.org/wg/weirds/documents/

Entity Object Types
-----------------------------------------------------
Entity RDAP object can be either a PERSON, ROLE, MNTNER or ORGANISATION RPSL object.

Entity object class represents individual persons, informal groups of people, organisations and related information.

Refer to [rfc9083](https://datatracker.ietf.org/doc/rfc9083/) Section 5.1, "The Entity Object Class"

Multiple country attributes are not returned
--------------------------------------------
inetnum and inet6num objects can contain multiple country attributes, but RDAP schema only allows a single value.

This implementation returns the first country attribute value, and any subsequent country attributes are redacted.

Multiple language attributes are not returned
---------------------------------------------
inetnum, inet6num, and organisation can contain multiple language attributes, but RDAP schema only allows a single
value.

This implementation returns the first language attribute value, and any subsequent country attributes are redacted.

Flat AS Model
----------------------------------------
We support the ASN flat model rather than the hierarchical model in our autnum queries. This means that for an autnum for which we have
registration authority but that has not been further delegated by us the service will respond with a Not Found.

For more information refer to https://bitbucket.org/nroecg/nro-rdap-profile/raw/v1/nro-rdap-profile.txt section
6.1.2.3. Flat model

Custom "ZONE" role for domain objects
-------------------------------------
For zone-c attributes in domain objects, a custom "ZONE" role is used, which is not in the RDAP spec.

Ref. https://wiki.tools.ietf.org/html/rfc7483 section 10.2.4.

Organisation role "registrant" is ambiguous
-------------------------------------------
The role "registrant" is used to identify organisation entities, however this is ambiguous as it's also used for mntner entities.

Entity Primary Key can match multiple objects
---------------------------------------------
If an entity primary key matches more than one object, a 500 Internal Server Error is returned.

This can not be easily fixed because the same key can be used in multiple different object types: mntner and
person/role. So it is not clear which object must be returned for a single object request.

For example: https://rdap.db.ripe.net/entity/KR4422-RIPE

Related Contact information is Filtered
---------------------------------------
All related entities (such as a contact or registrant) have filtered contact information, i.e. the "e-mail" attribute
value is not included. Filtered information does not count towards the daily query limit according to the
[Acceptable Use Policy (AUP)](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-acceptable-use-policy).
This was done to avoid blocking clients for inadvertently querying excessively for personal data.

For entity responses, the contact information is not filtered, i.e. the "e-mail" attribute is included.
Clients making entity requests must comply with the daily limit specified in the
[Acceptable Use Policy (AUP)](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-acceptable-use-policy).

For non-entity requests, e-mail addresses are filtered, except for the abuse contact which is always returned.
Attributes related to whois update notification ("notify", "ref-nfy", "upd-to", "mnt-nfy") are filtered
because they are not a general contact email.

Abuse Contact information
--------------------------
Abuse contact information is not filtered because it is not considered personal information. However, this attribute's
`type` does not conform to the [RDAP spec](https://bitbucket.org/nroecg/nro-rdap-profile/raw/v1/nro-rdap-profile.txt)
section 5.1.1, is not "home" or "work" as the RFC specifies. The `type` of this attribute is "abuse". In this
paragraph `type` is considered as an element of the Jcard.
For example:
````
["adr",
    {
        "type":"home",
        "label":"123 Maple Ave\nSuite 90001\nVancouver\nBC\n1239\n"
    }
]
````

Entity Search
--------------------------
Entity search on a handle is limited to returning 100 results, so response size and/or time is not excessive.

This is done as recommendation from the next RFC: https://datatracker.ietf.org/doc/rfc9083/ section 9. To conform with
this spec a notification is added when the output is truncated.

Domain Search
--------------------------
Domain search is restricted to only search for reverse delegations, there are no forward domains in the RIPE database.

Domain search is limited to returning 100 results, so response size and/or time is not excessive.

This is done as recommendation from the next RFC: https://datatracker.ietf.org/doc/rfc9083/ section 9. To conform with
this spec a notification is added when the output is truncated.

Nameserver queries always return Not Implemented
-------------------------------------------------
The RIPE database doesn't contain any forward domain objects, consequently according to the RFC
https://bitbucket.org/nroecg/nro-rdap-profile/raw/v1/nro-rdap-profile.txt section 6.3 "501 Not Implemented" will be
returned.

Only "mnt-by:" Maintainers are Listed as Registrants
-----------------------------------------------------
Only maintainers referenced in "mnt-by:" attributes will be listed as Registrants in responses. It is not relevant
to include the rest of the mntners as they do not maintain the current object, they are not registrant of the object.

RIR Search does not return objects with "administrative" status
-----------------------------------------------------------------
The RDAP RIR Search feature does not return resources with "administrative" status.

Also filtering by status is not implemented, only "active" (non-administrative) objects are returned.

Relation Searches for Autnums always return Not Implemented
-----------------------------------------------------------------
In this case we return a 501 Not Implemented. We don't have any kind of hierarchy for autnums.




