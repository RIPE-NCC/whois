package net.ripe.db.whois.update.handler.validator.keycert;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
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
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

@Component
public class X509KeycertValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private static final CIString METHOD_X509 = CIString.ciString("X509");

    private static final Set<String> WEAK_HASH_ALGORITHMS = Sets.newHashSet("SHA1withRSA", "SHA1withDSA", "MD5withRSA", "MD5withDSA");

    private static final int MINIMUM_KEY_LENGTH_RSA = 1024;
    private static final int MINIMUM_KEY_LENGTH_DSA = 1024;

    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public X509KeycertValidator(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.ALLOC_MAINTAINER)) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString method = updatedObject.getValueOrNullForAttribute(AttributeType.METHOD);
        if (!METHOD_X509.equals(method)) {
            return;
        }

        final X509CertificateWrapper wrapper;
        try {
            wrapper = X509CertificateWrapper.parse(updatedObject);
        } catch (Exception e) {
            updateContext.log(new Message(Messages.Type.ERROR, "Unable to parse X509 keycert"), e);
            return;
        }

        if (wrapper.isExpired(dateTimeProvider)) {
            updateContext.addMessage(update, UpdateMessages.publicKeyHasExpired(updatedObject.getKey()));
        }

        if (WEAK_HASH_ALGORITHMS.contains(wrapper.getCertificate().getSigAlgName())) {
            updateContext.addMessage(update, UpdateMessages.certificateHasWeakHash(updatedObject.getKey(), wrapper.getCertificate().getSigAlgName()));
        }

        final PublicKey publicKey = wrapper.getCertificate().getPublicKey();
        if  (publicKey instanceof RSAPublicKey) {
            final int bitLength = ((RSAPublicKey)publicKey).getModulus().bitLength();
            if (bitLength < MINIMUM_KEY_LENGTH_DSA) {
                updateContext.addMessage(update, UpdateMessages.publicKeyLengthIsWeak("RSA", MINIMUM_KEY_LENGTH_RSA, bitLength));
            }
        } else {
            if  (publicKey instanceof DSAPublicKey) {
                final int bitLength = ((DSAPublicKey)publicKey).getParams().getP().bitLength();
                if (bitLength < MINIMUM_KEY_LENGTH_DSA) {
                    updateContext.addMessage(update, UpdateMessages.publicKeyLengthIsWeak("DSA", MINIMUM_KEY_LENGTH_DSA, bitLength));
                }
            } else {
                // skip key length check until we are sure about an appropriate minimum length for that algorithm
                updateContext.log(new Message(Messages.Type.INFO, "Skipping public key length check for algorithm %s", wrapper.getCertificate().getPublicKey().getClass().getName()));
            }
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
