package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.api.freetext.PatternFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogFileAnalyzer extends Analyzer {
    private final Version matchVersion;


    public LogFileAnalyzer(final Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(matchVersion, reader);

        TokenStream tok = new LowerCaseFilter(matchVersion, new WordDelimiterFilter(
                tokenizer,
                WordDelimiterFilter.GENERATE_WORD_PARTS | WordDelimiterFilter.SPLIT_ON_CASE_CHANGE | WordDelimiterFilter.PRESERVE_ORIGINAL,
                CharArraySet.EMPTY_SET));

        tok = new LogFilePatternFilter(tok);

        return new TokenStreamComponents(tokenizer, tok);
    }

    /**
     * Filter to tokenize whois update logfile content.
     * (1) extract IP address from "REQUEST FROM:<ip>" (no space after colon).
     */
    private class LogFilePatternFilter extends PatternFilter {

        private final Pattern requestFromPattern = Pattern.compile("(?i)^(FROM)[:](.*)$");

        protected LogFilePatternFilter(final TokenStream input) {
            super(input);
        }

        @Override
        protected void tokenize(final CharSequence input) {
            final Matcher matcher = requestFromPattern.matcher(input);
            if (matcher.matches()) {
                super.tokens.add(matcher.group(1));
                super.tokens.add(matcher.group(2));
                return;
            }
        }
    }
}
