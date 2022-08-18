package net.ripe.db.whois.update.dns.zonemaster;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestRequest;
import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StartDomainTestRequestTest {

    private StartDomainTestRequest startDomainTestRequest;
    @Mock
    Update update;


    @Test
    public void remove_last_dot_from_domain(){
        isDomainType();
        assertEquals("18.182.5.in-addr.arpa", startDomainTestRequest.getParams().getDomain());
    }

    private void isDomainType() {
        RpslObject rpslObject = new RpslObject(List.of(new RpslAttribute(AttributeType.DOMAIN, "18.182.5.in-addr.arpa.")));
        when(update.getSubmittedObject()).thenReturn(rpslObject);

        this.startDomainTestRequest = new StartDomainTestRequest(new DnsCheckRequest(update,
                "18.182.5.in-addr.arpa.", "glue"));
    }


}
