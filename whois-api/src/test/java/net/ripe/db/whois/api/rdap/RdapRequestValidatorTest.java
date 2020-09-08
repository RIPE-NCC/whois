package net.ripe.db.whois.api.rdap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class RdapRequestValidatorTest {

    @InjectMocks
    private RdapRequestValidator validator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldThrowExceptionForInvalidOrganisation() {
        expectedEx.expect(NotFoundException.class);
        expectedEx.expectMessage("Invalid syntax");

        validator.validateEntity("ORG-Test");
    }

    @Test
    public void shouldNotThrowAnyExceptionForValidEntity() {
        validator.validateEntity("ORG-BAD1-TEST");
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionForInvalidAutnum() {
        validator.validateAutnum("TEST");
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionForInvalidIP() {
        validator.validateIp("", "invalid");
    }

    @Test
    public void shouldNotThrowExceptionForValidIP() {
        validator.validateIp("", "192.0.0.0");
    }

    @Test
    public void shouldNotThrowAExceptionForValidAutnum() {
        validator.validateAutnum("AS102");
    }
}
