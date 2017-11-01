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

Multiple language attributes in RPSL are not returned
-----------------------------------------------------
inetnum, inet6num, and organisation objects can have multiple language attributes, but only the first language is returned.

https://wiki.tools.ietf.org/html/rfc7483 Appendix C allows multiple lang elements, with a preference assigned to each.

"jCard has the ability to represent multiple language preferences"

A preference should also be assigned to each language (in the order they appear in the RPSL object).

Multiple organisation e-mail attributes are returned, but not with preferences
------------------------------------------------------------------------------
Currently, e-mail attribute values are returned as follows:

    [ "email", { }, "text", "org@test.com" ], [ "email", { }, "text", "org2@test.com" ]

Preferences are not assigned to multiple email elements.

Multiple address attributes are concatenated into one value
-----------------------------------------------------------
Multiple address values are not returned as an array (as suggested in https://tools.ietf.org/html/rfc7483, Appendix C), but are concatenated into one address element.

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

Entity Primary Key can match multiple objects
---------------------------------------------
If an entity primary key matches more than one object, a 500 Internal Server Error is returned.

For example: https://rdap.db.ripe.net/entity/AZRT

