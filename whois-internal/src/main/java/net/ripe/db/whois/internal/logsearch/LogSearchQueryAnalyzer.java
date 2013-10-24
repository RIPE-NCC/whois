package net.ripe.db.whois.internal.logsearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class LogSearchQueryAnalyzer extends Analyzer {
    private final Version matchVersion;

    public LogSearchQueryAnalyzer(final Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(matchVersion, reader);
        TokenStream tok = new WordDelimiterFilter(tokenizer, WordDelimiterFilter.PRESERVE_ORIGINAL, CharArraySet.EMPTY_SET);
        tok = new LowerCaseFilter(matchVersion, tok);

        return new TokenStreamComponents(tokenizer, tok);
    }
}
