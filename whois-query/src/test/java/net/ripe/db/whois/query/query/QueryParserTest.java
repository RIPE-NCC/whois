package net.ripe.db.whois.query.query;

import net.ripe.db.whois.query.QueryParser;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class QueryParserTest {
    private QueryParser subject;

    private QueryParser parseWithNewline(String input) {
        // [EB]: userinput will always be newline terminated
        return new QueryParser(input + "\n");
    }

    private void parse(String input) {
        subject = parseWithNewline(input);
    }


    @Test
    public void equals_hashcode() {
        parse("-Tperson Truus");

        assertTrue(subject.equals(subject));
        assertThat(subject.hashCode(), is(subject.hashCode()));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals(2L));

        QueryParser differentQuery = parseWithNewline("-Tperson joost");
        assertFalse(subject.equals(differentQuery));
        assertThat(subject.hashCode(), not(is(differentQuery.hashCode())));

        QueryParser sameQuery = parseWithNewline("-Tperson Truus");
        assertTrue(subject.equals(sameQuery));
        assertThat(subject.hashCode(), is(sameQuery.hashCode()));
    }

}
