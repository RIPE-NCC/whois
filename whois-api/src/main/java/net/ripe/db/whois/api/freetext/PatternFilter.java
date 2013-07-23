package net.ripe.db.whois.api.freetext;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Split input using pattern matching.
 *
 * Split IPv4 and IPv6 addresses into multiple tokens when indexing, to allow querying on part of the address.
 * IPv4 addresses are split into individual octets (e.g. 192.168.1.1 into 192, 192.168, 192.168.1, 192.168.1.1).
 * IPv6 addresses are split into a maximum of 4 groups (assuming a /64 network address, ignore the local part)
 * (e.g. 2001:2002:2003:2004:: into 2001, 2001:2002, 2001:2002:2003, 2001:2002:2003:2004).
 *
 * The domain part of an email address is also separated.
 *
 * The original input is also preserved as a separate token.
 */
public class PatternFilter extends TokenFilter {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3})([.]\\d{1,3})([.]\\d{1,3})([.]\\d{1,3})(?:[/]\\d{1,2})?$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("(?i)^([0-9a-f]{0,4})([:][0-9a-f]{0,4})([:][0-9a-f]{1,4})?([:][0-9a-f]{1,4})?(?:.*)?(?:[/]\\d{1,3})?$");
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("(?i)^.+@((?:[^.]+[.])+[^.]+)$");

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

    private final List<String> tokens = Lists.newArrayList();
    private int index = 0;

    protected PatternFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            if (index < tokens.size()) {
                // iterate next token
                write(tokens.get(index++));
                return true;
            } else {
                // exhausted tokens
                tokens.clear();
                index = 0;
            }
        }

        if (input.incrementToken()) {
            tokenize(CharBuffer.wrap(termAttribute.buffer(), 0, termAttribute.length()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.tokens.clear();
        this.index = 0;
    }

    private void write(final String token) {
        char buffer[] = termAttribute.buffer();

        if (token.length() > termAttribute.length()) {
            buffer = termAttribute.resizeBuffer(token.length());
        }

        System.arraycopy(token.toCharArray(), 0, buffer, 0, token.length());
        termAttribute.setLength(token.length());
    }

    protected void tokenize(final CharSequence input) {
        final Matcher ipv4matcher = IPV4_PATTERN.matcher(input);
        if (ipv4matcher.find()) {
            final StringBuilder token = new StringBuilder();
            for (int index = 1; index <= ipv4matcher.groupCount(); index++) {
                final String group = ipv4matcher.group(index);
                token.append(group);
                this.tokens.add(token.toString());
            }
            return;
        }

        final Matcher ipv6matcher = IPV6_PATTERN.matcher(input);
        if (ipv6matcher.find()) {
            final StringBuilder token = new StringBuilder();
            for (int index = 1; index <= ipv6matcher.groupCount(); index++) {
                final String group = ipv6matcher.group(index);
                if (group == null) {
                    // stop on first empty group
                    break;
                }
                token.append(group);
                this.tokens.add(token.toString());
            }
            return;
        }

        final Matcher emailMatcher = EMAIL_ADDRESS_PATTERN.matcher(input);
        if (emailMatcher.find()) {
            tokens.add(emailMatcher.group(1));
            return;
        }
    }
}
