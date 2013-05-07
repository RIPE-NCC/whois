package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.autokey.dao.NicHandleRepository;
import net.ripe.db.whois.update.dao.CountryCodeRepository;
import net.ripe.db.whois.update.domain.NicHandle;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NicHandleFactoryTest {
    private static final String SOURCE = "RIPE";

    @Mock NicHandleRepository nicHandleRepository;
    @Mock CountryCodeRepository countryCodeRepository;
    @InjectMocks NicHandleFactory subject;

    @Before
    public void setUp() throws Exception {
        subject.setSource(SOURCE);

        when(countryCodeRepository.getCountryCodes()).thenReturn(Collections.<CIString>emptySet());
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
        subject.generate("AUTO", RpslObject.parse("person: name"));
    }

    @Test
    public void generate_specified_space() {
        when(nicHandleRepository.claimNextAvailableIndex("DW", SOURCE)).thenReturn(new NicHandle("DW", 10, SOURCE));

        final NicHandle nicHandle = subject.generate("AUTO-1234567DW", RpslObject.parse("person: name\nnic-hdl: AUTO-1234567DW"));
        assertThat(nicHandle.toString(), is("DW10-RIPE"));
    }

    @Test
    public void generate_unspecified_space() {
        when(nicHandleRepository.claimNextAvailableIndex("JAS", SOURCE)).thenReturn(new NicHandle("JAS", 10, SOURCE));

        final NicHandle nicHandle = subject.generate("AUTO-111", RpslObject.parse("person: John Archibald Smith\nnic-hdl: AUTO-111"));
        assertThat(nicHandle.toString(), is("JAS10-RIPE"));
    }

    @Test
    public void generate_unspecified_lower() {
        when(nicHandleRepository.claimNextAvailableIndex("SN", SOURCE)).thenReturn(new NicHandle("SN", 10, SOURCE));

        final NicHandle nicHandle = subject.generate("AUTO-111", RpslObject.parse("person: some name\nnic-hdl: AUTO-111"));
        assertThat(nicHandle.toString(), is("SN10-RIPE"));
    }

    @Test
    public void generate_unspecified_long() {
        when(nicHandleRepository.claimNextAvailableIndex("SATG", SOURCE)).thenReturn(new NicHandle("SATG", 10, SOURCE));

        final NicHandle nicHandle = subject.generate("AUTO-1234567", RpslObject.parse("person: Satellite advisory Technologies Group Ltd\nnic-hdl: AUTO-1234567"));
        assertThat(nicHandle.toString(), is("SATG10-RIPE"));
    }

    @Test
    public void getAttributeType() {
        assertThat(subject.getAttributeType(), is(AttributeType.NIC_HDL));
    }

    @Test
    public void claim() throws Exception {
        final NicHandle nicHandle = new NicHandle("DW", 10, "RIPE");
        when(nicHandleRepository.claimSpecified(nicHandle)).thenReturn(true);
        assertThat(subject.claim("DW10-RIPE"), is(nicHandle));
    }

    @Test
    public void claim_not_available() {
        when(nicHandleRepository.claimSpecified(new NicHandle("DW", 10, "RIPE"))).thenReturn(false);
        try {
            subject.claim("DW10-RIPE");
            fail("Claim succeeded?");
        } catch (ClaimException e) {
            assertThat(e.getErrorMessage(), is(UpdateMessages.nicHandleNotAvailable("DW10-RIPE")));
        }
    }

    @Test
    public void claim_invalid_handle() {
        try {
            subject.claim("INVALID_HANDLE");
            fail("Claim succeeded?");
        } catch (ClaimException e) {
            assertThat(e.getErrorMessage(), is(ValidationMessages.syntaxError("INVALID_HANDLE")));
        }
    }
}
