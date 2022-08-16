package net.ripe.db.whois.update.dao;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@Tag("IntegrationTest")
public class LanguageCodeRepositoryIntegrationTest extends AbstractUpdateDaoIntegrationTest {

    @Autowired
    private LanguageCodeRepository subject;

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void getLanguageCodes() {
        assertThat(subject.getLanguageCodes(), notNullValue());
        assertThat(subject.getLanguageCodes(), hasSize(183));
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    public void getLanguageCodes_immutable() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            subject.getLanguageCodes().add(ciString("vq"));
        });
    }
}
