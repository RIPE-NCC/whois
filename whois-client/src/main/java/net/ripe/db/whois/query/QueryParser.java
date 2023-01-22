package net.ripe.db.whois.query;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.ripe.db.whois.common.IllegalArgumentExceptionMessage;
import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class QueryParser {
    public static final Pattern FLAG_PATTERN = Pattern.compile("(--?)([^-].*)");
    private static final Joiner SPACE_JOINER = Joiner.on(' ');
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings();
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings();
    protected static final int MAX_QUERY_ARGUMENTS = 61;

    private static final QueryFlagParser PARSER = new QueryFlagParser();

    private final String originalStringQuery;
    private final String searchKey;
    private final OptionSet options;

    private final boolean hasSubstitutions;

    public QueryParser(final String query) {
        originalStringQuery = query;

        final String substituted = Latin1Conversion.convertString(query);
        hasSubstitutions = !substituted.equals(query);

        options = PARSER.parse(Iterables.toArray(SPACE_SPLITTER.split(substituted), String.class));


        final List<?> searchKeys = options.nonOptionArguments();
        if (searchKeys.size() >= MAX_QUERY_ARGUMENTS) {
            throw new IllegalArgumentExceptionMessage(QueryMessages.tooManyArguments());
        }
        searchKey = SPACE_JOINER.join(searchKeys);
    }

    public String getSearchKey() {
        return searchKey;
    }

    public boolean hasOptions() {
        return options.hasOptions();
    }

    public boolean hasOption(final QueryFlag queryFlag) {
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                return true;
            }
        }

        return false;
    }

    // TODO: for Integers, this results in conversion String -> Integer -> String -> Integer
    private Collection<?> getOptionValue(final String flag) {
        try {
            return options.valuesOf(flag);
        } catch (OptionException e) {   // Undocumented; thrown on integer conversion failure
            throw new IllegalArgumentExceptionMessage(QueryMessages.malformedQuery());
        }
    }

    public String getOptionValue(final QueryFlag queryFlag) {
        String optionValue = null;
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : getOptionValue(flag)) {
                    if (optionValue == null) {
                        optionValue = optionArgument.toString();
                    } else {
                        throw new IllegalArgumentExceptionMessage(QueryMessages.invalidMultipleFlags((flag.length() == 1 ? "-" : "--") + flag));
                    }
                }
            }
        }
        return optionValue;
    }

    public Set<String> getOptionValues(final QueryFlag queryFlag) {
        final Set<String> optionValues = Sets.newLinkedHashSet();
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : getOptionValue(flag)) {
                    for (final String splittedArgument : COMMA_SPLITTER.split(optionArgument.toString())) {
                        optionValues.add(splittedArgument);
                    }
                }
            }
        }

        return optionValues;
    }

    // TODO: [AH] only this CIString version should be used
    public Set<CIString> getOptionValuesCI(final QueryFlag queryFlag) {
        final Set<CIString> optionValues = Sets.newLinkedHashSet();
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : getOptionValue(flag)) {
                    for (final String splittedArgument : COMMA_SPLITTER.split(optionArgument.toString())) {
                        optionValues.add(ciString(splittedArgument));
                    }
                }
            }
        }

        return optionValues;
    }

    @Override
    public String toString() {
        return originalStringQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final QueryParser that = (QueryParser) o;

        return Objects.equals(originalStringQuery, that.originalStringQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalStringQuery);
    }

    public boolean hasOnlyQueryFlag(final QueryFlag queryFlag) {
        final List<OptionSpec<?>> specs = options.specs();
        return specs.size() == 1 && queryFlag.getFlags().contains(specs.get(0).options().iterator().next());
    }

    public static boolean hasFlags(final String queryString) {
        return PARSER.parse(Iterables.toArray(SPACE_SPLITTER.split(queryString), String.class)).hasOptions();
    }

    static class QueryFlagParser extends OptionParser {
        {
            for (final QueryFlag queryFlag : QueryFlag.values()) {
                for (final String flag : queryFlag.getFlags()) {
                    final OptionSpecBuilder optionSpecBuilder = accepts(flag);
                    if (queryFlag.getRequiredArgument() != null) {
                        optionSpecBuilder.withRequiredArg().ofType(queryFlag.getRequiredArgument());
                    }
                }
            }
        }

        @Override
        public OptionSet parse(final String... arguments) {
            for (final String argument : arguments) {
                final Matcher matcher = FLAG_PATTERN.matcher(argument);
                if (matcher.matches() && !isValidOption(matcher)) {
                    throw new IllegalArgumentExceptionMessage(QueryMessages.malformedQuery("Invalid option: " + argument));
                }
            }

            try {
                return super.parse(arguments);
            } catch (OptionException e) {
                throw new IllegalArgumentExceptionMessage(QueryMessages.malformedQuery());
            }
        }

        private boolean isValidOption(final Matcher matcher) {
            final boolean shortOptionSupplied = matcher.group(1).length() == 1;
            final String suppliedFlag = matcher.group(2);

            for (final String flag : QueryFlag.getValidLongFlags()) {
                if (flag.equalsIgnoreCase(suppliedFlag)) {
                    return !shortOptionSupplied;
                }
            }

            return shortOptionSupplied;
        }
    }

    public boolean hasSubstitutions() {
        return hasSubstitutions;
    }
}
