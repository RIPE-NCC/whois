package net.ripe.db.whois.rdap.domain.vcard;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class VCardProperty {

    protected List<Object> values = Lists.newArrayList();

    public VCardProperty(final VCardName name, final Map parameters, final VCardType type, final String value) {
        this.values.add(name.getValue());
        this.values.add(parameters);
        this.values.add(type.getValue());
        this.values.add(value);
    }

    public VCardProperty(final VCardName name, final Map parameters, final VCardType type, final List<Object> values) {
        this.values.add(name.getValue());
        this.values.add(parameters);
        this.values.add(type.getValue());
        this.values.add(values);
    }

    public VCardProperty() {
        // required no-arg constructor
    }

    public List<Object> getObjects() {
        return values;
    }
}
