package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Do not allow reserved words as object key, or prefixes not of a specified type.
 *
 * According to RFC2622 (section 2)
 *
 * {{
 *       <object-name>
 *       Many objects in RPSL have a name.  An <object-name> is made up of
 *       letters, digits, the character underscore "_", and the character
 *       hyphen "-"; the first character of a name must be a letter, and
 *       the last character of a name must be a letter or a digit.  The
 *       following words are reserved by RPSL, and they can not be used as
 *       names:
 *
 *           any as-any rs-any peeras
 *           and or not
 *           atomic from to at action accept announce except refine
 *           networks into inbound outbound
 * }}
 *
 * Also according to RFC2622:
 *
 * {{
 *       Names starting with certain prefixes are reserved for certain
 *       object types.  Names starting with "as-" are reserved for as set
 *       names.  Names starting with "rs-" are reserved for route set
 *       names.  Names starting with "rtrs-" are reserved for router set
 *       names.  Names starting with "fltr-" are reserved for filter set
 *       names.  Names starting with "prng-" are reserved for peering set
 *       names.
 * }}
 *
 * Ref. https://tools.ietf.org/html/rfc2622
 */
@Component
public class ReservedWordValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final Set<CIString> RESERVED_WORDS = CIString.ciImmutableSet(
        "any", "as-any", "rs-any", "peeras",
                "and", "or", "not",
                "atomic", "from", "to", "at", "action", "accept", "announce", "except", "refine",
                "networks", "into", "inbound", "outbound");

    private static final Map<CIString, ObjectType> RESERVED_PREFIXES = Maps.newHashMap();
    static {
        RESERVED_PREFIXES.put(CIString.ciString("as-"), ObjectType.AS_SET);
        RESERVED_PREFIXES.put(CIString.ciString("rs-"), ObjectType.ROUTE_SET);
        RESERVED_PREFIXES.put(CIString.ciString("rtrs-"), ObjectType.RTR_SET);
        RESERVED_PREFIXES.put(CIString.ciString("fltr-"), ObjectType.FILTER_SET);
        RESERVED_PREFIXES.put(CIString.ciString("prng-"), ObjectType.PEERING_SET);
        RESERVED_PREFIXES.put(CIString.ciString("org-"), ObjectType.ORGANISATION);
        RESERVED_PREFIXES.put(CIString.ciString("irt-"), ObjectType.IRT);
        RESERVED_PREFIXES.put(CIString.ciString("pgpkey-"), ObjectType.KEY_CERT);
        RESERVED_PREFIXES.put(CIString.ciString("x509-"), ObjectType.KEY_CERT);
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (updatedObject == null) {
            return Collections.emptyList();
        }

        final CIString primaryKey = updatedObject.getKey();

        if (RESERVED_WORDS.contains(primaryKey)) {
            return Arrays.asList(UpdateMessages.reservedNameUsed(primaryKey.toLowerCase()));
        }

        for (Map.Entry<CIString, ObjectType> entry : RESERVED_PREFIXES.entrySet()) {
            if (primaryKey.startsWith(entry.getKey()) && (!updatedObject.getType().equals(entry.getValue()))) {
                return Arrays.asList(UpdateMessages.reservedPrefixUsed(entry.getKey(), entry.getValue()));
            }
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
}
