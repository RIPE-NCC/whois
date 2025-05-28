package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.dao.CountryCodeRepository;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.NicHandle;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AutoKeyResolverTest {
    @Mock AutoKeyFactory autoKeyFactory;
    @Mock CountryCodeRepository countryCodeRepository;
    @Mock LoggerContext loggerContext;

    @Mock Update update;

    AutoKeyResolver subject;

    UpdateContext updateContext;
    int index;

    @BeforeEach
    public void setUp() throws Exception {
        updateContext = new UpdateContext(loggerContext);
        lenient().when(update.getUpdate()).thenReturn(update);

        index = 1;

        lenient().when(autoKeyFactory.getAttributeType()).thenReturn(AttributeType.NIC_HDL);
        lenient().when(autoKeyFactory.isApplicableFor(any(RpslObject.class))).thenReturn(true);

        subject = new AutoKeyResolver(autoKeyFactory);

        primaryKeyGeneratorSuccessBehavior();
    }

    private void primaryKeyGeneratorSuccessBehavior() {
        lenient().when(autoKeyFactory.isKeyPlaceHolder(argThat(argument -> argument.toString().startsWith("AUTO")))).thenReturn(true);
        lenient().when(autoKeyFactory.generate(anyString(), any(RpslObject.class))).thenAnswer(invocation -> {
            final RpslObject rpslObject = (RpslObject) invocation.getArguments()[1];
            return new NicHandle(rpslObject.getTypeAttribute().getCleanValue().toString().substring(0, 1), index++, "RIPE");
        });
    }

    @Test
    public void resolveAutoKeys_key_create_auto() {
        RpslObject object = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: AUTO-1\n");

        final RpslObject rpslObject = subject.resolveAutoKeys(object, update, updateContext, Action.CREATE);

        assertThat(rpslObject.toString(), is("" +
                "person:         John Doe\n" +
                "nic-hdl:        J1-RIPE\n"));

        verify(autoKeyFactory, times(1)).generate(anyString(), any(RpslObject.class));
    }

    @Test
    public void resolveAutoKeys_reference_auto() {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: AUTO-1\n");

        subject.resolveAutoKeys(person, update, updateContext, Action.CREATE);

        RpslObject mntner = RpslObject.parse("" +
                "mntner: TST-MNT\n" +
                "admin-c: AUTO-1\n");

        final RpslObject rpslObject = subject.resolveAutoKeys(mntner, update, updateContext, Action.CREATE);

        assertThat(rpslObject.toString(), is("" +
                "mntner:         TST-MNT\n" +
                "admin-c:        J1-RIPE\n"));

        verify(autoKeyFactory, times(1)).generate(anyString(), any(RpslObject.class));
    }

    @Test
    public void resolveAutoKeys_self_reference() {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: AUTO-1\n" +
                "tech-c: AUTO-1\n" +
                "remarks: AUTO-1");

        final RpslObject rpslObject = subject.resolveAutoKeys(person, update, updateContext, Action.CREATE);

        assertThat(rpslObject.toString(), is("" +
                "person:         John Doe\n" +
                "nic-hdl:        J1-RIPE\n" +
                "tech-c:         J1-RIPE\n" +
                "remarks:        AUTO-1\n"));

        verify(autoKeyFactory, times(1)).generate(anyString(), any(RpslObject.class));
    }

    @Test
    public void resolveAutoKeys_modify() throws Exception {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: JD1-RIPE\n");

        subject.resolveAutoKeys(person, update, updateContext, Action.MODIFY);
        verify(autoKeyFactory, never()).claim(anyString());
        verify(autoKeyFactory, never()).generate(anyString(), any(RpslObject.class));
    }

    @Test
    public void resolveAutoKeys_key_specified_available_create() throws Exception {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: JD1-RIPE\n");

        when(autoKeyFactory.claim(anyString())).thenReturn(new NicHandle("JD", 1, "RIPE"));

        final RpslObject rpslObject = subject.resolveAutoKeys(person, update, updateContext, Action.CREATE);

        assertThat(rpslObject, is(person));
        verify(autoKeyFactory).claim("JD1-RIPE");
        verify(autoKeyFactory, never()).generate(anyString(), any(RpslObject.class));
    }

    @Test
    public void resolveAutoKeys_key_specified_invalid() throws Exception {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: INVALID_NIC_HANDLE\n");

        when(autoKeyFactory.claim(anyString())).thenThrow(new ClaimException(ValidationMessages.syntaxError("INVALID_NIC_HANDLE", "")));

        final RpslObject rpslObject = subject.resolveAutoKeys(person, update, updateContext, Action.CREATE);

        assertThat(rpslObject, is(person));
        verify(autoKeyFactory).claim("INVALID_NIC_HANDLE");
        verify(autoKeyFactory, never()).generate(anyString(), any(RpslObject.class));

        assertThat(updateContext.getMessages(update).getMessages(person.getAttributes().get(1)).getAllMessages(), contains(ValidationMessages.syntaxError("INVALID_NIC_HANDLE", "")));
    }

    @Test
    public void resolveAutoKeys_key_specified_not_available_create() throws Exception {
        RpslObject person = RpslObject.parse("" +
                "person: John Doe\n" +
                "nic-hdl: JD1-RIPE\n");

        when(autoKeyFactory.claim(anyString())).thenThrow(new ClaimException(UpdateMessages.nicHandleNotAvailable("JD1-RIPE")));

        final RpslObject rpslObject = subject.resolveAutoKeys(person, update, updateContext, Action.CREATE);

        assertThat(rpslObject, is(person));
        verify(autoKeyFactory).claim("JD1-RIPE");
        verify(autoKeyFactory, never()).generate(anyString(), any(RpslObject.class));
        assertThat(updateContext.getMessages(update).hasErrors(), is(true));
        assertThat(updateContext.getMessages(update).getMessages(person.getAttributes().get(1)).getAllMessages(), contains(UpdateMessages.nicHandleNotAvailable("JD1-RIPE")));
    }

    @Test
    public void resolveAutoKeys_reference_not_found() {
        when(autoKeyFactory.getKeyPlaceholder(any(CharSequence.class))).thenReturn(ciString("AUTO-1"));

        RpslObject mntner = RpslObject.parse("" +
                "mntner: TST-MNT\n" +
                "admin-c: AUTO-1\n");

        final RpslObject rpslObject = subject.resolveAutoKeys(mntner, update, updateContext, Action.MODIFY);

        assertThat(rpslObject.toString(), is("" +
                "mntner:         TST-MNT\n" +
                "admin-c:        AUTO-1\n"));

        assertThat(updateContext.getMessages(update).hasErrors(), is(true));
        assertThat(updateContext.getMessages(update).getMessages(mntner.getAttributes().get(1)).getAllMessages(), contains(UpdateMessages.referenceNotFound("AUTO-1")));
    }
}
