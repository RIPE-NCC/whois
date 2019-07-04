package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Component
@Provider
public class RdapRequestTypeConverter implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        if(!rawType.equals(RdapRequestType.class)) {
            return null;
        }

        return (ParamConverter<T>) new ParamConverter<RdapRequestType>() {
                @Override
                public RdapRequestType fromString(String rdapType){
                    return RdapRequestType.valueOf(rdapType.toUpperCase());
                }

                @Override
                public String toString(RdapRequestType rdapType){
                    return rdapType.toString();
                }
            };
    }
}