package net.ripe.db.whois.api.freetext;

import org.apache.commons.lang.Validate;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;

public class SearchOptions {

    private final int maxResults;
    private final Sort sort;
    private final QueryParser.Operator operator;

    public SearchOptions(final int maxResults, final Sort sort, final QueryParser.Operator operator) {
        Validate.notNull(sort);
        Validate.notNull(operator);
        this.maxResults = maxResults;
        this.sort = sort;
        this.operator = operator;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public Sort getSort() {
        return sort;
    }

    public QueryParser.Operator getOperator() {
        return operator;
    }
}
