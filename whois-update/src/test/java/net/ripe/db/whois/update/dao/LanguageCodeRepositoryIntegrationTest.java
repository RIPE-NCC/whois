package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
public class LanguageCodeRepositoryIntegrationTest extends AbstractUpdateDaoIntegrationTest {

    @Autowired
    private LanguageCodeRepository subject;

    @Test
    public void getLanguageCodes() {
        assertThat(subject.getLanguageCodes(), notNullValue());
        assertThat(subject.getLanguageCodes(), hasSize(183));
    }

    @Test
    public void getLanguageCodes_immutable() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            subject.getLanguageCodes().add(ciString("vq"));
        });
    }
}
