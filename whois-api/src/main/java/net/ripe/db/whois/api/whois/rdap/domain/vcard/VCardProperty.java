package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class VCardProperty {

    protected List<Object> values = Lists.newArrayList();

    public VCardProperty(final String name, final Map parameters, final String type, final String value) {
        this.values.add(name);
        this.values.add(parameters);
        this.values.add(type);
        this.values.add(value);
    }

    public VCardProperty(final String name, final Map parameters, final String type, final List<Object> values) {
        this.values.add(name);
        this.values.add(parameters);
        this.values.add(type);
        this.values.add(values);
    }

    public VCardProperty() {
        // required no-arg constructor
    }

    public List<Object> getObjects() {
        return values;
    }
}
