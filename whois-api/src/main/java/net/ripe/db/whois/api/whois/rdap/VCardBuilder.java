package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VCardBuilder {

    private final List<VCardProperty> properties = new ArrayList();

    public VCardBuilder addAdr(final Map parameters, final AdrEntryValueType value) {
        final Adr adr = new Adr();
        adr.setParameters(parameters);
        if (value != null) {
            adr.setValue(value);
        }
        properties.add(adr);
        return this;
    }

    public VCardBuilder addAdr(final AdrEntryValueType value) {
        return addAdr(Maps.newHashMap(), value);
    }

    public VCardBuilder addEmail(final Map parameters, final String value) {
        final Email email = new Email();
        email.setName("email");
        email.setParameters(parameters);
        email.setValue(value);
        email.setType("text");
        properties.add(email);
        return this;
    }

    public VCardBuilder addEmail(final String value) {
        return addEmail(Maps.newHashMap(), value);
    }

    public VCardBuilder addFn(final String value) {
        final Fn fn = new Fn();
        fn.setParameters(Maps.newHashMap());
        fn.setValue(value);
        properties.add(fn);
        return this;
    }

    public VCardBuilder addGeo(final Map parameters, final String value) {
        final Geo geo = new Geo();
        geo.setParameters(parameters);
        geo.setValue(value);
        properties.add(geo);
        return this;
    }

    public VCardBuilder addKind(final String value) {
        final Kind kind = new Kind();
        kind.setParameters(Maps.newHashMap());
        kind.setValue(value);
        properties.add(kind);
        return this;
    }

    public VCardBuilder addLang(final Map parameters, final String value) {
        final Lang lang = new Lang();
        lang.setParameters(parameters);
        lang.setValue(value);
        properties.add(lang);
        return this;
    }

    public VCardBuilder addTel(final Map parameters, final String value) {
        final Tel tel = new Tel();
        tel.setParameters(parameters);
        tel.setValue(value);
        properties.add(tel);
        return this;
    }

    public VCardBuilder addTel(final String value) {
        return this.addTel(Maps.newHashMap(), value);
    }

    public VCardBuilder addVersion() {
        final Version version = new Version();
        version.setParameters(Maps.newHashMap());
        properties.add(version);
        return this;
    }

    // Other possibly useful vcard properties

    public VCardBuilder addAnniversary(final String value) {
        final Anniversary anniversary = new Anniversary();
        anniversary.setParameters(Maps.newHashMap());
        anniversary.setValue(value);
        properties.add(anniversary);
        return this;
    }

    public VCardBuilder addBday(final String value) {
        final Bday birthDay = new Bday();
        birthDay.setParameters(Maps.newHashMap());
        birthDay.setValue(value);
        properties.add(birthDay);
        return this;
    }

    public VCardBuilder addN(final NValueType value) {
        final N n = new N();
        n.setParameters(Maps.newHashMap());
        if (value != null) {
            n.setValue(value);
        }
        properties.add(n);
        return this;
    }

    public VCardBuilder addGender(final String value) {
        final Gender gender = new Gender();
        gender.setParameters(Maps.newHashMap());
        gender.setValue(value);
        properties.add(gender);
        return this;
    }

    public VCardBuilder addOrg(final String value) {
        final Org org = new Org();
        org.setParameters(Maps.newHashMap());
        org.setValue(value);
        properties.add(org);
        return this;
    }

    public VCardBuilder addTitle(final String value) {
        final Title title = new Title();
        title.setParameters(Maps.newHashMap());
        title.setValue(value);
        properties.add(title);
        return this;
    }

    public VCardBuilder addRole(final String value) {
        final Role role = new Role();
        role.setParameters(Maps.newHashMap());
        role.setValue(value);
        properties.add(role);
        return this;
    }

    public VCardBuilder addKey(final Map parameters, final String value) {
        final Key key = new Key();
        key.setParameters(parameters);
        key.setValue(value);
        properties.add(key);
        return this;
    }

    public VCardBuilder addTz(final String value) {
        final Tz tz = new Tz();
        tz.setParameters(Maps.newHashMap());
        tz.setValue(value);
        properties.add(tz);
        return this;
    }

    public VCardBuilder addUrl(final Map parameters, final String value) {
        final Key key = new Key();
        key.setParameters(parameters);
        key.setValue(value);
        properties.add(key);
        return this;
    }

    // TODO: refactor
    public NValueType createNEntryValueType(final String surname, final String given, final String prefix, final String suffix, final NValueType.Honorifics honorifics) {
        final NValueType ret = new NValueType();
        ret.setSurname(surname);
        ret.setGiven(given);
        ret.setPrefix(prefix);
        ret.setSuffix(suffix);
        ret.setHonorifics(honorifics);
        return ret;
    }

    // TODO: refactor
    public NValueType.Honorifics createNEntryValueHonorifics(final String prefix, final String suffix) {
        final NValueType.Honorifics honorifics = new NValueType.Honorifics();
        honorifics.setPrefix(prefix);
        honorifics.setSuffix(suffix);
        return honorifics;
    }

    // TODO: refactor
    public AdrEntryValueType createAdrEntryValueType(final String pobox, final String ext, final String street, final String locality, final String region, final String code, final String country) {
        final AdrEntryValueType adressEntry = new AdrEntryValueType();
        adressEntry.setPobox(pobox);
        adressEntry.setExt(ext);
        adressEntry.setStreet(street);
        adressEntry.setLocality(locality);
        adressEntry.setRegion(region);
        adressEntry.setCode(code);
        adressEntry.setCountry(country);
        return adressEntry;
    }

    public List<Object> build() {
        VCard entityVcard = new VCard("vcard", properties);
        return VCardObjectHelper.toObjects(entityVcard);
    }
}