package net.ripe.db.whois.api.freetext;


import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SearchRequestTest {

    @Test
    public void query() throws Exception {
        SearchRequest subject = SearchRequest.parse((
                "http://db-apps-solr1/apache-solr-1.4.1/core0/select/" +
                        "?hl.requireFieldMatch=false" +
                        "&facet=true" +
                        "&sort=object-type+asc" +
                        "&facet.mincount=1" +
                        "&hl.mergeContiguous=true" +
                        "&hl.simple.pre=<b>" +
                        "&hl.fl=*" +
                        "&wt=javabin" +
                        "&hl=true" +
                        "&version=1" +
                        "&rows=10" +
                        "&start=60" +
                        "&q=(walker)" +
                        "&hl.simple.post=</b>" +
                        "&facet.field=object-type"));

        assertThat(subject.getQuery(), is("(walker)"));
        assertThat(subject.isFacet(), is(true));
        assertThat(subject.isHighlight(), is(true));
        assertThat(subject.getRows(), is(10));
        assertThat(subject.getStart(), is(60));
    }

    @Test
    public void escape_forward_slash() {
        SearchRequest subject = SearchRequest.parse("q=%282001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48%29");
        assertThat(subject.getQuery(), is("(2001\\:0638\\:0501\\:\\:\\/48)"));
    }
}
