package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "notice", propOrder = {
    "title",
    "description",
    "links"
})
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Notice implements Serializable, Comparable<Notice> {
    protected String title;
    protected List<String> description;
    protected List<Link> links;

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public List<String> getDescription() {
        if (description == null) {
            description = Lists.newArrayList();
        }
        return this.description;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = Lists.newArrayList();
        }
        return links;
    }

    @Override
    public int compareTo(Notice o) {
        if (title != null) {
            if (o.getTitle() != null) {
                return title.compareTo(o.getTitle());
            } else {
                return 1;
            }
        } else {
            if (o.getTitle() != null) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
