package net.ripe.db.whois.common.rpki;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static net.ripe.db.whois.common.rpki.ValidationStatus.VALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WhoisRoaCheckerTest {

    @Mock
    RpkiService rpkiService;

    @InjectMocks
    WhoisRoaChecker whoisRoaChecker;

    @Test
    public void exact_match_valid() {
        when(rpkiService.findRoas(eq("10.0.0.0/8"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "origin: AS1"
        ));
        assertThat(result.getValue(), is(ValidationStatus.VALID));
    }

    @Test
    public void different_origin_invalid() {
        when(rpkiService.findRoas(eq("10.0.0.0/8"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "origin: AS2"
        ));
        assertThat(result.getValue(), is(ValidationStatus.INVALID_ORIGIN));
    }

    @Test
    public void asn0_invalid() {
        when(rpkiService.findRoas(eq("10.0.0.0/8"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "origin: AS0"
        ));
        assertThat(result.getValue(), is(ValidationStatus.INVALID_ORIGIN));
    }

    @Test
    public void more_specific_roa_prefix_lower_length_invalid() {
        when(rpkiService.findRoas(eq("10.0.0.0/16"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/32",
                "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/16\n" +
                        "origin: AS1"
        ));
        assertThat(result.getValue(), is(ValidationStatus.INVALID_PREFIX_LENGTH));
    }

    @Test
    public void more_specific_roa_allowed_by_same_length_valid() {
        when(rpkiService.findRoas(eq("10.0.0.0/16"))).thenReturn(Sets.newHashSet(new Roa("AS1", 16, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/16\n" +
                        "origin: AS1"
        ));
        assertThat(result.getValue(), is(VALID));
    }

    @Test
    public void more_specific_roa_allowed_by_max_length_valid() {
        when(rpkiService.findRoas(eq("10.0.0.0/16"))).thenReturn(Sets.newHashSet(new Roa("AS1", 24, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/16\n" +
                        "origin: AS1"
        ));
        assertThat(result.getValue(), is(VALID));
    }

    @Test
    public void more_specific_roa_prefix_and_different_origin_invalid() {
        when(rpkiService.findRoas(eq("10.0.0.0/16"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/8", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/16\n" +
                        "origin: AS2"
        ));
        assertThat(result.getValue(), is(ValidationStatus.INVALID_PREFIX_AND_ORIGIN));
    }

    @Test
    public void less_specific_roa_prefix_valid() {
        when(rpkiService.findRoas(eq("10.0.0.0/8"))).thenReturn(Sets.newHashSet(new Roa("AS1", 8, "10.0.0.0/16", "ripe")));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 10.0.0.0/8\n" +
                        "origin: AS1"
        ));
        assertThat(result.getValue(), is(ValidationStatus.VALID));
    }

    @Test
    public void overlap_valid_invalid_roa_then_valid() {
        final Roa nonMatchingRoa = new Roa("AS44546", 24, "92.38.0.0/17", "ripe");
        final Roa matchingRoa = new Roa("AS61979", 24, "92.38.44.0/22", "ripe");
        when(rpkiService.findRoas(eq("92.38.45.0/24"))).thenReturn(Sets.newHashSet(nonMatchingRoa, matchingRoa));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 92.38.45.0/24\n" +
                        "origin: AS61979"
        ));
        assertThat(result.getValue(), is(ValidationStatus.VALID));
    }

    @Test
    public void overlap_two_invalid_roas_then_invalid() {
        final Roa nonMatchingRoa = new Roa("AS44546", 24, "92.38.0.0/17", "ripe");
        final Roa matchingRoa = new Roa("AS61979", 8, "92.38.44.0/22", "ripe");
        when(rpkiService.findRoas(eq("92.38.45.0/24"))).thenReturn(Sets.newHashSet(nonMatchingRoa, matchingRoa));
        final Map.Entry<Roa, ValidationStatus> result = whoisRoaChecker.validateAndGetInvalidRoa(RpslObject.parse(
                "route: 92.38.45.0/24\n" +
                        "origin: AS61979"
        ));
        assertThat(result.getValue(), is(ValidationStatus.INVALID_ORIGIN));
    }
}
