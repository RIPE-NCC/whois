package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContactValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final Pattern WHATS_APP_PATTERN = Pattern.compile("^https?://(wa\\.me/\\+?\\d+|api\\.whatsapp\\.com/send\\?phone=\\+?\\d+)$");
    private static final Pattern SIGNAL_PATTERN = Pattern.compile("^https?://signal\\.me/#p/\\+?[0-9]{7,15}$");
    private static final Pattern SIP_PATTERN = Pattern.compile("^sip:[a-zA-Z0-9_.!~*'()%+-]+@[a-zA-Z0-9.-]+(:\\d+)?$");
    private static final Pattern TELEGRAM_PATTERN = Pattern.compile("^(https?://(t\\.me|telegram\\.me)/share/url\\?url=[^&]+&text=[^&]+|tg://msg_url\\?url=[^&]+&text=[^&]+)$");


    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!updatedObject.containsAttribute(AttributeType.CONTACT)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();

        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.CONTACT)) {
            if (!isValidContact(attribute.getCleanValue())) {
                messages.add(UpdateMessages.contactValueNotRecognised(attribute));
            }
        }

        return messages;
    }

    private boolean isValidContact(final CIString value){
        final Matcher whatAppMatcher = WHATS_APP_PATTERN.matcher(value);
        if (whatAppMatcher.matches()) {
            return true;
        }

        final Matcher signalMatcher = SIGNAL_PATTERN.matcher(value);
        if (signalMatcher.matches()) {
            return true;
        }

        final Matcher sipMatcher = SIP_PATTERN.matcher(value);
        if (sipMatcher.matches()) {
            return true;
        }

        final Matcher telegramMatcher = TELEGRAM_PATTERN.matcher(value);

        return telegramMatcher.matches();
    }
}