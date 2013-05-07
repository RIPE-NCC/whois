/**
 * The RIPE WHOIS DB REST API is currently only available for internal use.
 * <p/>
 * When using the API it is mandatory to specify a valid API key as a request parameter in the form.
 * <p/>
 * <code>http://host:port/api/question?apiKey=API_KEY</code>
 * <p/>
 * To obtain an API KEY contact the RIPE DB development team.
 */
@XmlSchema(
        namespace = "http://www.ripe.net/whois",
        elementFormDefault = XmlNsForm.QUALIFIED)
package net.ripe.db.whois.api.acl;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
