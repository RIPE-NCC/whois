package net.ripe.db.whois.api.rdap;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Component
@Provider
public class RdapRelationTypeConverter implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        if(!rawType.equals(RelationType.class)) {
            return null;
        }

        return (ParamConverter<T>) new ParamConverter<RelationType>() {
            @Override
            public RelationType fromString(String relationType){
                return RelationType.valueOf(relationType.toUpperCase());
            }

            @Override
            public String toString(RelationType relationType){
                return relationType.toString();
            }
        };
    }
}
