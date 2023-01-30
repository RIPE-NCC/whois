package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AsSetSourceAttributeTransformer implements Transformer{

    private final RpslObjectDao rpslObjectDao;

    @Value("${whois.nonauth.source}")
    private String nonAuthSource;

    public AsSetSourceAttributeTransformer(final RpslObjectDao rpslObjectDao){
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public RpslObject transform(final RpslObject rpslObject, final Update update,
                                final UpdateContext updateContext,
                                final Action action) {
        if (Action.DELETE.equals(action) || !ObjectType.AS_SET.equals(rpslObject.getType())) {
            return rpslObject;
        }
        final String asSetKey = rpslObject.getKey().toString();

        boolean flatAsSet = !asSetKey.contains(":");
        final CIString asSetSource = rpslObject.getValueForAttribute(AttributeType.SOURCE);

        if (flatAsSet){
            if (asSetSource != null) {
                updateContext.addMessage(update, UpdateMessages.flatModelNotAllowSourceModifications(asSetSource.toString()));
            }
            return rpslObject;
        }

        final String autnumKey = asSetKey.substring(0, asSetKey.indexOf(":"));
        final RpslObject autnumObject = rpslObjectDao.getByKeyOrNull(ObjectType.AUT_NUM, autnumKey);

        if (autnumObject == null){
            return rpslObject;
        }

        final CIString autnumSource = autnumObject.getValueForAttribute(AttributeType.SOURCE);
        if (asSetSource.equals(autnumSource)){
            return rpslObject;
        }

        if (nonAuthSource.contentEquals(autnumSource)){
            updateContext.addMessage(update, UpdateMessages.sourceChanged(asSetSource, autnumSource, autnumKey));

            return new RpslObjectBuilder(rpslObject).replaceAttribute(rpslObject.findAttribute(AttributeType.SOURCE),
                    new RpslAttribute(AttributeType.SOURCE, nonAuthSource)).get();
        } else if (nonAuthSource.contentEquals(asSetSource)) {
            updateContext.addMessage(update, UpdateMessages.notValidSource());
            return rpslObject;
        }
        return rpslObject;
    }
}
