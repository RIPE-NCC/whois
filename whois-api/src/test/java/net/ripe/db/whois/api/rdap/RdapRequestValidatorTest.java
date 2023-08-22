package net.ripe.db.whois.api.rdap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class RdapRequestValidatorTest {

    @InjectMocks
    private RdapRequestValidator validator;

    @Test
    public void shouldThrowExceptionForInvalidOrganisation() {
        assertThrows(RdapException.class, () -> {
            validator.validateEntity("ORG-Test");
        });
    }

    @Test
    public void shouldNotThrowAnyExceptionForValidEntity() {
        validator.validateEntity("ORG-BAD1-TEST");
    }

    @Test
    public void shouldThrowExceptionForInvalidAutnum() {
        assertThrows(RdapException.class, () -> {
            validator.validateAutnum("TEST");
        });
    }

    @Test
    public void shouldThrowExceptionForInvalidIP() {
        assertThrows(RdapException.class, () -> {
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
