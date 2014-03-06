package net.ripe.db.whois.query;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.ripe.db.whois.common.IllegalArgumentExceptionMessage;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
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

    private static final QueryFlagParser PARSER = new QueryFlagParser();

    private final String originalStringQuery;
    private final String searchKey;
    private final OptionSet options;

    public QueryParser(final String query) {
        originalStringQuery = query;
        String[] args = Iterables.toArray(SPACE_SPLITTER.split(query), String.class);
        options = PARSER.parse(args);
        searchKey = SPACE_JOINER.join(options.nonOptionArguments());
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
    private Collection<?> getOptionValue(String flag) {
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

        QueryParser that = (QueryParser) o;

        return originalStringQuery.equals(that.originalStringQuery);
    }

    @Override
    public int hashCode() {
        return originalStringQuery.hashCode();
    }

    public boolean hasOnlyQueryFlag(QueryFlag queryFlag) {
        return options.specs().size() == 1 && queryFlag.getFlags().contains(options.specs().get(0).options().iterator().next());
    }

    public static boolean hasFlags(String queryString) {
        return !PARSER.parse(Iterables.toArray(SPACE_SPLITTER.split(queryString), String.class)).specs().isEmpty();
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
}