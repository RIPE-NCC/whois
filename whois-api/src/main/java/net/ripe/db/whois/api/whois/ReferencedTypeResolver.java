package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.regex.Pattern;

public class ReferencedTypeResolver {

    private static final Pattern MNT_ROUTES_NO_REFERENCE = Pattern.compile("^\\s*(ANY|\\{.*\\})$");

    @Nullable
    public static String getReferencedType(final AttributeType attributeType, final CIString value) {
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
                if (AttributeType.AUTH.equals(attributeType)) {
                    if (value.startsWith(CIString.ciString("MD5-PW"))) {
                        return null;
                    }
                }
                if (AttributeType.MBRS_BY_REF.equals(attributeType)) {
                    if (value.equals(CIString.ciString("ANY"))) {
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
                if (references.contains(ObjectType.PERSON) && references.contains(ObjectType.ROLE)) {
                    return "person-role";
                }

                for (ObjectType objectType : references) {
                    for (AttributeType lookupAttribute : ObjectTemplate.getTemplate(objectType).getLookupAttributes()) {
                        if (lookupAttribute.isValidValue(objectType, value)) {
                            return objectType.getName();
                        }
                    }
                }

                throw new IllegalStateException(
                        String.format("Unable to determine object type for attribute %s: %s", attributeType, value));
        }
    }
}
