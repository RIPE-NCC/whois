package net.ripe.db.whois.api.rdap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.junit.rules.ExpectedException;
import org.junit.Rule;
import javax.ws.rs.BadRequestException;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RdapRequestValidatorTest {

    @Mock
    private RdapExceptionMapper rdapExceptionMapper;

    @InjectMocks
    private RdapRequestValidator validator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldThrowExceptionForInvalidOrganisation() {
        when(rdapExceptionMapper.badRequest("Invalid syntax."))
                .thenReturn(new BadRequestException("Invalid syntax."));

        expectedEx.expect(BadRequestException.class);
        expectedEx.expectMessage("Invalid syntax");

        validator.validateEntity("ORG-Test");
    }

    @Test
    public void shouldNotThrowAnyExceptionForValidEntity() {
        validator.validateEntity("ORG-BAD1-TEST");
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionForInvalidAutnum() {
        when(rdapExceptionMapper.badRequest("Invalid syntax."))
                .thenReturn(new BadRequestException("Invalid syntax."));

        validator.validateAutnum("TEST");
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionForInvalidIP() {
        when(rdapExceptionMapper.badRequest("Invalid syntax."))
                .thenReturn(new BadRequestException("Invalid syntax."));

        validator.validateIp("", "invalid");
    }

    @Test
    public void shouldNotThrowExceptionForValidIP() {
        validator.validateIp("", "192.0.0.0");
        verifyZeroInteractions(rdapExceptionMapper);
    }

    @Test
    public void shouldNotThrowAExceptionForValidAutnum() {
        validator.validateAutnum("AS102");
        verifyZeroInteractions(rdapExceptionMapper);
    }
}
