package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "notice", propOrder = {
    "title",
    "description",
    "links"
})
public class Notice
    implements Serializable
{
    protected String title;
    protected List<String> description;
    protected Link links;

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<String>();
        }
        return this.description;
    }

    public Link getLinks() {
        return links;
    }

    public void setLinks(Link value) {
        this.links = value;
    }
}
