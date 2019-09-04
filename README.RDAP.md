RIPE NCC RDAP Implementation
-----------------------------
Read more about the RDAP specification in the RFC documents on the IETF site: https://datatracker.ietf.org/wg/weirds/documents/

Multiple country attributes are not returned
--------------------------------------------
inetnum and inet6num objects can contain multiple country attributes, but RDAP only allows a single value.

This implementation returns the first country attribute value, and includes an explanatory notice.

Multiple language attributes are not returned
---------------------------------------------
inetnum, inet6num, and organisation objects can have multiple language attributes, but only the first language is returned.

Multiple organisation e-mail and phone attributes are returned, but not with preferences
----------------------------------------------------------------------------------------
Preferences are not assigned to multiple e-mail or phone elements.

Syntax checks are performed on entity values, and 400 Bad Request is returned on invalid syntax
-----------------------------------------------------------------------------------------------
400 Bad Request, and not 404 Not Found, is returned for an entity name with an invalid syntax.

For example: curl -v https://rdap.db.ripe.net/entity/invalid

AS block returned if AS number not found
----------------------------------------
If an AS number is allocated to the RIPE region, that is returned.

If an AS number is allocated to a different region, a redirect is returned.

If an AS number is not allocated to any region, the parent AS block is returned. This includes reserved AS numbers.


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

(This will be fixed in the next Whois release).

For example: https://rdap.db.ripe.net/entity/AZRT

Organisation entity is not returned in ip or autnum query responses
-------------------------------------------------------------------
An org: reference from an inetnum,inet6num,autnum resource is not included in the query response.

By contrast, ARIN returns the org reference as a "registrant" entity.

(This will be fixed in the next Whois release).

Related Contact information is Filtered
---------------------------------------
Any related contact entities ("technical","administrative","abuse" etc.) have filtered contact information, i.e. "e-mail" and "notify" values are not included. This was done to avoid blocking clients for inadvertently querying excessively for personal data.

A workaround is to query for each entity separately using the contact's nic-hdl, and the unfiltered information is returned (although a limit for personal data does apply).


Related Contact Information is not Returned for Resources
---------------------------------------------------------
Related contact entities ("technical","administrative","abuse" etc.) are not returned for resources (ip, autnum), but should be.

(This will be fixed in the next Whois release).


startAddress and endAddress do not include prefix length
--------------------------------------------------------
For an ip network object class, the "startAddress" and "endAddress" field values are in CIDR format, but do not include prefix length.


Resource handle and parentHandle
---------------------------------
For an ip network object class, the "handle" field uses the closest-matching prefix range, whereas the "parentHandle" field uses the parent "netname" value.

(This will be fixed in the next Whois release).


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

