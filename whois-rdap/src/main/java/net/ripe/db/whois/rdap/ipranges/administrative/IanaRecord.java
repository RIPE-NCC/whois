package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public class IanaRecord {

    @XmlElement(name = "prefix", namespace = "http://www.iana.org/assignments")
    @XmlJavaTypeAdapter(IpIntervalAdapter.class)
    private IpInterval prefix;

    @XmlElement(name = "rdap", namespace = "http://www.iana.org/assignments")
    private Rdap rdap;


    public IpInterval getPrefix() {
        return prefix;
    }

    public void setPrefix(IpInterval prefix) {
        this.prefix = prefix;
    }

    public Rdap getRdap() {
        return rdap;
    }

    public void setRdap(Rdap rdap) {
        this.rdap = rdap;
    }

    static class IpIntervalAdapter extends XmlAdapter<String, IpInterval> {
        @Override
        public IpInterval unmarshal(final String value) {
            return StringUtils.isEmpty(value)  ? null : IpInterval.parse(value);
        }

        @Override
        public String marshal(IpInterval interval) {
            return (interval != null) ? interval.toString() : null;
        }
    }
}
