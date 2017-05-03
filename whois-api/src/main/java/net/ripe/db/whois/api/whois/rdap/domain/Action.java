package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "action")
@XmlEnum
@XmlJavaTypeAdapter(Action.Adapter.class)
public enum Action {

    REGISTRATION("registration"),
    REREGISTRATION("reregistration"),
    LAST_CHANGED("last changed"),
    EXPIRATION("expiration"),
    DELETION("deletion"),
    REINSTANTIATION("reinstantiation"),
    TRANSFER("transfer");

    final String value;

    Action(final String value) {
        this.value = value;
    }

    public static class Adapter extends XmlAdapter<String, Action> {

        @Override
        public Action unmarshal(final String value) throws Exception {
            for (Action action : Action.values()) {
                if (action.value.equals(value)) {
                    return action;
                }
            }
            throw new IllegalArgumentException(value);
        }

        @Override
        public String marshal(final Action action) throws Exception {
            return action.value;
        }
    }
}
