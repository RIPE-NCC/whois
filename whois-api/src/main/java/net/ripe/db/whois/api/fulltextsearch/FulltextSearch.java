package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Version;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FulltextSearch {

    private final Version version;

    public FulltextSearch(final ApplicationVersion applicationVersion) {
        this.version = new Version(
                applicationVersion.getVersion(),
                applicationVersion.getTimestamp(),
                applicationVersion.getCommitId());

    }

    abstract SearchResponse performSearch(final SearchRequest searchRequest, final String remoteAddr) throws IOException;


    protected SearchResponse.Lst getResponseHeader(final SearchRequest searchRequest, final long elapsedTime) {
        SearchResponse.Lst responseHeader = new SearchResponse.Lst("responseHeader");
        final List<SearchResponse.Int> responseHeaderInts = Lists.newArrayList(new SearchResponse.Int("status", "0"), new SearchResponse.Int("QTime", Long.toString(elapsedTime)));
        responseHeader.setInts(responseHeaderInts);

        final List<SearchResponse.Str> paramStrs = Lists.newArrayList();
        paramStrs.add(new SearchResponse.Str("q", searchRequest.getQuery()));
        paramStrs.add(new SearchResponse.Str("rows", Integer.toString(searchRequest.getRows())));
        paramStrs.add(new SearchResponse.Str("start", Integer.toString(searchRequest.getStart())));
        paramStrs.add(new SearchResponse.Str("hl", Boolean.toString(searchRequest.isHighlight())));
        paramStrs.add(new SearchResponse.Str("hl.simple.pre", searchRequest.getHighlightPre()));
        paramStrs.add(new SearchResponse.Str("hl.simple.post", searchRequest.getHighlightPost()));
        paramStrs.add(new SearchResponse.Str("wt", searchRequest.getFormat()));
        paramStrs.add(new SearchResponse.Str("facet", Boolean.toString(searchRequest.isFacet())));

        final SearchResponse.Lst params = new SearchResponse.Lst("params");
        params.setStrs(paramStrs);
        responseHeader.setLsts(Lists.newArrayList(params));
        return responseHeader;
    }

    protected SearchResponse.Lst createVersion() {
        final SearchResponse.Lst result = new SearchResponse.Lst("version");

        result.setStrs(Lists.newArrayList(
                new SearchResponse.Str("version", version.getVersion()),
                new SearchResponse.Str("timestamp", version.getTimestamp()),
                new SearchResponse.Str("commit_id", version.getCommitId())));

        return result;
    }

    protected String customizeIfFiltering(final String value){
        final Matcher matcher = Pattern.compile("\\((.*?\\))").matcher(value);
        if (!matcher.find()) {
            return value;
        }
        final String fullAttributeValue = matcher.group(1);
        final String attributeType = fullAttributeValue.replace("(", "").split(":")[0];

        if (attributeType==null || AttributeType.getByNameOrNull(attributeType) == null){
            return value;
        }
        return customize(value, fullAttributeValue);
    }
    protected String customize(final String value, final String attributeAndValue){
        final int attributeDelimiterPosition = attributeAndValue.indexOf(":");
        final StringBuilder customQuery = new StringBuilder(attributeAndValue);
        customQuery.insert(attributeDelimiterPosition, ".custom");

        return new StringBuilder(value)
                .insert(0, "(")
                .insert(value.lastIndexOf("AND"), " OR (" + customQuery + "))")
                .toString();
    }
    protected String escape(final String value) {
        return value.replaceAll("[/]", "\\\\/");
    }
}
