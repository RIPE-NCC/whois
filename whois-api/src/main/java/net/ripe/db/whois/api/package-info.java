/**
 * Welcome to the RIPE Database REST API documentation.
 *
 * <p>
 * These pages are auto-generated from the source code at compilation time, so should accurately describe
 * the current behaviour of the deployed code.</p>
 *
 * <p>These REST services are invoked via HTTP requests, using the HTTP method that is most appropriate.
 * The HTTP response contains a status code, some headers and the body data.</p>
 *
 * <p><div>All the services are accessible via HTTPS.</div>
 * As the update services use cleartext passwords for authentication, it is highly recommended to use HTTPS.</p>
 *
 * <h3>Query Services</h3>
 *   <ul>
 *       <li><a href="path__geolocation.html">geolocation</a></li>
 *       <li><a href="path__lookup_-source-_-objectType-_-key-.html">lookup</a></li>
 *       <li><a href="path__grs-lookup_-source-_-objectType-_-key-.html">grs-lookup</a></li>
 *       <li><a href="path__metadata_sources.html">metadata (list sources)</a></li>
 *       <li><a href="path__metadata_templates_-objectType-.html">metadata (object template for given object type)</a></li>
 *       <li><a href="path__search.html">search</a></li>
 *       <li><a href="path__grs-search.html">grs-search</a></li>
 *       <li><a href="path__versions_-source-_-key-.html">versions</a></li>
 *       <li><a href="path__version_-source-_-version-_-key-.html">version</a></li>
 *   </ul>
 *
 * <p>The Query Services are REST interfaces that can be used to search for, or directly lookup, RIPE database objects.</p>
 *
 * <h3>Update Services</h3>
 *  <div><b>REST Services</b></div>
 *  <ul>
 *      <li><a href="path__create_-source-.html">create</a></li>
 *      <li><a href="path__modify_-source-_-objectType-_-key-.html">modify</a></li>
 *      <li><a href="path__update_-source-_-objectType-_-key-.html">update</a></li>
 *      <li><a href="path__delete_-source-_-objectType-_-key-.html">delete</a></li>
 *  </ul>
 *
 * <p>The Update Services are REST interfaces that you can use to create, update and delete RIPE database objects.</p>
 *
 * <p>Unlike the other update interfaces (Mail Update and Syncupdates), the Update Services accepts only a single object per request.</p>
 *
 * <div><b>Syncupdates</b></div>
 * <ul>
 *     <li><a href="path__syncupdates_-source-.html">syncupdates</a></li>
 * </ul>
 *
 * <h3>Content Negotiation</h3>
 * <p>All services support the standard HTTP/1.1 content negotiation method.</p>
 *
 * <p>The client should specify the desired response format using the "Accept" header in the HTTP request.
 * If the response format is not specified, then the default response format will be XML.
 * The HTTP response will include a "Content-Type" header, and the response body will be encoded in the requested format.</p>
 *
 * <p><div>The possible values that you can specify for the Accept header are:</div>
 * <ul>
 *  <li>"application/xml" or "text/xml" for XML</li>
 *  <li>"application/json" or "text/json" for JSON</li>
 * </ul>
 * Clients can also append an extension of .xml or .json to the request URL instead of setting an Accept header.
 * The server will return a response in the appropriate format for that given extension.</p>
 *
 * <h3>Status Codes</h3>
 * <p>In compliance with the REST paradigm any error information is returned in the form of a standard HTTP response
 * with an HTTP status code describing the error and a plain-text body message describing the error.</p>
 *
 * <p>Client applications should use the HTTP status code to detect the result of an operation. Any error message text
 * is intended as a description only, and may change at any time.</p>
 *
 * <p>Possible reasons for various HTTP status codes are as follows:</p>
 * <table class="grid listing">
 *  <thead>
 *   <tr><th>HTTP Status Code</th><th>Cause</th></tr>
 *  </thead>
 *  <tbody>
 *   <tr><td>Bad Request (400)</td><td class="ripeTd">The service is unable to understand and process the query.</td></tr>
 *   <tr><td>Forbidden (403)</td><td class="ripeTd">Query limit exceeded.</td></tr>
 *   <tr><td>Not Found (404)</td><td class="ripeTd">No results were found on a search request, or an attempt was made to change a non-existant object.</td></tr>
 *   <tr><td>Conflict (409)</td><td class="ripeTd">The request couldn't be accepted because some integrity constraint was violated.</td></tr>
 *   <tr><td>Internal Server Error (500)</td><td>The server encountered an unexpected condition which prevented it from fulfilling the request.</td></tr>
 *  </tbody>
 * </table>
 *
 * <p>A complete list of HTTP status codes is available here: <a class="external-link" href="http://www.iana.org/assignments/http-status-codes"
 * rel="nofollow" target="_blank">http://www.iana.org/assignments/http-status-codes </a>.</p>
 *
 */
@XmlSchema(
        namespace = "http://www.ripe.net/whois",
        elementFormDefault = XmlNsForm.QUALIFIED)
package net.ripe.db.whois.api;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
