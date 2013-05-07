package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

abstract class AbstractObjectIsMaintainedValidator implements BusinessRuleValidator {
    protected final RpslObjectDao rpslObjectDao;

    public AbstractObjectIsMaintainedValidator(RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    protected List<RpslObject> validateReferencedPersonsAndRoles(final RpslObject rpslObject) {
        final List<RpslObject> result = Lists.newArrayList();

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ADMIN_C, AttributeType.TECH_C)) {
            for (final CIString value : attribute.getCleanValues()) {
                try {
                    final RpslObject object = getPersonOrRoleByKey(value.toString());
                    if (!object.containsAttribute(AttributeType.MNT_BY)) {
                        result.add(object);
                    }
                } catch (EmptyResultDataAccessException ignored) {
                }
            }
        }

        return result;
    }

    private RpslObject getPersonOrRoleByKey(final String key) {
        try {
            return rpslObjectDao.getByKey(ObjectType.PERSON, key);
        } catch (EmptyResultDataAccessException ignored) {
            return rpslObjectDao.getByKey(ObjectType.ROLE, key);
        }
    }
}
