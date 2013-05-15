package net.ripe.db.whois.api.freetext;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.List;

class FreeTextAnalyzer extends Analyzer {
    static enum Operation {QUERY, INDEX}

    private static final List<String> STOP_WORDS = Lists.newArrayList(
            "a", "an", "and", "are", "as", "at",
            "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "s",
            "such", "t", "that", "the", "their", "then",
            "there", "these", "they", "this", "to", "was", "will", "with");

    private final Version matchVersion;
    private final Operation operation;

    public FreeTextAnalyzer(final Version matchVersion, final Operation operation) {
        this.matchVersion = matchVersion;
        this.operation = operation;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(matchVersion, reader);

        final CharArraySet stopSet = new CharArraySet(matchVersion, STOP_WORDS.size(), true);
        stopSet.addAll(STOP_WORDS);

        TokenStream tok = new StopFilter(matchVersion, tokenizer, stopSet);

        tok = new WordDelimiterFilter(
                tok,
                operation.equals(Operation.QUERY) ?
                        (WordDelimiterFilter.PRESERVE_ORIGINAL) :
                        (WordDelimiterFilter.GENERATE_WORD_PARTS |
                                WordDelimiterFilter.CATENATE_WORDS |
                                WordDelimiterFilter.CATENATE_NUMBERS |
                                WordDelimiterFilter.SPLIT_ON_CASE_CHANGE |
                                WordDelimiterFilter.PRESERVE_ORIGINAL),
                CharArraySet.EMPTY_SET);

        tok = new LowerCaseFilter(matchVersion, tok);

        if (operation.equals(Operation.INDEX)) {
            tok = new PartialAddressFilter(tok);
        }

        return new TokenStreamComponents(tokenizer, tok);
    }
}
