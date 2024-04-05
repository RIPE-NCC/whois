package net.ripe.db.whois.api.rest.search;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.api.rest.domain.ObjectMessage;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

public interface QueryMessageGenerator {
    default ObjectMessage generate(final RpslObject rpslObject, final Parameters parameters){
        if (!this.getTypes().contains(rpslObject.getType())){
            return null;
        }
        return proceed(rpslObject, parameters);
    };

    ObjectMessage proceed(final RpslObject rpslObject, final Parameters parameters);
    ImmutableList<ObjectType> getTypes();


}
