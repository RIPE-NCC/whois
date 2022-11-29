package net.ripe.db.whois.api.rdap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

@ExtendWith(MockitoExtension.class)
public class RdapRequestValidatorTest {

    @InjectMocks
    private RdapRequestValidator validator;

    @Test
    public void shouldThrowExceptionForInvalidOrganisation() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            validator.validateEntity("ORG-Test");
        });
    }

    @Test
    public void shouldNotThrowAnyExceptionForValidEntity() {
        validator.validateEntity("ORG-BAD1-TEST");
    }

    @Test
    public void shouldThrowExceptionForInvalidAutnum() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            validator.validateAutnum("TEST");
        });
    }

    @Test
    public void shouldThrowExceptionForInvalidIP() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            validator.validateIp("", "invalid");
        });
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
