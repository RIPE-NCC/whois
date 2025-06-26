package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class KeywordTest {

    @Test
    public void getByKeyword() {
        for (final Keyword keyword : Keyword.values()) {
            final String keywordString = keyword.getKeyword();
            if (keywordString != null) {
                assertThat(Keyword.getByKeyword(keywordString), is(keyword));
            }
        }
    }

    @Test
    public void getAction() {
        for (final Keyword keyword : Keyword.values()) {
            assertThat(keyword.getAction(), not(nullValue()));
        }
    }
}
