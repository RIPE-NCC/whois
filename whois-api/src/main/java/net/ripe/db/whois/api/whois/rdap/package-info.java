/**
 * <p>Welcome to the RIPE Database RDAP API documentation.</p>
 *
 * <div>The RDAP rest api handles requests for 5 different object types:</div>
 * <ul>
 *     <li><a href="path__-objectType-_-key-.html">ip</a></li>
 *     <li><a href="path__-objectType-_-key-.html">autnum</a></li>
 *     <li><a href="path__-objectType-_-key-.html">domain</a></li>
 *     <li><a href="path__-objectType-_-key-.html">entity</a></li>
 *     <li><a href="path__-objectType-_-key-.html">nameserver</a></li>
 * </ul>
 *
 * For more information on RDAP, see <a href="http://datatracker.ietf.org/doc/search/?name=rdap&rfcs=on&activeDrafts=on">RFCs on ietf.org</a>
 */
@XmlSchema(
        namespace = "http://rdap.ripe.net",
        elementFormDefault = XmlNsForm.QUALIFIED)
package net.ripe.db.whois.api.whois.rdap;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
