package net.ripe.db.whois.update.dao;

import org.junit.Before;
import org.junit.Test;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class CountryCodeRepositoryTest {
    CountryCodeRepository subject;

    @Before
    public void setUp() throws Exception {
        subject = new CountryCodeRepository(new String[]{"NL", "EN"});
    }

    @Test
    public void getCountryCodes() {
        assertThat(subject.getCountryCodes(), containsInAnyOrder(ciString("NL"), ciString("EN")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCountryCodes_immutable() {
        subject.getCountryCodes().add(ciString("DE"));
    }
}
