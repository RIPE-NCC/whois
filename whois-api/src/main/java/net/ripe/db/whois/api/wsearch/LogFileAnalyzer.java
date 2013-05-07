package net.ripe.db.whois.api.wsearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;

class LogFileAnalyzer extends Analyzer {
    private final Version matchVersion;

    public LogFileAnalyzer(final Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(matchVersion, reader);

        return new TokenStreamComponents(tokenizer, new LowerCaseFilter(matchVersion, new WordDelimiterFilter(
                tokenizer,
                WordDelimiterFilter.GENERATE_WORD_PARTS | WordDelimiterFilter.SPLIT_ON_CASE_CHANGE | WordDelimiterFilter.PRESERVE_ORIGINAL,
                CharArraySet.EMPTY_SET)));
    }
}
