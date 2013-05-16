package net.ripe.db.whois.api.freetext;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class PartialAddressFilterTest {

    @Test
    public void tokenize_ipv4() throws Exception {
        tokenize("1", "1");
        tokenize("1.2.3.4", "1.2.3.4", "1", "1.2", "1.2.3", "1.2.3.4");
        tokenize("1.2.3.4/32", "1.2.3.4/32", "1", "1.2", "1.2.3", "1.2.3.4");
        tokenize("255.255.255.255", "255.255.255.255", "255", "255.255", "255.255.255", "255.255.255.255");
    }

    @Test
    public void tokenize_ipv6() throws Exception {
        tokenize("2001::", "2001::", "2001", "2001:");
        tokenize("2001::/16", "2001::/16", "2001", "2001:");
        tokenize("2001::2008/128", "2001::2008/128", "2001", "2001:", "2001::2008");
        tokenize("a:b:c:d::", "a:b:c:d::", "a", "a:b", "a:b:c", "a:b:c:d");
        tokenize("a:b:c:d::/64", "a:b:c:d::/64", "a", "a:b", "a:b:c", "a:b:c:d");
        tokenize("2001:2002:2003:2004::", "2001:2002:2003:2004::", "2001", "2001:2002", "2001:2002:2003", "2001:2002:2003:2004");
        tokenize("2001:2002:2003:2004::/64", "2001:2002:2003:2004::/64", "2001", "2001:2002", "2001:2002:2003", "2001:2002:2003:2004");
        tokenize("a:b:c:d:e:e:e:e", "a:b:c:d:e:e:e:e", "a", "a:b", "a:b:c", "a:b:c:d");
        tokenize("2001:2002:2003:2004:2005:2006:2007:2008", "2001:2002:2003:2004:2005:2006:2007:2008", "2001", "2001:2002", "2001:2002:2003", "2001:2002:2003:2004");
        tokenize("a:b:c:d:e:e:e:e/128", "a:b:c:d:e:e:e:e/128", "a", "a:b", "a:b:c", "a:b:c:d");
        tokenize("2001:2002:2003:2004:2005:2006:2007:2008/128", "2001:2002:2003:2004:2005:2006:2007:2008/128", "2001", "2001:2002", "2001:2002:2003", "2001:2002:2003:2004");
        tokenize("2a00:1f78::fffe/48", "2a00:1f78::fffe/48", "2a00", "2a00:1f78");
    }

    @Test
    public void tokenize_words() throws Exception {
        tokenize("aaaa", "aaaa");
        tokenize("something", "something");
    }

    private void tokenize(final String input, final String... expectedTokens) throws IOException {
        final List<String> tokens = tokenize(input);
        assertThat(tokens, contains(expectedTokens));
    }

    private List<String> tokenize(final String input) throws IOException {
        final List<String> tokens = Lists.newArrayList();

        final WhitespaceTokenizer whitespaceTokenizer = new WhitespaceTokenizer(Version.LUCENE_41, new StringReader(input));
        final PartialAddressFilter subject = new PartialAddressFilter(whitespaceTokenizer);

        try {
            subject.reset();

            CharTermAttribute attribute = subject.addAttribute(CharTermAttribute.class);
            while (subject.incrementToken()) {
                tokens.add(attribute.toString());
            }

            return tokens;

        } finally {
            subject.end();
            subject.close();
        }
    }
}
