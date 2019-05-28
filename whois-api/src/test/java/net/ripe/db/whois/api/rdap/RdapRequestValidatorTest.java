package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.*;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import javax.ws.rs.BadRequestException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RdapRequestValidatorTest {

    @Mock
    private RdapExceptionMapper rdapExceptionMapper;

    private RdapRequestValidator validator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        this.validator = new RdapRequestValidator(rdapExceptionMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForInvalidEntity() {
        validator.validateEntity("invalidEntity");
    }

    @Test
    public void shouldThrowExceptionForInvalidOrganisation() throws Exception{
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid syntax");

        validator.validateEntity("ORG-Test");
    }

    @Test(expected = Test.None.class)
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

    @Test(expected = Test.None.class)
    public void shouldNotThrowExceptionForValidIP() {
        validator.validateIp("", "192.0.0.0");
        verifyZeroInteractions(rdapExceptionMapper);
    }

    @Test(expected = Test.None.class)
    public void shouldNotThrowAExceptionForValidAutnum() {
        validator.validateAutnum("AS102");
        verifyZeroInteractions(rdapExceptionMapper);
    }
}
