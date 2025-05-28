package net.ripe.db.whois.update.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountryCodeRepositoryTest {
    CountryCodeRepository subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new CountryCodeRepository(new String[]{"NL", "EN"});
    }

    @Test
    public void getCountryCodes() {
        assertThat(subject.getCountryCodes(), containsInAnyOrder(ciString("NL"), ciString("EN")));
    }

    @Test
    public void getCountryCodes_immutable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            subject.getCountryCodes().add(ciString("DE"));
        });
    }
}
