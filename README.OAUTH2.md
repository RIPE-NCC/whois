WHOIS Oauth2 Implementation
-----------------------------
Read more about the Oauth2 specification in the RFC documents on the IETF site: https://datatracker.ietf.org/doc/html/rfc7519

Audience (`aud`)
-----------------------------------------------------
`aud` usually is the intended recipient of the access token, usually a resource server (API). In the general case, 
the `aud` value is an array of case-sensitive strings, each containing a StringOrURI value. The interpretation of 
audience values is generally application specific. 
WHOIS is using `aud` to differentiate between [IdP Client](#idp-client). This guarantee that the 
token belongs to a specific environment and that cross between environments cannot happen.

Client Id (`client_id`)
-----------------------------------------------------
Regarding the JWT token: `client_id` is a unique identifier for the client application that is requesting access. 
For example, a third-party app that is trying to access to WHOIS in behalf of an end user.

IdP Client
-----------------------------------------------------
The aim of this section is to explain why this property is being used in WHOIS project and to explain the difference 
with [Client Id](#client-id-client_id).
We are using this term as well to identify the environment where the token is created or used. This could be
ambiguous but when referring to the `client_id` in those terms it is the app the access token is intended for or
[Audience](#audience-aud) in other words.

Scope
-----------------------------------------------------
A space-separated list of permissions the application is requesting. It limits what the access token can be used for.
WHOIS is specifying the mntners as scopes or ANY if the access token can be used for all the mntners.
Note that a user can only use mntners where their RIPE NCC account is associated.

Example for API KEYs
-----------------------------------------------------
1. The user using the web page fill the fields specifying the [Scope](#scope), label and expiration date.
2. WHOIS request a new API KEY from the authorisation server (IdP) forwarding all the information filled by the user 
   and specifying the application (which is the [IdP Client](#idp-client)). WHOIS-PREPDEV for example.
3. The API KEY including claims such as [Audience](#audience-aud), [Scope](#scope), and the client's info (like 
   email...) is created by the IdP. Note that the [IdP Client](#idp-client) is turned [Audience](#audience-aud).
4. The user uses the API KEY to make an update in WHOIS.
5. WHOIS extracts the necessary information from the API KEY using the IdP. All the information mentioned in the 3rd 
   point will be taking including the [Client Id](#client-id-client_id).
6. WHOIS validates the [Audience](#audience-aud) to guarantee that the token belongs to the correct environment and 
   the [Scope](#scope) to validate if the token has privileges in the mntner.
