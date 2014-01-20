package net.ripe.db.whois.query.query;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.QueryMessages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryFlagParser extends OptionParser {
    public static final Pattern FLAG_PATTERN = Pattern.compile("(--?)([^-].*)");

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
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("Invalid option: " + argument));
            }
        }

        try {
            return super.parse(arguments);
        } catch (OptionException e) {
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery());
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
