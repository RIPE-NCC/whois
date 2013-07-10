package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.*;
import org.codehaus.plexus.util.StringUtils;

import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VcardObjectHelper {

    public static List<Object> toObjects(final Object target) {

        // Introspect the annotation field ordering
        Map<String, Integer> order = Maps.newHashMap();
        if (target.getClass().isAnnotationPresent(XmlType.class)) {
            XmlType xmlType = target.getClass().getAnnotation(XmlType.class);
            Integer pos = 0;
            for (String fieldName : xmlType.propOrder()) {
                order.put(fieldName, pos++);
            }
        }

        // Reflect class getters and populate our Object List
        Method[] methods = target.getClass().getMethods();

        Object[] result = new Object[methods.length];
        List<Object> unordered = new ArrayList<>();

        for (Method method : methods) {
            Integer pos = null;
            if (!method.getDeclaringClass().equals(Object.class)) {
                if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith("get")) {
                    try {
                        Object o = method.invoke(target, null);

                        // TODO: Handle nulls if you like
                        if (o == null) {
                            // Create an empty Map for null value
                            if (method.getReturnType().isAssignableFrom(Map.class)) {
                                o = method.getReturnType().newInstance();
                            }
                        } else if (o instanceof List) {
                            // Convert any VcardObject list to object arrays
                            List listConversion = new ArrayList();
                            for (Object entry : ((List)o)) {
                                if (entry instanceof VCardProperty) {
                                    listConversion.add(toObjects(entry));
                                } else {
                                    listConversion.add(entry);
                                }
                            }
                            o = listConversion;
                        } else if (o instanceof VCardProperty) {
                            // Convert any VcardObject to object array
                            o = toObjects(o);
                        }

                        // Get the order position
                        pos = order.get(StringUtils.lowercaseFirstLetter(method.getName().substring(3)));
                        if (pos != null) {
                            result[pos] = o;
                        } else {
                            unordered.add(o);
                        }
                    } catch (Exception ex) {
                        // @TODO error log this
                        ex.printStackTrace();
                        if (pos != null) {
                            result[pos] = "?";
                        } else {
                            unordered.add("?");
                        }
                    }
                }
            }
        }

        List<Object> ret = new ArrayList(Arrays.asList(result)).subList(0, order.size());
        // Add unordered/no-annotated getters values to the end of our list
        ret.addAll(unordered);
        return ret;
    }

    public static <K, V> Map createMap(final Map.Entry<K, V>... entries) {
        final Map<K, V> ret = Maps.newHashMap();
        for (Map.Entry<K, V> entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static class VcardBuilder {
        final Vcard entityVcard = new Vcard();
        final Map<String, VcardObject> settersMap = Maps.newHashMap();

        public boolean isEmpty() {
            boolean ret = false;
            if (entityVcard.getVcardEntries().size() == 0) {
                ret = true;
            }

            if (entityVcard.getVcardEntries().size() == 1) {
                if (entityVcard.getVcardEntries().get(0).getClass().getName().equals(Version.class.getName())) {        // TODO
                    ret = true;
                }
            }
            return ret;
        }

        public VcardBuilder addAdr(final Map parameters, final AdrEntryValueType value) {
            final Adr adr = new Adr();
            adr.setParameters(parameters);
            if (value != null) {
                adr.setValue(value);
            }
            entityVcard.getVcardEntries().add(adr);
            return this;
        }

        public VcardBuilder addAdr(final AdrEntryValueType value) {
            return addAdr(Maps.newHashMap(), value);
        }

        public VcardBuilder setEmail(final Map parameters, final String value) {
            final Email email = new Email();
            email.setParameters(parameters);
            email.setValue(value);
            setCheck(email);
            return this;
        }

        public VcardBuilder setEmail(final String value) {
            return setEmail(Maps.newHashMap(), value);
        }

        public VcardBuilder setFn(final String value) {
            final Fn fn = new Fn();
            fn.setParameters(Maps.newHashMap());
            fn.setValue(value);
            setCheck(fn);
            return this;
        }

        public VcardBuilder setGeo(final Map parameters, final String value) {
            final Geo geo = new Geo();
            geo.setParameters(parameters);
            geo.setValue(value);
            setCheck(geo);
            return this;
        }

        public VcardBuilder setKind(final String value) {
            final Kind kind = new Kind();
            kind.setParameters(Maps.newHashMap());
            kind.setValue(value);
            setCheck(kind);
            return this;
        }

        public VcardBuilder addLang(final Map parameters, final String value) {
            final Lang lang = new Lang();
            lang.setParameters(parameters);
            lang.setValue(value);
            entityVcard.getVcardEntries().add(lang);
            return this;
        }

        public VcardBuilder addTel(final Map parameters, final String value) {
            final Tel tel = new Tel();
            tel.setParameters(parameters);
            tel.setValue(value);
            entityVcard.getVcardEntries().add(tel);
            return this;
        }

        public VcardBuilder addTel(final String value) {
            return addTel(Maps.newHashMap(), value);
        }


        public VcardBuilder setVersion() {
            final Version version = new Version();
            version.setParameters(Maps.newHashMap());
            setCheck(version);
            return this;
        }

        // Other possibly useful vcard properties

        public VcardBuilder setAnniversary(final String value) {
            final Anniversary anniversary = new Anniversary();
            anniversary.setParameters(Maps.newHashMap());
            anniversary.setValue(value);
            setCheck(anniversary);
            return this;
        }

        public VcardBuilder setBday(final String value) {
            final Bday birthDay = new Bday();
            birthDay.setParameters(Maps.newHashMap());
            birthDay.setValue(value);
            setCheck(birthDay);
            return this;
        }

        public VcardBuilder setN(final NValueType value) {
            final N n = new N();
            n.setParameters(Maps.newHashMap());
            if (value != null) {
                n.setValue(value);
            }
            setCheck(n);
            return this;
        }

        public VcardBuilder setGender(final String value) {
            final Gender gender = new Gender();
            gender.setParameters(Maps.newHashMap());
            gender.setValue(value);
            setCheck(gender);
            return this;
        }

        public VcardBuilder setOrg(final String value) {
            final Org org = new Org();
            org.setParameters(Maps.newHashMap());
            org.setValue(value);
            setCheck(org);
            return this;
        }

        public VcardBuilder setTitle(final String value) {
            final Title title = new Title();
            title.setParameters(Maps.newHashMap());
            title.setValue(value);
            setCheck(title);
            return this;
        }

        public VcardBuilder setRole(final String value) {
            final Role role = new Role();
            role.setParameters(Maps.newHashMap());
            role.setValue(value);
            setCheck(role);
            return this;
        }

        public VcardBuilder setKey(final Map parameters, final String value) {
            final Key key = new Key();
            key.setParameters(parameters);
            key.setValue(value);
            setCheck(key);
            return this;
        }

        public VcardBuilder setTz(final String value) {
            final Tz tz = new Tz();
            tz.setParameters(Maps.newHashMap());
            tz.setValue(value);
            setCheck(tz);
            return this;
        }

        public VcardBuilder setUrl(final Map parameters, final String value) {
            final Key key = new Key();
            key.setParameters(parameters);
            key.setValue(value);
            setCheck(key);
            return this;
        }

        public NValueType createNEntryValueType(final String surname, final String given, final String prefix, final String suffix, final NValueType.Honorifics honorifics) {
            final NValueType ret = new NValueType();
            ret.setSurname(surname);
            ret.setGiven(given);
            ret.setPrefix(prefix);
            ret.setSuffix(suffix);
            ret.setHonorifics(honorifics);
            return ret;
        }

        public NValueType.Honorifics createNEntryValueHonorifics(final String prefix, final String suffix) {
            final NValueType.Honorifics honorifics = new NValueType.Honorifics();
            honorifics.setPrefix(prefix);
            honorifics.setSuffix(suffix);
            return honorifics;
        }

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
            return toObjects(entityVcard);
        }

        private void setCheck(final VcardObject vcardObject) {
            if (settersMap.get(vcardObject.getClass().getName()) != null) {
                // Overwrite
                int idx = 0;
                for (final VcardObject entry : entityVcard.getVcardEntries()) {
                    if (vcardObject.getClass().getName().equals(entry.getClass().getName())) {
                        entityVcard.getVcardEntries().set(idx, vcardObject);
                        break;
                    }
                }
            } else {
                entityVcard.getVcardEntries().add(vcardObject);
            }
        }
    }
}
