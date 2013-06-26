package net.ripe.db.whois.api.whois.rdap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VcardObject {

    public List<Object> toObjects() {

        List<Object> ret = new ArrayList<Object>();
        // Reflect class getters and populate our Object List
        Method[] methods = getClass().getMethods();

        for (Method method : methods) {
            if (!method.getDeclaringClass().equals(Object.class)) {
                if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith("get")) {
                    try {
                        Object o = method.invoke(this, null);

                        // Create an empty hashmap for null value
                        if (o == null && method.getReturnType().isAssignableFrom(HashMap.class)) {
                            o =  method.getReturnType().newInstance();
                        }
                        ret.add(o);
                    } catch (Exception ex) {
                        // @TODO error log this
                        ret.add(null);
                    }
                }
            }
        }
        return ret;
    }

}
