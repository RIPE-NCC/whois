package net.ripe.db.whois.api.freetext;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.Reader;
import java.util.List;

public class FreeTextAnalyzer extends Analyzer {
    public enum Operation {QUERY, INDEX}

    private static final List<String> STOP_WORDS = Lists.newArrayList(
            "a", "an", "and", "are", "as", "at",
            "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "s",
            "such", "t", "that", "the", "their", "then",
            "there", "these", "they", "this", "to", "was", "will", "with");

    private final Operation operation;

    public FreeTextAnalyzer(final Operation operation) {
        this.operation = operation;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(reader);

        final CharArraySet stopSet = new CharArraySet(STOP_WORDS.size(), true);
        stopSet.addAll(STOP_WORDS);

        TokenStream tok = new StopFilter(tokenizer, stopSet);

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

        tok = new LowerCaseFilter(tok);

        if (operation.equals(Operation.INDEX)) {
            tok = new PatternFilter(tok);
        }

        return new TokenStreamComponents(tokenizer, tok);
    }
}
