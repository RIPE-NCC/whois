package net.ripe.db.whois.update.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LanguageCodeRepositoryTest extends AbstractUpdateDaoTest {

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
