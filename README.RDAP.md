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

Multiple organisation phone attributes are returned, but not with preferences
----------------------------------------------------------------------------------------
Preferences are not assigned to multiple phone elements.

Flat AS Model
----------------------------------------
Not Found (404) is thrown if AS number is not found.

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

For example: https://rdap.db.ripe.net/entity/KR4422-RIPE

Related Contact information is Filtered
---------------------------------------
Any related contact entities ("technical","administrative","abuse" etc.) have filtered contact information, i.e. "e-mail" and "notify" values are not included. This was done to avoid blocking clients for inadvertently querying excessively for personal data.

A workaround is to query for each entity separately using the contact's nic-hdl, and the unfiltered information is returned (although a limit for personal data does apply).

Entity Search
--------------------------
Entity search on a handle is limited to returning 100 results.

Domain Search
--------------------------
Domain search is restricted to only search for reverse delegations, and results are limited to 100.

Netname may not match Whois
----------------------------
The netname value returned by RDAP may not match what is returned by Whois.

Nameserver queries always return Not Implemented
-------------------------------------------------
The RIPE database doesn't contain any forward domain objects, consequently a nameserver query will always return Not Implemented.

Only "mnt-by:" Maintainers are Listed as Registrants
-----------------------------------------------------
Only maintainers referenced in "mnt-by:" attributes will be listed as Registrants in responses.

Objects with "administrative" status are not returned
-----------------------------------------------------
If the prefix is either delegated but unallocated or only partially delegated to the RIPE region, then a 404 is returned. An object with "administrative" status is never returned.

Refer to NRO RDAP Profile section 4.5. "Status"
