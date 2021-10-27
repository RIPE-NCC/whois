package net.ripe.db.whois.common.support;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QueryLogEntryTest {

    @Test
    public void negative_channel_id() {
        final QueryLogEntry entry = QueryLogEntry.parse("20180401 00:14:37 -215685797 PW-QRY-INFO <0+0+0>  3ms [127.0.0.1] --  WWW.COM.EU");

        assertThat(entry.getQueryString(), is("WWW.COM.EU"));
    }

    @Test
    public void empty_query_string_rejected() {
        final QueryLogEntry query = QueryLogEntry.parse("20180601 07:38:27 1553948452 PW-QRY-INFO <0+0+0> REJECTED 0ms [127.0.0.1] --");

        assertThat(query.getAddress(), is("127.0.0.1"));
        assertThat(query.getExecutionTime(), is("0ms"));
        assertThat(query.getQueryString(), is(emptyString()));
    }

    @Test
    public void is_qry_log() {
        assertThat(QueryLogEntry.isQryLog("/var/log/qry/qrylog.20190101.bz2"), is(true));
        assertThat(QueryLogEntry.isQryLog("qrylog.20190101.bz2"), is(true));
        assertThat(QueryLogEntry.isQryLog("qrylog.20190101"), is(true));

        assertThat(QueryLogEntry.isQryLog("/tmp/test"), is(false));
        assertThat(QueryLogEntry.isQryLog("/tmp/"), is(false));
    }

    @Test
    public void is_bzip2() {
        assertThat(QueryLogEntry.isBZip2("/var/log/qry/qrylog.20190101.bz2"), is(true));
        assertThat(QueryLogEntry.isBZip2("qrylog.20190101.bz2"), is(true));

        assertThat(QueryLogEntry.isBZip2("/var/log/qry/qrylog.20190101"), is(false));
        assertThat(QueryLogEntry.isBZip2("qrylog.20190101"), is(false));
    }


}
