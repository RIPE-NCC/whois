package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.vcard.*;
import org.codehaus.plexus.util.StringUtils;

import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class VcardObjectHelper {

    public static List<Object> toObjects(Object target) {

        // Introspect the annotation field ordering
        HashMap<String, Integer> order = new HashMap<String, Integer>();
        if (target.getClass().isAnnotationPresent(XmlType.class)) {
            XmlType xmlType = target.getClass().getAnnotation(XmlType.class);
            Integer pos = 0;
            for (String fieldName : xmlType.propOrder()) {
                order.put(fieldName,pos++);
            }

        }

        // Reflect class getters and populate our Object List
        Method[] methods = target.getClass().getMethods();

        Object[] result = new Object[methods.length];
        List<Object> unordered = new ArrayList<Object>();

        for (Method method : methods) {
            Integer pos = null;
            if (!method.getDeclaringClass().equals(Object.class)) {
                if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith("get")) {
                    try {
                        Object o = method.invoke(target, null);

                        // Handle nulls if you like
                        if (o == null) {
                            // Create an empty hashmap for null value
                            if (method.getReturnType().isAssignableFrom(HashMap.class)) {
                                o = method.getReturnType().newInstance();
                            }
                        } else if (o instanceof List) {
                            // Convert any VcardObject list to object arrays
                            List listConversion = new ArrayList();
                            for (Object entry : ((List)o)) {
                                if (entry instanceof VcardObject) {
                                    listConversion.add(toObjects(entry));
                                } else {
                                    listConversion.add(entry);
                                }
                            }
                            o = listConversion;
                        } else if (o instanceof VcardObject) {
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
        List<Object> ret = new ArrayList<Object>(Arrays.asList(result)).subList(0, order.size());
        // Add unordered/no-annotated getters values to the end of our list
        ret.addAll(unordered);
        return ret;
    }


    public static <K, V> HashMap createHashMap(Map.Entry<K, V>... entries) {
        HashMap <K, V> ret = new HashMap <K, V>();
        for (Map.Entry<K, V>entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static class VcardBuilder {
        ObjectFactory vcardObjectFactory = new ObjectFactory();
        Vcard entityVcard = vcardObjectFactory.createVcard();
        HashMap<String, VcardObject> settersMap = new HashMap<String, VcardObject>();

        public VcardBuilder() {
        }

        public VcardBuilder addAdr(HashMap parameters, AdrEntryValueType value) {
            Adr ev = vcardObjectFactory.createAdr();
            ev.setParameters(parameters);
            if (value != null) {
                ev.setValue(value);
            }
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public VcardBuilder setEmail(HashMap parameters, String value) {
            Email ev = vcardObjectFactory.createEmail();
            ev.setParameters(parameters);
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setFn(String value) {
            Fn ev = vcardObjectFactory.createFn();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setGeo(HashMap parameters, String value) {
            Geo ev = vcardObjectFactory.createGeo();
            ev.setParameters(parameters);
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setKind(String value) {
            Kind ev = vcardObjectFactory.createKind();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder addLang(HashMap parameters, String value) {
            Lang ev = vcardObjectFactory.createLang();
            ev.setParameters(parameters);
            ev.setValue(value);
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public VcardBuilder addTel(HashMap parameters, String value) {
            Tel ev = vcardObjectFactory.createTel();
            ev.setParameters(parameters);
            ev.setValue(value);
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public VcardBuilder setVersion() {
            entityVcard.getVcardEntries().add(vcardObjectFactory.createVersion());
            return this;
        }


        // Other possibly useful vcard properties

        public VcardBuilder setAnniversary(String value) {
            Anniversary ev = vcardObjectFactory.createAnniversary();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setBday(String value) {
            Bday ev = vcardObjectFactory.createBday();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setN(NValueType value) {
            N ev = vcardObjectFactory.createN();
            ev.setParameters(new HashMap());
            if (value != null) {
                ev.setValue(value);
            }
            setCheck(ev);
            return this;
        }

        public VcardBuilder setGender(String value) {
            Gender ev = vcardObjectFactory.createGender();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setOrg(String value) {
            Org ev = vcardObjectFactory.createOrg();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setTitle(String value) {
            Title ev = vcardObjectFactory.createTitle();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setRole(String value) {
            Role ev = vcardObjectFactory.createRole();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }


        public VcardBuilder setKey(HashMap parameters, String value) {
            Key ev = vcardObjectFactory.createKey();
            ev.setParameters(parameters);
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setTz(String value) {
            Tz ev = vcardObjectFactory.createTz();
            ev.setParameters(new HashMap());
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public VcardBuilder setUrl(HashMap parameters, String value) {
            Key ev = vcardObjectFactory.createKey();
            ev.setParameters(parameters);
            ev.setValue(value);
            setCheck(ev);
            return this;
        }

        public NValueType createNEntryValueType(String surname, String given, String prefix, String suffix, NValueType.Honorifics honorifics) {
            NValueType ret = vcardObjectFactory.createNValueType();
            ret.setSurname(surname);
            ret.setGiven(given);
            ret.setPrefix(prefix);
            ret.setSuffix(suffix);
            ret.setHonorifics(honorifics);
            return ret;
        }

        public NValueType.Honorifics createNEntryValueHonorifics(String prefix, String suffix) {
            NValueType.Honorifics ret = vcardObjectFactory.createNValueTypeHonorifics();
            ret.setPrefix(prefix);
            ret.setSuffix(suffix);
            return ret;
        }

        public AdrEntryValueType createAdrEntryValueType(String pobox, String ext, String street, String locality, String region, String code, String country) {
            AdrEntryValueType ret = vcardObjectFactory.createAdrEntryValueType();
            ret.setPobox(pobox);
            ret.setExt(ext);
            ret.setStreet(street);
            ret.setLocality(locality);
            ret.setRegion(region);
            ret.setCode(code);
            ret.setCountry(country);
            return ret;
        }

        public Vcard build() {
            return entityVcard;
        }

        private void setCheck(VcardObject ev) {
            if (settersMap.get(ev.getClass().getName()) != null) {
                // Overwrite
                int idx = 0;
                for (VcardObject entry : entityVcard.getVcardEntries()) {
                    if (ev.getClass().getName().equals(entry.getClass().getName())) {
                        entityVcard.getVcardEntries().set(idx, ev);
                        break;
                    }
                }
            } else {
                entityVcard.getVcardEntries().add(ev);
            }

        }

    }
}
