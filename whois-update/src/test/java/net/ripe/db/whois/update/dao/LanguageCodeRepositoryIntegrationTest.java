package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class LanguageCodeRepositoryIntegrationTest extends AbstractUpdateDaoIntegrationTest {

    @Autowired
    private LanguageCodeRepository subject;

    @Test
    public void getLanguageCodes() {
        assertThat(subject.getLanguageCodes(), notNullValue());
        assertThat(subject.getLanguageCodes(), hasSize(183));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLanguageCodes_immutable() {
        subject.getLanguageCodes().add(ciString("vq"));
    }
}
