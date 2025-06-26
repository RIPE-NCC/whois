package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeParser.MntRoutesParser;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

@Component
public class ReferencedTypeResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferencedTypeResolver.class);
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    private static final MntRoutesParser MNT_ROUTES_PARSER = new MntRoutesParser();

    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public ReferencedTypeResolver(@Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Nullable
    public String getReferencedType(final AttributeType attributeType, final CIString value) {
        final Set<ObjectType> references = attributeType.getReferences();
        switch (references.size()) {
            case 0:
                if (AttributeType.MEMBERS.equals(attributeType) || AttributeType.MP_MEMBERS.equals(attributeType)) {
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
                if (AttributeType.AUTH.equals(attributeType)) {
                    final String authType = SPACE_SPLITTER.split(value).iterator().next().toUpperCase();
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
                    try {
                        MNT_ROUTES_PARSER.parse(value.toString());
                    } catch (AttributeParseException e) {
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
                                LOGGER.debug("{}: {}", ignored.getClass().getName(), ignored.getMessage());
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
