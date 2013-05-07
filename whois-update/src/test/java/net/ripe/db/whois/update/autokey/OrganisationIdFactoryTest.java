package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.autokey.dao.OrganisationIdRepository;
import net.ripe.db.whois.update.domain.OrganisationId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationIdFactoryTest {
    private static final String SOURCE = "RIPE";

    @Mock OrganisationIdRepository organisationIdRepository;
    @InjectMocks OrganisationIdFactory subject;

    @Before
    public void setUp() throws Exception {
        subject.setSource(SOURCE);
    }

    @Test
    public void isKeyPlaceHolder_AUTO() {
        assertThat(subject.isKeyPlaceHolder("AUTO"), is(false));
    }

    @Test
    public void isKeyPlaceHolder_AUTO_() {
        assertThat(subject.isKeyPlaceHolder("AUTO-"), is(false));
    }

    @Test
    public void isKeyPlaceHolder_AUTO_1() {
        assertThat(subject.isKeyPlaceHolder("AUTO-1"), is(true));
    }

    @Test
    public void isKeyPlaceHolder_AUTO_100() {
        assertThat(subject.isKeyPlaceHolder("AUTO-100"), is(true));
    }

    @Test
    public void isKeyPlaceHolder_AUTO_100NL() {
        assertThat(subject.isKeyPlaceHolder("AUTO-100NL"), is(true));
    }

    @Test
    public void isKeyPlaceHolder_AUTO_100_NL() {
        assertThat(subject.isKeyPlaceHolder("AUTO-100-NL"), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void generate_invalid_placeHolder() {
        subject.generate("AUTO", RpslObject.parse("organisation: AUTO\norg-name: name"));
    }

    @Test
    public void generate_specified_space() {
        when(organisationIdRepository.claimNextAvailableIndex("DW", SOURCE)).thenReturn(new OrganisationId("DW", 10, SOURCE));

        final OrganisationId organisationId = subject.generate("AUTO-1234567DW", RpslObject.parse("organisation: AUTO\norg-name: name"));
        assertThat(organisationId.toString(), is("ORG-DW10-RIPE"));
    }

    @Test
    public void generate_unspecified_space_single_word_name() {
        when(organisationIdRepository.claimNextAvailableIndex("TA", SOURCE)).thenReturn(new OrganisationId("TA", 1, SOURCE));
        RpslObject orgObject = RpslObject.parse("organisation: AUTO-1\norg-name: Tesco");

        final OrganisationId organisationId = subject.generate("AUTO-1", orgObject);

        assertThat(organisationId.toString(), is("ORG-TA1-RIPE"));
    }

    @Test
    public void generate_unspecified_space() {
        when(organisationIdRepository.claimNextAvailableIndex("SATG", SOURCE)).thenReturn(new OrganisationId("SATG", 10, SOURCE));

        final OrganisationId organisationId = subject.generate("AUTO-111", RpslObject.parse("organisation: AUTO\norg-name: Satellite advisory Technologies Group Ltd"));
        assertThat(organisationId.toString(), is("ORG-SATG10-RIPE"));
    }

    @Test
    public void getAttributeType() {
        assertThat(subject.getAttributeType(), is(AttributeType.ORGANISATION));
    }

    @Test
    public void claim() throws Exception {
        try {
            subject.claim("ORG-DW10-RIPE");
            fail("claim() supported?");
        } catch (ClaimException e) {
            assertThat(e.getErrorMessage(), is(ValidationMessages.syntaxError("ORG-DW10-RIPE")));
        }
    }
}
