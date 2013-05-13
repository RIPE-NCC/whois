package net.ripe.db.whois.update.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

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
