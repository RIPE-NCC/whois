package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remark", propOrder = {
    "description"
})
public class Remark implements Serializable {
    protected List<String> description;

    public List<String> getDescription() {
        if (description == null) {
            description = Lists.newArrayList();
        }
        return this.description;
    }
}
