package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.fulltextsearch.FullTextAnalyzer.Operation;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

/**
 * The {@link org.apache.lucene.analysis.Analyzer} used in whois does *not* split on punctuation at the moment.
 * This has been done to support finding objects based on exact key containing interpunction.
 * Downside of not stripping the punctuation is that the index is blown up considerably because for example we store both
 * "members" and "members:" in the index.
 * We probably want to implement a filter that strips most punctuation but also emits punctuation that is part of an object's key.
 * This test has been added to more quickly find the cause of changes in fulltext search results as a result of Lucene upgrades or Analyzer changes :)
 */
public class FullTextAnalyzerTest {

    FullTextAnalyzer subject;

    @BeforeEach
    public void setup() {
        subject = new FullTextAnalyzer(Operation.INDEX);
    }

    @Test
    public void test_tokens_contain_colon() throws IOException {
        final String rpsl = "route-set: AS30006:RS-OTC\nmembers: 46.29.103.32/27\nmembers: 46.29.96.0/24\nmnt-ref:AA1-MNT, # first\n+AA2-MNT,    # second\n\tAA3-MNT\t#third\nsource: TEST";
        assertThat("as30006:rs-otc", is(in(tokenize(rpsl))));
    }

    @Test
    public void test_tokens_contain_quote() throws IOException {
        assertThat("\"dev", is(in(tokenize("remarks: \"DEV mntner\"\n"))));
    }

    private List<String> tokenize(final String text) throws IOException {
        TokenStream stream = subject.tokenStream("fieldName", text);

        final List<String> tokens = Lists.newArrayList();
        stream.reset();
        while (stream.incrementToken()) {
            CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);
            tokens.add(termAttribute.toString());
        }

        return tokens;
    }

}