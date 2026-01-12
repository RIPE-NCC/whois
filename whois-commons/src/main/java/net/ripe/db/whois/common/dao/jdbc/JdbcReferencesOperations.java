package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class JdbcReferencesOperations {


    public static boolean isReferenced(final JdbcTemplate jdbcTemplate, final RpslObject object) {
        for (final RpslAttribute attribute : object.findAttributes(ObjectTemplate.getTemplate(object.getType()).getKeyAttributes())) {
            for (final IndexStrategy indexStrategy : IndexStrategies.getReferencing(object.getType())) {
                for (final CIString value : attribute.getReferenceValues()) {
                    for (final RpslObjectInfo result : indexStrategy.findInIndex(jdbcTemplate, value)) {
                        if (object.getKey().equals(ciString(result.getKey())) && result.getObjectType().equals(object.getType())) {
                            continue;
                        }

                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Set<RpslObjectInfo> getReferences(final JdbcTemplate jdbcTemplate, final RpslObject object) {
        Set<RpslObjectInfo> references = Sets.newHashSet();
        final List<IndexStrategy> indexStrategies = IndexStrategies.getReferencing(object.getType());

        // for route(6), individually check each key
        for (final RpslAttribute keyAttr : object.findAttributes(ObjectTemplate.getTemplate(object.getType()).getKeyAttributes())) {
            for (final IndexStrategy indexStrategy : indexStrategies) {
                for (final CIString value : keyAttr.getReferenceValues()) {
                    final List<RpslObjectInfo> results = indexStrategy.findInIndex(jdbcTemplate, value);
                    for (final RpslObjectInfo result : results) {
                        if (object.getKey().equals(result.getKey()) && result.getObjectType().equals(object.getType())) {
                            continue;
                        }

                        references.add(result);
                    }
                }
            }
        }

        return references;
    }


    public static Map<RpslAttribute, Set<CIString>> getInvalidReferences(final JdbcTemplate jdbcTemplate, final RpslObject object) {
        final Map<RpslAttribute, Set<CIString>> invalidReferenceMap = Maps.newHashMap();

        for (final RpslAttribute attribute : object.getAttributes()) {
            final Set<CIString> invalidReferenceValues = getInvalidReferences(jdbcTemplate, object, attribute);
            if (!invalidReferenceValues.isEmpty()) {
                invalidReferenceMap.put(attribute, invalidReferenceValues);
            }
        }

        return invalidReferenceMap;
    }

    private static Set<CIString> getInvalidReferences(final JdbcTemplate jdbcTemplate, final RpslObject object, final RpslAttribute attribute) {
        AttributeType attributeType = attribute.getType();
        if (attributeType != null && attributeType.getReferences().isEmpty()) {
            return Collections.emptySet();
        }

        final Set<CIString> invalidReferences = Sets.newLinkedHashSet();
        for (final CIString value : attribute.getReferenceValues()) {
            if (isInvalidReference(jdbcTemplate, object, attributeType, value)) {
                invalidReferences.add(value);
            }
        }

        return invalidReferences;
    }

    private static boolean isInvalidReference(final JdbcTemplate jdbcTemplate, final RpslObject object, final AttributeType attributeType, final CIString referenceValue) {
        final Set<ObjectType> references = attributeType.getReferences(referenceValue);
        if (references.isEmpty()) {
            return false;
        }

        for (final ObjectType reference : references) {
            if (reference.equals(object.getType()) && object.getKey().equals(referenceValue)) {
                return false;
            }

            if (getAttributeReference(jdbcTemplate, reference, referenceValue) != null) {
                return false;
            }
        }

        return true;
    }

    @CheckForNull
    public static RpslObjectInfo getAttributeReference(final JdbcTemplate jdbcTemplate, final AttributeType attributeType, final CIString value) {
        final CIString referenceValue = new RpslAttribute(attributeType, value.toString()).getReferenceValue();
        for (final ObjectType objectType : attributeType.getReferences()) {
            final RpslObjectInfo result = getAttributeReference(jdbcTemplate, objectType, referenceValue);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static RpslObjectInfo getAttributeReference(final JdbcTemplate jdbcTemplate, final ObjectType objectType, final CIString keyValue) {
        final ObjectTemplate referenceTemplate = ObjectTemplate.getTemplate(objectType);
        final Set<AttributeType> referenceKeyAttributes = referenceTemplate.getKeyAttributes();
        Validate.isTrue(referenceKeyAttributes.size() == 1, "We can never have a reference to a composed key");
        final IndexStrategy indexStrategy = IndexStrategies.get(referenceKeyAttributes.iterator().next());
        final List<RpslObjectInfo> result = indexStrategy.findInIndex(jdbcTemplate, keyValue);
        return CollectionHelper.uniqueResult(result);
    }
}
