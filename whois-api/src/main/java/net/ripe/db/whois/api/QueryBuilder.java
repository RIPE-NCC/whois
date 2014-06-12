package net.ripe.db.whois.api;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import net.ripe.db.whois.query.QueryFlag;

import java.util.Arrays;
import java.util.Collection;

// TODO: QueryBuilder.get() should return a Query object, constructed optimally (i.e., not by Query.parse())
public  class QueryBuilder {
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings();

    private final StringBuilder query = new StringBuilder(128);

    public QueryBuilder addFlag(final QueryFlag queryFlag) {
        query.append(queryFlag.getLongFlag()).append(' ');
        return this;
    }

    public QueryBuilder addCommaList(final QueryFlag queryFlag, final String arg) {
        if (checkForNoParam(arg)) {
            throw new IllegalArgumentException(queryFlag.getLongFlag() + " has a flag argument");
        }
        query.append(queryFlag.getLongFlag()).append(' ').append(arg).append(' ');
        return this;
    }

    public QueryBuilder addCommaList(final QueryFlag queryFlag, final Collection<String> args) {
        if (args.size() > 0) {
            if (checkForNoParam(args)) {
                throw new IllegalArgumentException(queryFlag.getLongFlag() + " has a flag argument");
            }

            query.append(queryFlag.getLongFlag()).append(' ');
            COMMA_JOINER.appendTo(query, args);
            query.append(' ');
        }
        return this;
    }

    public String build(final String searchKey) {
        if (checkForNoParam(searchKey)) {
            throw new IllegalArgumentException("search key can not contain flags");
        }
        return query.append(searchKey).toString();
    }

    public static boolean checkForNoParam(final String... params) {
        return checkForNoParam(Arrays.asList(params));
    }

    public static boolean checkForNoParam(final Collection<String> params) {
        for (final String param : params) {
            for (final String searchparam : WHITESPACE_SPLITTER.split(param)) {
                if (searchparam.length() > 1 && searchparam.charAt(0) == '-') {
                    return true;
                }
            }
        }
        return false;
    }
}
