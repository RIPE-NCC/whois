RIPE NCC RDAP Implementation
-----------------------------
Read more about the RDAP specification in the RFC documents on the IETF site: https://datatracker.ietf.org/wg/weirds/documents/

Multiple country attributes allowed in inetnum and inet6num objects, but only 1 country attribute in RDAP spec.
---------------------------------------------------------------------------------------------------------------
This implementation interpreted the RFC7483 to only allow a single country code for a resource:

    "country -- a string containing the two-character country code of the network"
    https://datatracker.ietf.org/doc/rfc7483/?include_text=1

Multiple identically named elements should not be used in JSON, as it causes interoperability issues (https://tools.ietf.org/html/rfc7159, section 4).

The jCard spec does allow multiple records of the same type, with a "pref" element for list them by preference. This is allowed for certain fields:

    "jCard has the ability to represent multiple language preferences, multiple email address and phone numbers, and multiple postal addresses in both a structured and unstructured format."

This approach could be used for for country codes also, but it may not conform with the RDAP spec. 

Example:
* Request: https://rdap.db.ripe.net/ip/37.246.0.0/16
 * Response: 400 Bad Request, including the message "Multiple country: found in 37.246.0.0 - 37.246.255.255".

Multiple language attributes in RPSL are not returned
-----------------------------------------------------
inetnum, inet6num, and organisation objects can have multiple language attributes, but only the first language is returned.

https://wiki.tools.ietf.org/html/rfc7483 Appendix C allows multiple lang elements, with a preference assigned to each.

"jCard has the ability to represent multiple language preferences"

A preference should also be assigned to each language (in the order they appear in the RPSL object).

Example:
* Request:
 * Response:

Multiple organisation e-mail and phone attributes are returned, but not with preferences
------------------------------------------------------------------------------
Preferences are not assigned to multiple e-mail or phone elements.

Example:
* Request: https://rdap.db.ripe.net/entity/ORG-EA40-RIPE
 * Response:
 [ "email", { }, "text", "noc@sovintel.ru" ], [ "email", { }, "text", "registry@eltel.net" ]

Multiple address attributes are concatenated into one value
-----------------------------------------------------------
Multiple address values are not returned as an array (as suggested in https://tools.ietf.org/html/rfc7483, Appendix C), but are concatenated into one address element.

Example:
* Request:
 * Response:

Syntax checks are performed on entity values, and 400 Bad Request is returned on invalid syntax
-----------------------------------------------------------------------------------------------
400 Bad Request, and not 404 Not Found, is returned for an entity name with an invalid syntax.

For example: curl -v https://rdap.db.ripe.net/entity/invalid

Reserved AS numbers
-------------------
404 Not Found is returned for reserved AS numbers.

For example: curl -v https://rdap.db.ripe.net/autnum/65535

The jCard adr (address) property value is set to "null"
-------------------------------------------------------
The jCard adr (address) property value is incorrectly set to "null", and the address is set in the "label" element instead.

For example: curl -v https://rdap.db.ripe.net/entity/ORG-RIEN1-RIPE

Returns: 

  "vcardArray" : [ "vcard", [ 
    ...
    [ "adr", {"label" : "P.O. Box 10096\n1016 EB\nAmsterdam\nNETHERLANDS"}, "text", null ]

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

For example: https://rdap.db.ripe.net/entity/AZRT

Mntner entity not supported
---------------------------
Lookup organisation,person and role entities are supported, but mntner objects are not. 

Organisation entity is not returned in ip or autnum query responses
-------------------------------------------------------------------
An org: reference from an inetnum,inet6num,autnum resource is not included in the query response.

By contrast, ARIN returns the org reference as a "registrant" entity.

Related Contact information is Filtered
---------------------------------------
Any related contact entities ("technical","administrative","abuse" etc.) have filtered contact information, i.e. "e-mail" and "notify" values are not included. This was done to avoid blocking clients for inadvertently querying excessively for personal data.

A workaround is to query for each entity separately using the contact's nic-hdl, and the unfiltered information is returned (although a limit for personal data does apply).


Related Contact Information is not Returned for Resources
---------------------------------------------------------
Related contact entities ("technical","administrative","abuse" etc.) are not returned for resources (ip, autnum), but should be.



CIDR ranges for IP networks include prefix length
-------------------------------------------------
For an ip network object class, the "startAddress" and "endAddress" field values are in CIDR format (including prefix length).

The prefix length should not be included (see examples in RFC7483).

Example:
* Request: https://rdap.db.ripe.net/ip/2003::/18
 * Response: startAddress should be 2003:: and endAddress should be 2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff.


Resource handle and parentHandle
---------------------------------
For an ip network object class, the "handle" field uses the closest-matching prefix range, whereas the "parentHandle" field uses the parent "netname" value.


Entity Search is Disabled
--------------------------
Entity search on a handle is disabled, as matching a large number of objects causes Whois to run out of memory.

Example: 
* Request: /entities?handle=\*
 * Response: 403 Forbidden


Domain Search is Disabled
--------------------------
Domain search is disabled, as matching a large number of objects causes Whois to run out of memory.

Example:
* Request: /domains?name=XXXX
 * Response: 403 Forbidden


Netname may not match Whois
----------------------------
The netname value returned by RDAP may not match what is returned by Whois.

Help Returns Not Found
-----------------------
Requesting /help returns 404 Not Found.


Abuse-c Contact does not include Abuse Mailbox
-----------------------------------------------
An organisation's abuse-c email address is not included in an inetnum, inet6num, aut-num response. The abuse-c role is added as an "abuse" contact, but only an "e-mail" attribute is included in the vCard, not the "abuse-mailbox" attribute.

Example: https://rdap.db.ripe.net/autnum/8447


Entity does not include networks
---------------------------------
An entity (i.e. for an organisation) should include any related networks. 

This list of networks should have a maximum size to prevent the response from growing too large and taking too long.

Ref. RFC 7483, Section 5.1 The Entity Object Class. (https://tools.ietf.org/html/rfc7483#section-5.1).

Example:
* Request: http://rdap.db.ripe.net/entity/ORG-RIEN1-RIPE
 * Response: Should include "networks" element with referenced networks, including 193.0.0.0 - 193.0.23.255


Nameserver queries always return Not Found
-------------------------------------------
The RIPE database doesn't contain any forward domain objects, consequently a nameserver query will always return Not Found.

