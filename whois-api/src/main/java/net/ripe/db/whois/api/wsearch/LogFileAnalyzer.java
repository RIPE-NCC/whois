package net.ripe.db.whois.api.wsearch;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.freetext.PatternFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.List;
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

        tok = new PatternFilter(tok) {
            final Pattern pattern = Pattern.compile("(?i)^(FROM:)(.*)$");
            private final List<String> tokens = Lists.newArrayList();

            @Override
            protected void tokenize(CharSequence input) {
                final Matcher matcher = pattern.matcher(input);
                if (matcher.matches()) {
                    this.tokens.add(matcher.group(1));
                    super.tokenize(matcher.group(2));
                    return;
                }
                super.tokenize(input);
            }
        };
        return new TokenStreamComponents(tokenizer, tok);
    }
}
