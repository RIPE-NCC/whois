package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;

import java.util.List;

public class FullTextAnalyzer extends Analyzer {
    public enum Operation {QUERY, INDEX}

    private static final List<String> STOP_WORDS = Lists.newArrayList(
            "a", "an", "and", "are", "as", "at",
            "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "s",
            "such", "t", "that", "the", "their", "then",
            "there", "these", "they", "this", "to", "was", "will", "with");

    private final Operation operation;

    public FullTextAnalyzer(final Operation operation) {
        this.operation = operation;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();

        final CharArraySet stopSet = new CharArraySet(STOP_WORDS.size(), true);
        stopSet.addAll(STOP_WORDS);

        TokenStream tok = new StopFilter(tokenizer, stopSet);

        tok = new WordDelimiterGraphFilter(
                tok,
                operation.equals(Operation.QUERY) ?
                        (WordDelimiterGraphFilter.PRESERVE_ORIGINAL) :
                        (WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
                                WordDelimiterGraphFilter.CATENATE_WORDS |
                                WordDelimiterGraphFilter.CATENATE_NUMBERS |
                                WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
                                WordDelimiterGraphFilter.PRESERVE_ORIGINAL),
                CharArraySet.EMPTY_SET);

        tok = new LowerCaseFilter(tok);

        if (operation.equals(Operation.INDEX)) {
            tok = new PatternFilter(tok);
        }

        return new TokenStreamComponents(tokenizer, tok);
    }
}
