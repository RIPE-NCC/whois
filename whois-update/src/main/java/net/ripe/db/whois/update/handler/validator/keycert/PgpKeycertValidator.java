package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PgpKeycertValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private static final CIString METHOD_PGP = CIString.ciString("PGP");

    private static final int MINIMUM_KEY_LENGTH_RSA = 1024;
    private static final int MINIMUM_KEY_LENGTH_DSA = 1024;

    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public PgpKeycertValidator(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString method = updatedObject.getValueOrNullForAttribute(AttributeType.METHOD);
        if (!METHOD_PGP.equals(method)) {
            return Collections.emptyList();
        }

        final PgpPublicKeyWrapper wrapper;
        try {
            wrapper = PgpPublicKeyWrapper.parse(updatedObject);
        } catch (Exception e) {
            updateContext.log(new Message(Messages.Type.ERROR, "Unable to parse PGP keycert"), e);
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        if (wrapper.isExpired(dateTimeProvider)) {
            messages.add(UpdateMessages.publicKeyHasExpired(wrapper.getKeyId()));
        }

        if (wrapper.isRevoked()) {
            messages.add(UpdateMessages.publicKeyIsRevoked(wrapper.getKeyId()));
        }

        switch (wrapper.getPublicKey().getAlgorithm()) {
            case PublicKeyAlgorithmTags.DSA:
                if (wrapper.getPublicKey().getBitStrength() < MINIMUM_KEY_LENGTH_DSA) {
                    messages.add(UpdateMessages.publicKeyLengthIsWeak("DSA", MINIMUM_KEY_LENGTH_DSA, wrapper.getPublicKey().getBitStrength()));
                }
                break;
            case PublicKeyAlgorithmTags.RSA_GENERAL:
                if (wrapper.getPublicKey().getBitStrength() < MINIMUM_KEY_LENGTH_RSA) {
                    messages.add(UpdateMessages.publicKeyLengthIsWeak("RSA", MINIMUM_KEY_LENGTH_RSA, wrapper.getPublicKey().getBitStrength()));
                }
                break;
            default:
                // skip key length check until we are sure about an appropriate minimum length for that algorithm
                updateContext.log(new Message(Messages.Type.INFO, "Skipping public key length check for algorithm %d", wrapper.getPublicKey().getAlgorithm()));
                break;
        }

        return messages;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
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
