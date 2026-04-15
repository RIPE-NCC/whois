package net.ripe.db.whois.update.handler.validator.common;


import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactValidatorTest {

    @Mock private UpdateContext updateContext;

    @Mock
    private PreparedUpdate update;

    @InjectMocks
    private ContactValidator subject;

    @Test
    public void validate_whatApp_links() {
        final RpslObject rpslObject = RpslObject.parse("""
                person: Miguel H
                nic-hdl: MH1
                contact: https://wa.me/+441231231231
                contact: https://api.whatsapp.com/send?phone=+441231231231
                contact: https://wa.me/441231231231
                contact: https://api.whatsapp.com/send?phone=441231231231
                contact: http://wa.me/+441231231231
                contact: http://api.whatsapp.com/send?phone=+441231231231
                contact: a
                contact: https://wa.meaaa/+441231231231
                contact: https://wadfadfsa.me/+441231231231
                contact: https://wa.me/notAphone
                contact: https://api.whatsappaa.com/send?phone=+441231231231
                contact: https://api.whatsapp.com/send?+441231231231
                contact: https://api.whatsapp.com/send?phone1=+441231231231
                contact: httpsdddd://api.whatsapp.com/send?phone1=+441231231231
                """);

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        final List<Message> messageList = subject.performValidation(update, updateContext);

        assertThat(messageList.size(), is(8));
        assertThat(messageList.getFirst().toString(), is("Contact attribute syntax is not recognised: a"));
        assertThat(messageList.get(1).toString(), is("Contact attribute syntax is not recognised: https://wa.meaaa/+441231231231"));
        assertThat(messageList.get(2).toString(), is("Contact attribute syntax is not recognised: https://wadfadfsa.me/+441231231231"));
        assertThat(messageList.get(3).toString(), is("Contact attribute syntax is not recognised: https://wa.me/notAphone"));
        assertThat(messageList.get(4).toString(), is("Contact attribute syntax is not recognised: https://api.whatsappaa.com/send?phone=+441231231231"));
        assertThat(messageList.get(5).toString(), is("Contact attribute syntax is not recognised: https://api.whatsapp.com/send?+441231231231"));
        assertThat(messageList.get(6).toString(), is("Contact attribute syntax is not recognised: https://api.whatsapp.com/send?phone1=+441231231231"));
        assertThat(messageList.get(7).toString(), is("Contact attribute syntax is not recognised: httpsdddd://api.whatsapp.com/send?phone1=+441231231231"));
    }

    @Test
    public void validate_signal_links() {
        final RpslObject rpslObject = RpslObject.parse("""
                person: Miguel H
                nic-hdl: MH1
                contact: https://signal.me/#p/+441231231231
                contact: https://signal.me/#p/+441231231231 #comments still work
                contact: https://signal.me/#p/441231231231
                contact: http://signal.me/#p/+441231231231
                contact: https://signal.me/+441231231231
                contact: https://signalaaaa.me/#p/+441231231231
                contact: https://signal.me/#p/notAphone
                contact: https://signal.meaaa/send?phone=+441231231231
                contact: https://signal.me/send?+441231231231
                contact: https://signal.me/send?phone1=+441231231231
                contact: httpsdddd://signal.me/send?phone1=+441231231231
                """);

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        final List<Message> messageList = subject.performValidation(update, updateContext);

        assertThat(messageList.size(), is(7));
        assertThat(messageList.getFirst().toString(), is("Contact attribute syntax is not recognised: https://signal.me/+441231231231"));
        assertThat(messageList.get(1).toString(), is("Contact attribute syntax is not recognised: https://signalaaaa.me/#p/+441231231231"));
        assertThat(messageList.get(2).toString(), is("Contact attribute syntax is not recognised: https://signal.me/#p/notAphone"));
        assertThat(messageList.get(3).toString(), is("Contact attribute syntax is not recognised: https://signal.meaaa/send?phone=+441231231231"));
        assertThat(messageList.get(4).toString(), is("Contact attribute syntax is not recognised: https://signal.me/send?+441231231231"));
        assertThat(messageList.get(5).toString(), is("Contact attribute syntax is not recognised: https://signal.me/send?phone1=+441231231231"));
        assertThat(messageList.get(6).toString(), is("Contact attribute syntax is not recognised: httpsdddd://signal.me/send?phone1=+441231231231"));
    }

    @Test
    public void validate_sip_links() {
        final RpslObject rpslObject = RpslObject.parse("""
                person: Miguel H
                nic-hdl: MH1
                contact: sip:alice@192.168.1.42
                contact: sip:+12065550146@10.2.3.4
                contact: sip:12065550146@10.2.3.4
                contact: sip:alice@10.2.3.4:22
                contact: sip:a
                contact: sip:alice12.2.3.4:22
                contact: sip:@12.2.3.4:22
                """);

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        final List<Message> messageList = subject.performValidation(update, updateContext);

        assertThat(messageList.size(), is(3));
        assertThat(messageList.getFirst().toString(), is("Contact attribute syntax is not recognised: sip:a"));
        assertThat(messageList.get(1).toString(), is("Contact attribute syntax is not recognised: sip:alice12.2.3.4:22"));
        assertThat(messageList.get(2).toString(), is("Contact attribute syntax is not recognised: sip:@12.2.3.4:22"));
    }

    @Test
    public void validate_telegram_links() {
        final RpslObject rpslObject = RpslObject.parse("""
                person: Miguel H
                nic-hdl: MH1
                contact: https://t.me/share/url?url=https//example.com&text=Check%20this%20out
                contact: https://telegram.me/share/url?url=https//example.com/page&text=Interesting%20article
                contact: tg://msg_url?url=https//example.com&text=Hello%20from%20Telegram
                contact: http://t.me/share/url?url=https//example.com&text=Check%20this%20out
                contact: http://telegram.me/share/url?url=https//example.com/page&text=Interesting%20article
                contact: tg://msg_url?url=https//example.com&text=Hello%20from%20Telegram
                contact: https://t.me/share/url?url=https//example.com
                contact: https://t.me/share/url?text=Check%20this%20out
                contact: https://t.me/share/url?url=text=Check%20this%20out
                contact: https://taaaa.me/share/url?url=https//example.com&text=Check%20this%20out
                contact: https://t.me/url?url=https//example.com&text=Check%20this%20out
                contact: https://telegram.me/share/url?text=Interesting%20article
                contact: https://telegramaaa.me/share/url?url=https//example.com/page&text=Interesting%20article
                contact: https://telegrama.me/url?url=https//example.com/page&text=Interesting%20article
                contact: tg://msgurl?url=https//example.com&text=Hello%20from%20Telegram
                contact: tg://msg_url?text=Hello%20from%20Telegram
                contact: tg://msg_url?url=https//example.com
                """);

        when(update.getUpdatedObject()).thenReturn(rpslObject);

        final List<Message> messageList = subject.performValidation(update, updateContext);

        assertThat(messageList.size(), is(11));
        assertThat(messageList.getFirst().toString(), is("Contact attribute syntax is not recognised: https://t.me/share/url?url=https//example.com"));
        assertThat(messageList.get(1).toString(), is("Contact attribute syntax is not recognised: https://t.me/share/url?text=Check%20this%20out"));
        assertThat(messageList.get(2).toString(), is("Contact attribute syntax is not recognised: https://t.me/share/url?url=text=Check%20this%20out"));

        assertThat(messageList.get(3).toString(), is("Contact attribute syntax is not recognised: https://taaaa.me/share/url?url=https//example.com&text=Check%20this%20out"));
        assertThat(messageList.get(4).toString(), is("Contact attribute syntax is not recognised: https://t.me/url?url=https//example.com&text=Check%20this%20out"));
        assertThat(messageList.get(5).toString(), is("Contact attribute syntax is not recognised: https://telegram.me/share/url?text=Interesting%20article"));

        assertThat(messageList.get(6).toString(), is("Contact attribute syntax is not recognised: https://telegramaaa.me/share/url?url=https//example.com/page&text=Interesting%20article"));
        assertThat(messageList.get(7).toString(), is("Contact attribute syntax is not recognised: https://telegrama.me/url?url=https//example.com/page&text=Interesting%20article"));
        assertThat(messageList.get(8).toString(), is("Contact attribute syntax is not recognised: tg://msgurl?url=https//example.com&text=Hello%20from%20Telegram"));

        assertThat(messageList.get(9).toString(), is("Contact attribute syntax is not recognised: tg://msg_url?text=Hello%20from%20Telegram"));
        assertThat(messageList.get(10).toString(), is("Contact attribute syntax is not recognised: tg://msg_url?url=https//example.com"));
    }
}
