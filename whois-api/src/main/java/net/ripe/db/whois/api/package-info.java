/**
 * Welcome to the RIPE Database REST API documentation.
 * <p>
 * These pages are auto-generated from the source code at compilation time.
 * What you see here is always the complete documentation of the latest deployed code.</p>
 *
 * <p>In compliance with the REST paradigm our web services are invoked via plain HTTP requests, using the HTTP method that is most appropriate for the given kind of request. The web services responses are returned in the form of an HTTP response that contains a response code describing success or failure of the operation, some response headers and eventually a response body.</p>
 *
 * <p><div>All the services are also accessible on the HTTPS protocol.</div>
 * The Update Services require password authentication. For this reason HTTPS is highly recommended on these services and in future releases their access on HTTP may be deprecated.</p>
 *
 * <h3>The Query services</h3>
 *   <ul>
 *       <li><a href="path__geolocation.html">geolocation</a></li>
 *       <li><a href="path__lookup_-source-_-objectType-_-key-.html">lookup</a></li>
 *       <li><a href="path__grs-lookup_-source-_-objectType-_-key-.html">grs-lookup</a></li>
 *       <li><a href="path__metadata_sources.html">metadata (list sources)</a></li>
 *       <li><a href="path__metadata_templates_-objectType-.html">metadata (object template for given object type)</a></li>
 *       <li><a href="path__search.html">search</a></li>
 *       <li><a href="path__tags_-source-_-key-.html">tags</a></li>
 *       <li><a href="path__versions_-source-_-key-.html">versions</a></li>
 *       <li><a href="path__version_-source-_-version-_-key-.html">version</a></li>
 *   </ul>
 *
 * <h3>The Update services</h3>
 *  <div><b>Rest services</b></div>
 *  <ul>
 *      <li><a href="path__create.html">create</a></li>
 *      <li><a href="path__create_-source.html">create (for given source)</a></li>
 *      <li><a href="path__delete_-source-_-objectType-_-key-.html">delete</a></li>
 *      <li><a href="path__modify_-source-_-objectType-_-key-.html">modify</a></li>
 *      <li><a href="path__update_-source-_-objectType-_-key-.html">update</a></li>
 *  </ul>
 *
 * <p>The Update Services are REST interfaces that you can use to Create, Update and Delete RIPE Database objects. Additionally we consider part of the Update family also a set of Modify interfaces. Modify interfaces simplify all those object manipulations that would otherwise require complicate client side workflows, like modifying an attribute at a given index, replacing all the attributes of a given type with new attributes, adding attributes at a given index, etc. New modify interfaces will be provided in future depending on the feedback that we will receive on this project.</p>
 * <p>Unlike the legacy interfaces Mail Update and Syncupdates all the new Update REST Services accept only one single object per request.</p>
 *
 *  <div><b>Syncupdates</b></div>
 *  <ul>
 *      <li><a href="path__syncupdates_-source-.html">syncupdates</a></li>
 *  </ul>
 *
 * <h3>Content negotiation</h3>
 * <p>All our services support the standard HTTP/1.1 content negotiation method.</p>
 *
 * <p>The client must specify the desired response style setting the "Accept" header, otherwise the default response style will be XML. The HTTP response body will be therefore formatted in the requested style and a "Content-Type" header in the HTTP response will confirm the setting.</p>
 *
 * <p><div>The two possible values that you can specify for the Accept header are:</div>
 *
 * <ul>
 *  <li>application/xml or text/xml for XML</li>
 *  <li>application/json or text/json for JSON</li>
 * </ul>
 * Clients can also append an extension of .xml or .json to their request URL instead of setting an Accept header. The server will return a response in the appropriate format for that given extension.</p>
 *
 * <h3>Status codes</h3>
 * <p>In compliance with the REST paradigm any error information is returned in the form of a standard HTTP response with an HTTP status code describing the error and a text/plain body message describing the exception causing the error response.</p>
 * <p>We are using only standard HTTP codes (<a class="external-link" href="http://www.iana.org/assignments/http-status-codes" rel="nofollow" target="_blank">http://www.iana.org/assignments/http-status-codes </a>).</p>
 * <p>The following table gives a brief description of the mapping between standard Whois responses and the related REST services status codes.</p>
 * <table class="grid listing">
 *  <thead>
 *   <tr><th>System Exception</th><th>Whois Error</th><th>HTTP Status Code</th></tr>
 *  </thead>
 *  <tbody>
 *   <tr><td>IllegalArgumentException</td><td></td><td>Bad Request (400)</td></tr>
 *   <tr><td>IllegalStateException</td><td></td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>UnsupportedOperationException</td><td></td><td>Bad Request (400)</td></tr>
 *   <tr><td>ObjectNotFoundException</td><td></td><td>Not Found (404)</td></tr>
 *   <tr><td>IllegalStateException</td><td></td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>IOException</td><td></td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>SystemException</td><td></td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>TooManyResultsException</td><td></td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>WhoisServerException</td><td>No Entries Found (101)</td><td>Not Found (404)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Unknown Source (102)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Unknown Object Type (103)</td> <td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Unknown Attribute in Query (104)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Attribute Is Not Inverse Searchable (105)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>No Search Key Specified (106)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Input Line too Long (107)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Bad Character in Input (108)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Invalid Combination of Flags Passed (109)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Access Denied (201)</td><td>Forbidden (403)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Access Control Limit Reached (202)</td><td>Forbidden (403)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Address Passing Not Allowed (203)</td><td>Bad Request (400)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Maximum Referral Lines Exceeded (204)</td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Connection Refused (208)</td><td>Forbidden (403)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Connection Has Been Closed(301)</td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Referral Timeout (302)</td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>WhoisServerException</td><td>No Referral Host (303)</td><td>Internal Server Error (500)</td></tr>
 *   <tr><td>WhoisServerException</td><td>Referral Host Not Responding (304)</td><td>Internal Server Error (500)</td></tr>
 * </tbody>
 * </table>
 *
 *  <p>Clients should avoid any form of coupling with the the text/plain error message contained in response body since it may change between different releases of the API and is only intended as a starting point for debugging the real causes of the exception event.</p>
 *  <p>The following table show an example of a possible mapping for client side error messages generic enough for the four HTTP error codes:</p>
 *  <table class="grid listing">
 *    <thead>
 *      <tr><th>HTTP Status Code</th><th>Error Message</th></tr>
 *    </thead>
 *    <tbody>
 *      <tr><td>Bad Request (400)</td><td class="ripeTd">The service is unable to understand and process the query.</td></tr>
 *      <tr><td>Forbidden (403)</td><td class="ripeTd">Query limit exceeded.</td></tr>
 *      <tr><td>Not Found (404)</td><td class="ripeTd">No results were found for your search.</td></tr>
 *      <tr><td>Conflict (409)</td><td class="ripeTd">The request couldn't be accepted because some integrity constraint was violated.</td></tr>
 *      <tr><td>Internal Server Error (500)</td><td>The server encountered an unexpected condition which prevented it from fulfilling the request.</td></tr>
 *   </tbody>
 * </table>
 *
 * <p>The most frequent reasons for a Conflict 409 status code are described in the following table:
 * <table>
 * <thead><tr>Violated Constraint</tr></thead>
 * <tbody>
 *  <tr><td>UPDATE</td><td>The person/role attribute in a person/role object cannot be changed</td></tr>
 *  <tr><td>DELETE</td><td>The object is referenced from other objects</td></tr>
 *  <tr><td>CREATE</td><td>One of this object's attributes contains a key to an object that doesn't exist</td></tr>
 *  <tr><td>CREATE</td><td>An object with the requested nic-hdl already exists</td></tr>
 *  <tr><td>CREATE</td><td>The requested nic-hdl is not available because an object with that nic-hdl existed in the past</td></tr>
 * </tbody>
 * </table></p>
 */
@XmlSchema(
        namespace = "http://www.ripe.net/whois",
        elementFormDefault = XmlNsForm.QUALIFIED)
package net.ripe.db.whois.api;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
