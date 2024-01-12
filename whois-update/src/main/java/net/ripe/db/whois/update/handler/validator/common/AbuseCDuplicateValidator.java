package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class AbuseCDuplicateValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseCDuplicateValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);

    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public AbuseCDuplicateValidator(final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString abuseC = update.getUpdatedObject().getValueOrNullForAttribute(AttributeType.ABUSE_C);
        if (abuseC == null) {
            return Collections.emptyList();
        }

        final RpslObject orgAbuseCObject = findOrgAbuseC(update.getUpdatedObject());
        if (orgAbuseCObject == null) {
            return Collections.emptyList();
        }

        if (orgAbuseCObject.getValueForAttribute(AttributeType.ABUSE_C).equals(abuseC)) {
            return Arrays.asList(UpdateMessages.duplicateAbuseC(abuseC, orgAbuseCObject.getKey()));
        }

        return Collections.emptyList();
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Nullable
    final RpslObject findOrgAbuseC(final RpslObject updatedObject) {
        if (updatedObject == null) {
            return null;
        }

        final RpslObject orgObject = getOrgObject(updatedObject);
        if ((orgObject != null) &&
                (orgObject.containsAttribute(AttributeType.ABUSE_C))) {
            return orgObject;
        }

        return findOrgAbuseC(getParentObject(updatedObject));
    }


    @Nullable
    final RpslObject getOrgObject(final RpslObject rpslObject) {
        final CIString org = rpslObject.getValueOrNullForAttribute(AttributeType.ORG);
        if (org == null) {
            return null;
        }

        return lookup(ObjectType.ORGANISATION, org);
    }

    @Nullable
    private RpslObject getParentObject(final RpslObject object) {
        final IpEntry ipEntry;

        switch (object.getType()) {
            case INETNUM:
                ipEntry = CollectionHelper.uniqueResult(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(object.getKey())));
                break;
            case INET6NUM:
                ipEntry = CollectionHelper.uniqueResult(ipv6Tree.findFirstLessSpecific(Ipv6Resource.parse(object.getKey())));
                break;
            default:
                return null;
        }

        try {
            return (ipEntry != null) ? rpslObjectDao.getById(ipEntry.getObjectId()) : null;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Nullable
    private RpslObject lookup(final ObjectType objectType, final CIString key) {
        try {
            return rpslObjectDao.getByKeyOrNull(objectType, key);
        } catch (DataAccessException e) {
            return null;
        }
    }

}
