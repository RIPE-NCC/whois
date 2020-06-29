package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.X509AutoKeyFactory;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X509KeycertValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock X509AutoKeyFactory x509AutoKeyFactory;
    @Mock KeyWrapperFactory keyWrapperFactory;
    @InjectMocks X509KeycertValidator subject;
    List<Message> messages;

    @Before
    public void setup() {
        messages = Lists.newArrayList();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                messages.add((Message) args[2]);
                return null;
            }
        }).when(updateContext).addMessage(any(UpdateContainer.class), any(RpslAttribute.class), any(Message.class));
    }

    @Test
    public void test() {
        final RpslObject rpslObject =  RpslObject.parse(
                "key-cert:       AUTO-1\n" +
                "method:         X509\n" +
                "owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net\n" +
                "fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2\n" +
                "certif:         -----BEGIN CERTIFICATE-----\n" +
                "certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M\n" +
                "certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP\n" +
                "certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG\n" +
                "certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy\n" +
                "certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k\n" +
                "certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy\n" +
                "certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN\n" +
                "certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv\n" +
                "certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON\n" +
                "certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9\n" +
                "certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W\n" +
                "certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1\n" +
                "certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj\n" +
                "certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx\n" +
                "certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT\n" +
                "certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC\n" +
                "certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF\n" +
                "certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1\n" +
                "certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo\n" +
                "certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==\n" +
                "certif:         -----END CERTIFICATE-----\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        assertThat(messages, is(empty()));
    }

}
