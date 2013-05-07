package net.ripe.db.whois.query.handler;

import com.google.common.net.InetAddresses;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WhoisLogTest {

    @Mock Logger loggerMock;
    @InjectMocks WhoisLog subject;

    @Test
    public void default_constructor_also_inits_logger() {
        new WhoisLog().logQueryResult("QRY", 0, 0, null, 0, null, 0, "");
    }

    @Test
    public void time_gets_logged() {
        subject.logQueryResult("API", 1, 2, null, 1001, InetAddresses.forString("127.0.0.1"), 0, "testQuery");
        verify(loggerMock).info(anyString(), eq("         0"), eq("API"), eq(1), eq(2), eq(""), eq("1.00"), eq("127.0.0.1"), eq("testQuery"));
    }
}
