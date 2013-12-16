package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class ReferencedTypeResolver {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    private static final Pattern MNT_ROUTES_NO_REFERENCE = Pattern.compile("(?i)^\\s*(ANY|\\{.*\\})$");

    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public ReferencedTypeResolver(final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Nullable
    public String getReferencedType(final AttributeType attributeType, final CIString value) {
        final Set<ObjectType> references = attributeType.getReferences();
        switch (references.size()) {
            case 0:
                if (AttributeType.MEMBERS.equals(attributeType)) {
                    if (AttributeType.AUT_NUM.isValidValue(ObjectType.AUT_NUM, value)) {
                        return ObjectType.AUT_NUM.getName();
                    }

                    if (AttributeType.AS_SET.isValidValue(ObjectType.AS_SET, value)) {
                        return ObjectType.AS_SET.getName();
                    }

                    if (AttributeType.ROUTE_SET.isValidValue(ObjectType.ROUTE_SET, value)) {
                        return ObjectType.ROUTE_SET.getName();
                    }

                    if (AttributeType.RTR_SET.isValidValue(ObjectType.RTR_SET, value)) {
                        return ObjectType.RTR_SET.getName();
                    }
                }
                return null;

            case 1:
                // TODO: [AH] this is duplicate implementation for FilterAuthFunction
                if (AttributeType.AUTH.equals(attributeType)) {
                    String authType = SPACE_SPLITTER.split(value).iterator().next().toUpperCase();
                    if (authType.endsWith("-PW") || authType.equals("SSO")) {
                        return null;
                    }
                }
                if (AttributeType.MBRS_BY_REF.equals(attributeType)) {
                    if (value.toLowerCase().equals("any")) {
                        return null;
                    }
                }
                if (AttributeType.MNT_ROUTES.equals(attributeType)) {
                    if (MNT_ROUTES_NO_REFERENCE.matcher(value).matches()) {
                        return null;
                    }
                }

                return references.iterator().next().getName();

            default:
                if (references.contains(ObjectType.PERSON) || references.contains(ObjectType.ROLE)) {
                    for (ObjectType objectType : references) {
                        if (attributeType.isValidValue(objectType, value)) {
                            try {
                                // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
                                return rpslObjectDao.findByKey(objectType, value.toString()).getObjectType().getName();
                            } catch (EmptyResultDataAccessException ignored) {
                            }
                        }
                    }
                } else {
                    for (ObjectType objectType : references) {
                        for (AttributeType lookupAttribute : ObjectTemplate.getTemplate(objectType).getLookupAttributes()) {
                            if (lookupAttribute.isValidValue(objectType, value)) {
                                return objectType.getName();
                            }
                        }
                    }
                }

                return null;
        }
    }
}
