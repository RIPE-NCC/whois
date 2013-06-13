package net.ripe.db.whois.query.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.domain.QueryMessages;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

class CombinationValidator implements QueryValidator {
    private static final Map<QueryFlag, List<QueryFlag>> INVALID_COMBINATIONS = Maps.newLinkedHashMap();

    static {
        INVALID_COMBINATIONS.put(QueryFlag.ABUSE_CONTACT, Lists.newArrayList(QueryFlag.BRIEF, QueryFlag.NO_FILTERING, QueryFlag.NO_REFERENCED, QueryFlag.PRIMARY_KEYS));
        INVALID_COMBINATIONS.put(QueryFlag.SHOW_TAG_INFO, Lists.newArrayList(QueryFlag.NO_TAG_INFO));
        INVALID_COMBINATIONS.put(QueryFlag.RESOURCE, Lists.newArrayList(QueryFlag.SOURCES, QueryFlag.ALL_SOURCES, QueryFlag.INVERSE));

        final Map<QueryFlag, List<QueryFlag>> limitedCombinations = Maps.newHashMap();
        limitedCombinations.put(QueryFlag.LIST_VERSIONS, Lists.newArrayList(QueryFlag.PERSISTENT_CONNECTION, QueryFlag.CLIENT));
        limitedCombinations.put(QueryFlag.DIFF_VERSIONS, Lists.newArrayList(QueryFlag.PERSISTENT_CONNECTION, QueryFlag.CLIENT));
        limitedCombinations.put(QueryFlag.SHOW_VERSION, Lists.newArrayList(QueryFlag.PERSISTENT_CONNECTION, QueryFlag.CLIENT, QueryFlag.NO_FILTERING));

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
