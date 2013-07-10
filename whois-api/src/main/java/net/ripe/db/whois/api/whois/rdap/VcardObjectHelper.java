package net.ripe.db.whois.api.whois.rdap;

import org.codehaus.plexus.util.StringUtils;

import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VcardObjectHelper {

    public static List<Object> toObjects(final Object target) {

        // Introspect the annotation field ordering
        HashMap<String, Integer> order = new HashMap<String, Integer>();
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

}
