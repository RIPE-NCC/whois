package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InetnumLegacyStatusGenerator extends AttributeGenerator {
    private static final InetnumStatus LEGACY_STATUS = InetnumStatus.LEGACY;
    private final Ipv4Tree ipv4Tree;
    private final RpslObjectDao objectDao;

    @Autowired
    public InetnumLegacyStatusGenerator(final Ipv4Tree ipv4Tree, final RpslObjectDao objectDao) {
        this.ipv4Tree = ipv4Tree;
        this.objectDao = objectDao;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        if (updateContext.getAction(update) != Action.DELETE && updatedObject.getType() == ObjectType.INETNUM) {
            final CIString updatedStatusValue = updatedObject.getValueOrNullForAttribute(AttributeType.STATUS);
            final List<Ipv4Entry> firstLessSpecific = ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(updatedObject.getKey()));
            if (firstLessSpecific.size() == 1) {
                final RpslObject parent = objectDao.getByKey(ObjectType.INETNUM, firstLessSpecific.get(0).getKey().toRangeString());

                final CIString parentStatus = parent.getValueOrNullForAttribute(AttributeType.STATUS);
                if (parentStatus != null && InetnumStatus.getStatusFor(parentStatus) == LEGACY_STATUS) {
                    final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);
                    builder.removeAttributeType(AttributeType.STATUS);
                    builder.addAttributeSorted(new RpslAttribute(AttributeType.STATUS, LEGACY_STATUS.toString()));

                    if (InetnumStatus.getStatusFor(updatedStatusValue) != LEGACY_STATUS) {
                        updateContext.addMessage(update, ValidationMessages.attributeValueConverted(updatedStatusValue, LEGACY_STATUS.toString()));
                    }
                    return builder.get();
                }
            }
        }
        return updatedObject;
    }
}
