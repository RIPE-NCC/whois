package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "status")
@XmlEnum
@XmlJavaTypeAdapter(Status.Adapter.class)
public enum Status {

    VALIDATED("validated"),
    UPDATE_PROHIBITED("update prohibited"),
    TRANSFER_PROHIBITED("transfer prohibited"),
    DELETE_PROHIBITED("delete prohibited"),
    PROXY("proxy"),
    PRIVATE("private"),
    REDACTED("redacted"),
    OBSCURED("obscured");

    final String value;

    Status(final String value) {
        this.value = value;
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
