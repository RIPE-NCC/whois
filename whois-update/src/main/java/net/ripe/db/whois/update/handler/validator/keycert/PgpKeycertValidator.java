package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PgpKeycertValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PgpKeycertValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private static final CIString METHOD_PGP = CIString.ciString("PGP");

    private static final int MINIMUM_KEY_LENGTH = 2048;

    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public PgpKeycertValidator(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString method = updatedObject.getValueOrNullForAttribute(AttributeType.METHOD);
        if (!METHOD_PGP.equals(method)) {
            return;
        }

        final PgpPublicKeyWrapper wrapper;
        try {
            wrapper = PgpPublicKeyWrapper.parse(updatedObject);
        } catch (Exception e) {
            LOGGER.info("Unable to parse PGP keycert {} due to {}: {}", updatedObject.getKey(), e.getClass().getName(), e.getMessage());
            return;
        }

        if (wrapper.isExpired(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.publicKeyHasExpired(wrapper.getKeyId()));
        }

        if (wrapper.isRevoked()) {
            updateContext.addMessage(update, UpdateMessages.publicKeyIsRevoked(wrapper.getKeyId()));
        }

        if (wrapper.getPublicKey().getBitStrength() < MINIMUM_KEY_LENGTH) {
            updateContext.addMessage(update, UpdateMessages.publicKeyLengthIsWeak(wrapper.getKeyId(), MINIMUM_KEY_LENGTH, wrapper.getPublicKey().getBitStrength()));
        }
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }




}
