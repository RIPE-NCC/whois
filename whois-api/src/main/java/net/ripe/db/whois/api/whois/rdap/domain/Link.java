package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "link", propOrder = {
    "value",
    "rel",
    "href",
    "hreflang",
    "title",
    "media",
    "type"
})
public class Link
    implements Serializable
{
    protected String value;
    protected String rel;
    protected String href;
    protected List<String> hreflang;
    protected List<String> title;
    protected String media;
    protected String type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String value) {
        this.rel = value;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String value) {
        this.href = value;
    }

    public List<String> getHreflang() {
        if (hreflang == null) {
            hreflang = new ArrayList<String>();
        }
        return this.hreflang;
    }

    public List<String> getTitle() {
        if (title == null) {
            title = new ArrayList<String>();
        }
        return this.title;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String value) {
        this.media = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }
}
