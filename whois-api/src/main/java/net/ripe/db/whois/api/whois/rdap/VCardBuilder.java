package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.VCard;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.VCardProperty;

import java.util.List;
import java.util.Map;

public class VCardBuilder {

    private final List<VCardProperty> properties = Lists.newArrayList();

    public VCardBuilder addAdr(final Map parameters, final List values) {
        properties.add(new VCardProperty("adr", parameters, "text", values));
        return this;
    }

    public VCardBuilder addAdr(final List values) {
        return addAdr(Maps.newHashMap(), values);
    }

    public VCardBuilder addCaluri(final String value) {
        properties.add(new VCardProperty("caluri", Maps.newHashMap(), "uri", value));
        return this;
    }

    public VCardBuilder addCaladuri(final String value) {
        properties.add(new VCardProperty("caladuri", Maps.newHashMap(), "uri", value));
        return this;
    }

    public VCardBuilder addCategories(final List values) {
        properties.add(new VCardProperty("categories", Maps.newHashMap(), "text", values));
        return this;
    }

    public VCardBuilder addEmail(final Map parameters, final String value) {
        properties.add(new VCardProperty("email", parameters, "text", value));
        return this;
    }

    public VCardBuilder addEmail(final String value) {
        return addEmail(Maps.newHashMap(), value);
    }

    public VCardBuilder addFn(final String value) {
        properties.add(new VCardProperty("fn", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addGeo(final Map parameters, final String value) {
        properties.add(new VCardProperty("geo", parameters, "uri", value));
        return this;
    }

    public VCardBuilder addGeo(final String value) {
        return addGeo(Maps.newHashMap(), value);
    }

    public VCardBuilder addKind(final String value) {
        properties.add(new VCardProperty("kind", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addLogo(final String value) {
        properties.add(new VCardProperty("logo", Maps.newHashMap(), "uri", value));
        return this;
    }

    public VCardBuilder addLang(final Map parameters, final String value) {
        properties.add(new VCardProperty("lang", parameters, "language-tag", value));
        return this;
    }

    public VCardBuilder addNickname(final String value) {
        properties.add(new VCardProperty("nickname", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addNote(final String value) {
        properties.add(new VCardProperty("note", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addTel(final Map parameters, final String value) {
        final String type = (value.startsWith("tel:")) ? "uri" : "text";
        properties.add(new VCardProperty("tel", parameters, type, value));
        return this;
    }

    public VCardBuilder addTel(final String value) {
        return this.addTel(Maps.newHashMap(), value);
    }

    public VCardBuilder addVersion() {
        properties.add(new VCardProperty("version", Maps.newHashMap(), "text", "4.0"));
        return this;
    }

    public VCardBuilder addAnniversary(final String value) {
        properties.add(new VCardProperty("anniversary", Maps.newHashMap(), "date-and-or-time", value));
        return this;
    }

    public VCardBuilder addBday(final String value) {
        properties.add(new VCardProperty("bday", Maps.newHashMap(), "date-and-or-time", value));
        return this;
    }

    public VCardBuilder addN(final List values) {
        properties.add(new VCardProperty("n", Maps.newHashMap(), "text", values));
        return this;
    }

    public VCardBuilder addGender(final String value) {
        properties.add(new VCardProperty("gender", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addOrg(final String value) {
        properties.add(new VCardProperty("org", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addTitle(final String value) {
        properties.add(new VCardProperty("title", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addRole(final String value) {
        properties.add(new VCardProperty("role", Maps.newHashMap(), "text", value));
        return this;
    }

    public VCardBuilder addKey(final Map parameters, final String value) {
        properties.add(new VCardProperty("key", parameters, "text", value));
        return this;
    }

    public VCardBuilder addTz(final String value) {
        properties.add(new VCardProperty("tz", Maps.newHashMap(), "utc-offset", value));
        return this;
    }

    public VCardBuilder addUrl(final Map parameters, final String value) {
        properties.add(new VCardProperty("url", parameters, "uri", value));
        return this;
    }

    public VCard build() {
        return new VCard(properties);
    }
}