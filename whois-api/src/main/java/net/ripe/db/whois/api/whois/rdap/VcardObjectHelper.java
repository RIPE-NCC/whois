package net.ripe.db.whois.api.whois.rdap;

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

                        // Create an empty hashmap for null value
                        if (o == null && method.getReturnType().isAssignableFrom(HashMap.class)) {
                            o =  method.getReturnType().newInstance();
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
        // Add unordered values to the end of our list
        List<Object> ret = new ArrayList<Object>(Arrays.asList(result)).subList(0, order.size());
        ret.removeAll(Collections.singleton(null));
        ret.addAll(unordered);
        return ret;
    }

}
