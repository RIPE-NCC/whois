package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

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
            assertNotNull(keyword.getAction());
        }
    }
}
