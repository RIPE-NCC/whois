package net.ripe.db.whois.update.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LanguageCodeRepositoryTest {

    @InjectMocks
    private LanguageCodeRepository subject;

    @Before
    public void setUp() throws Exception {
        subject.setLanguageCodes(new String[]{"az", "bm"});
    }

    @Test
    public void getLanguageCodes() {
        assertThat(subject.getLanguageCodes(), containsInAnyOrder(ciString("az"), ciString("bm")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLanguageCodes_immutable() {
        subject.getLanguageCodes().add(ciString("vq"));
    }
}
