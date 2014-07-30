package net.ripe.db.whois.query.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.query.QueryFlag.*;

class CombinationValidator implements QueryValidator {
    private static final Map<QueryFlag, List<QueryFlag>> INVALID_COMBINATIONS = Maps.newLinkedHashMap();

    static {
        INVALID_COMBINATIONS.put(ABUSE_CONTACT, Lists.newArrayList(BRIEF, NO_FILTERING, NO_REFERENCED, PRIMARY_KEYS));
        INVALID_COMBINATIONS.put(SHOW_TAG_INFO, Lists.newArrayList(NO_TAG_INFO));
        INVALID_COMBINATIONS.put(RESOURCE, Lists.newArrayList(SOURCES, ALL_SOURCES, INVERSE));
        INVALID_COMBINATIONS.put(VALID_SYNTAX, Lists.newArrayList(NO_VALID_SYNTAX, LIST_VERSIONS, SHOW_VERSION, DIFF_VERSIONS));

        final Map<QueryFlag, List<QueryFlag>> limitedCombinations = Maps.newHashMap();
        limitedCombinations.put(LIST_VERSIONS, Lists.newArrayList(SELECT_TYPES, PERSISTENT_CONNECTION, CLIENT));
        limitedCombinations.put(DIFF_VERSIONS, Lists.newArrayList(SELECT_TYPES, PERSISTENT_CONNECTION, CLIENT));
        limitedCombinations.put(SHOW_VERSION, Lists.newArrayList(SELECT_TYPES, PERSISTENT_CONNECTION, CLIENT));
        limitedCombinations.put(SHOW_INTERNAL_VERSION, Lists.newArrayList(SELECT_TYPES, PERSISTENT_CONNECTION, CLIENT));

        for (Map.Entry<QueryFlag, List<QueryFlag>> limitedCombinationEntry : limitedCombinations.entrySet()) {
            final QueryFlag queryFlag = limitedCombinationEntry.getKey();
            final List<QueryFlag> validFlags = limitedCombinationEntry.getValue();
            final List<QueryFlag> invalidFlags = Lists.newArrayListWithExpectedSize(QueryFlag.values().length - validFlags.size());
            for (final QueryFlag flag : QueryFlag.values()) {
                if (flag != queryFlag && !validFlags.contains(flag)) {
                    invalidFlags.add(flag);
                }
            }

            INVALID_COMBINATIONS.put(queryFlag, invalidFlags);
        }
    }

    @Override
    public void validate(final Query query, final Messages messages) {
        final Set<BigInteger> combinationMasks = Sets.newHashSet();

        for (final Map.Entry<QueryFlag, List<QueryFlag>> combinationEntry : INVALID_COMBINATIONS.entrySet()) {
            final QueryFlag option = combinationEntry.getKey();
            if (query.hasOption(option)) {
                for (final QueryFlag otherOption : combinationEntry.getValue()) {
                    if (query.hasOption(otherOption)) {
                        if (combinationMasks.add(BigInteger.ONE.shiftLeft(option.ordinal()).and(BigInteger.ONE.shiftLeft(otherOption.ordinal())))) {
                            messages.add(QueryMessages.invalidCombinationOfFlags(option.toString(), otherOption.toString()));
                        }
                    }
                }
            }
        }
    }
}
