@XmlSchema(
    xmlns = {
        @XmlNs(prefix = "xlink", namespaceURI = "http://www.w3.org/1999/xlink"),
        @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
    },
    elementFormDefault = XmlNsForm.QUALIFIED,
    location = "http://apps.db.ripe.net/whois-beta/xsd/whois-resources.xsd"
)

package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

