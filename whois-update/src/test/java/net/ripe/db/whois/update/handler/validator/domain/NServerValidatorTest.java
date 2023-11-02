package net.ripe.db.whois.update.handler.validator.domain;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NServerValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @InjectMocks NServerValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.DOMAIN));
    }

    @Test
    public void enum_domain_mandatory_nserver_glue_present() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
                "domain: 8.8.8.e164.arpa\n" +
                "nserver: ns1.8.8.8.e164.arpa 192.0.2.1"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void enum_domain_mandatory_nserver_glue_not_present() {
        final RpslObject rpslObject = RpslObject.parse(
                "domain: 8.8.8.e164.arpa\n" +
                "nserver: ns1.8.8.8.e164.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.NSERVER), UpdateMessages.glueRecordMandatory( rpslObject.findAttribute(AttributeType.NSERVER),"8.8.8.e164.arpa"));
    }

    @Test
    public void enum_domain_nserver_glue_not_mandatory_not_present() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
                "domain: 8.8.8.e164.arpa\n" +
                "nserver: ns1.example.net"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void enum_domain_nserver_glue_is_present() {
        final RpslObject rpslObject = RpslObject.parse(
                "domain: 8.8.8.e164.arpa\n" +
                "nserver: ns1.example.net 192.0.2.1");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.NSERVER), UpdateMessages.invalidGlueForEnumDomain( rpslObject.findAttribute(AttributeType.NSERVER),"192.0.2.1/32"));
    }

    @Test
    public void validate_enum_missing_glue() {
        final RpslObject rpslObject = RpslObject.parse(
                "domain:      2.1.2.1.5.5.5.2.0.2.1.e164.arpa\n" +
                "nserver:     a.ns.2.1.2.1.5.5.5.2.0.2.1.e164.arpa\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.NSERVER), UpdateMessages.glueRecordMandatory( rpslObject.findAttribute(AttributeType.NSERVER), "2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));
    }

    @Test
    public void validate_enum_nserver_does_not_end_with_domain() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:      1.e164.arpa\n" +
                "nserver:     a.ns.e164.arpa\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_enum_nserver_with_glue() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:      1.e164.arpa\n" +
                "nserver:     a.ns.1.e164.arpa 193.46.210.1\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_in_addr_success() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "domain:  144.102.5.in-addr.arpa\n" +
                "nserver: 144.102.5.in-addr.arpa 81.20.133.177\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_in_addr_success_ipv4_ipv6() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "domain:         64.67.217.in-addr.arpa\n" +
                "nserver:        a.ns.64.67.217.in-addr.arpa 193.46.210.1\n" +
                "nserver:        ns1.64.67.217.in-addr.arpa 2001:db8::1\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_in_addr_invalid() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:  144.102.5.in-addr.arpa\n" +
                "nserver: ns1.internetprovider.ch 81.20.133.177\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.NSERVER), UpdateMessages.hostNameMustEndWith(rpslObject.findAttribute(AttributeType.NSERVER),"144.102.5.in-addr.arpa"));
    }

    @Test
    public void validate_glue_record_mandatory() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:  144.102.5.in-addr.arpa\n" +
                "nserver: ns1.144.102.5.in-addr.arpa\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.NSERVER), UpdateMessages.glueRecordMandatory( rpslObject.findAttribute(AttributeType.NSERVER),"144.102.5.in-addr.arpa"));
    }

    @Test
    public void validate_in_addr_valid() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "domain:  144.102.5.in-addr.arpa\n" +
                "nserver: ns1.144.102.5.in-addr.arpa 81.20.133.177\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }
}
