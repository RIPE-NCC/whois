package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.vcard.*;
import org.codehaus.plexus.util.StringUtils;

import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
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

    public static class EntityVcardBuilder {
        ObjectFactory vcardObjectFactory = new ObjectFactory();
        Vcard entityVcard = vcardObjectFactory.createVcard();
        HashMap<String, VcardObject> settersMap = new HashMap<String, VcardObject>();

        public EntityVcardBuilder() {
        }

        public EntityVcardBuilder setVersion() {
            entityVcard.getVcardEntries().add(vcardObjectFactory.createVersion());
            return this;
        }

        public EntityVcardBuilder setFn(String entryValue) {
            Fn ev = vcardObjectFactory.createFn();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setN(NEntryValueType entryValue) {
            N ev = vcardObjectFactory.createN();
            ev.setKeyValues(new HashMap());
            ev.setNEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setBday(String entryValue) {
            Bday ev = vcardObjectFactory.createBday();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setAnniversary(XMLGregorianCalendar entryValue) {
            Anniversary ev = vcardObjectFactory.createAnniversary();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setGender(String entryValue) {
            Gender ev = vcardObjectFactory.createGender();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setKind(String entryValue) {
            Kind ev = vcardObjectFactory.createKind();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder addLang(HashMap keyValues, String entryValue) {
            Lang ev = vcardObjectFactory.createLang();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public EntityVcardBuilder setOrg(HashMap keyValues, String entryValue) {
            Org ev = vcardObjectFactory.createOrg();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setTitle(String entryValue) {
            Title ev = vcardObjectFactory.createTitle();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setRole(String entryValue) {
            Role ev = vcardObjectFactory.createRole();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder addAdr(HashMap keyValues, AdrEntryValueType entryValue) {
            Adr ev = vcardObjectFactory.createAdr();
            ev.setKeyValues(keyValues);
            if (entryValue != null) {
                ev.setEntryValue(entryValue);
            }
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public EntityVcardBuilder addTel(HashMap keyValues, String entryValue) {
            Tel ev = vcardObjectFactory.createTel();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            entityVcard.getVcardEntries().add(ev);
            return this;
        }

        public EntityVcardBuilder setEmail(HashMap keyValues, String entryValue) {
            Email ev = vcardObjectFactory.createEmail();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setGeo(HashMap keyValues, String entryValue) {
            Geo ev = vcardObjectFactory.createGeo();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setKey(HashMap keyValues, String entryValue) {
            Key ev = vcardObjectFactory.createKey();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setTz(String entryValue) {
            Tz ev = vcardObjectFactory.createTz();
            ev.setKeyValues(new HashMap());
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public EntityVcardBuilder setUrl(HashMap keyValues, String entryValue) {
            Key ev = vcardObjectFactory.createKey();
            ev.setKeyValues(keyValues);
            ev.setEntryValue(entryValue);
            setCheck(ev);
            return this;
        }

        public NEntryValueType createNEntryValueType(String n1, String n2, String n3, String n4, List<String> nPost) {
            NEntryValueType ret = vcardObjectFactory.createNEntryValueType();
            ret.setN1(n1);
            ret.setN2(n2);
            ret.setN3(n3);
            ret.setN4(n4);
            ret.getNPost().addAll(nPost);
            return ret;
        }


        public AdrEntryValueType createAdrEntryValueType(String adr1, String adr2, String adr3, String adr4, String adr5, String adr6, String adr7) {
            AdrEntryValueType ret = vcardObjectFactory.createAdrEntryValueType();
            ret.setAdr1(adr1);
            ret.setAdr2(adr2);
            ret.setAdr3(adr3);
            ret.setAdr4(adr4);
            ret.setAdr5(adr5);
            ret.setAdr6(adr6);
            ret.setAdr7(adr7);
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
