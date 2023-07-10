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

Services
-----------------------------------------------------
* The Lookup service enables searching for one object. The output is in RDAP format.
    ```
    https://rdap.db.ripe.net/{objectType}/{key}
    ```
  * ObjectType: Is the key's object type. The values "autnum", "domain", "ip", and "entity" are all possible. As 
    stated above, "nameserver" is not implemented.
    * "autnum": Refers to autnum or as-block. Example: https://rdap.db.ripe.net/autnum/3333
    * "domain": Refers to domain. Example: https://rdap.db.ripe.net/domain/196.46.95.in-addr.arpa
    * "ip": Refers to inet(6)nums. Example: https://rdap.db.ripe.net/ip/193.0.0.0/21 or https://rdap.db.ripe.net/ip/2001:67c:2e8::/48
    * "entity": Refers to Person, Role or Mntner. Example: https://rdap.db.ripe.net/entity/RIPE-NCC-MNT
  * Key: Term to search for.


* The Entities service enables searching for matches based on a term. The output is a List with objects in RDAP format.
    ```
    https://rdap.db.ripe.net/entities?fn={fn}&handle={handle}
    ```
  * fn: Returns all the matches for **person:**, **role:**, and **org-name:** attributes. Example: https://rdap.db.ripe.net/entities?fn=RIPE-RIPE
  * handle: Returns all the matches for **organisation:**, **nic-hdl:** attributes. Example: https://rdap.db.ripe.net/entities?handle=RIPE-RIPE


* The Domains service enables searching for matches based on a term. The output is a List with objects in RDAP format.
    ```
    https://rdap.db.ripe.net/domains?name={name}
    ```
    * name: Returns all the matches for **domain:** attribute. Example: https://rdap.db.ripe.net/domains?name=196.46.95.in-addr.arpa


* The Help service return a valid RDAP object containing the default set of notices for the service. The 
  rdapConformance element in the response include the extension identifiers for all the extension implemented.
    ```
    https://rdap.db.ripe.net/help
    ```



