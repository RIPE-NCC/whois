package net.ripe.db.whois.update.handler.transform;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class AsSetAttributeTransformer implements Transformer{

    private final RpslObjectDao rpslObjectDao;

    @Value("${whois.nonauth.source}")
    private String nonAuthRipeSource;

    public AsSetAttributeTransformer(final RpslObjectDao rpslObjectDao){
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public RpslObject transform(RpslObject rpslObject, Update update, UpdateContext updateContext, Action action) {
        String asSetKey = rpslObject.getKey().toString();
        if (asSetKey.contains(":")){
            String autnumKey = asSetKey.substring(0, asSetKey.indexOf(":"));
            RpslObject autnumObject = rpslObjectDao.getByKeyOrNull(ObjectType.AUT_NUM, autnumKey);

            if (autnumObject != null && nonAuthRipeSource.equals(Objects.requireNonNull(autnumObject.getValueOrNullForAttribute(AttributeType.SOURCE)).toString())){
                final Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
                replace.put(rpslObject.findAttribute(AttributeType.SOURCE), new RpslAttribute(AttributeType.SOURCE, nonAuthRipeSource));

                RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
                builder.replaceAttributes(replace);
                return builder.get();
            }
        }
        return rpslObject;
    }
}
