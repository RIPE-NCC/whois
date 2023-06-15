package net.ripe.db.whois.api.rdap.domain;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "status")
@XmlEnum
@XmlJavaTypeAdapter(Status.Adapter.class)
public enum Status {

    RESERVED("reserved"),
    ACTIVE("active"),
    ADMINISTRATIVE("administrative");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static class Adapter extends XmlAdapter<String, Status> {

        @Override
        public Status unmarshal(final String value) throws Exception {
            for (Status status : Status.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException(value);
        }

        @Override
        public String marshal(final Status status) throws Exception {
            return status.value;
        }
    }
}
