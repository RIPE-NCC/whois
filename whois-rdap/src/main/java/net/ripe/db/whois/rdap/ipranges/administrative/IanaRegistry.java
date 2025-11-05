package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"records"})
public class IanaRegistry {

    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private List<IanaRecord> records;

    public List<IanaRecord> getRecords() {
        return records;
    }

    public void setRecords(List<IanaRecord> records) {
        this.records = records;
    }
}