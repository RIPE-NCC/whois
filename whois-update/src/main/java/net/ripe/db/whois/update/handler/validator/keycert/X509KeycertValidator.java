package net.ripe.db.whois.update.handler.validator.keycert;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class X509KeycertValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private static final CIString METHOD_X509 = CIString.ciString("X509");

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString method = updatedObject.getValueOrNullForAttribute(AttributeType.METHOD);
        if (!METHOD_X509.equals(method)) {
            return;
        }

        final X509Certificate certificate = getCertificate(updatedObject);
        if (certificate == null) {
            return;
        }

        validateKeyLength(update, updateContext, updatedObject, certificate);
        validateAlgorithm(update, updateContext, updatedObject, certificate);
    }

    private void validateKeyLength(
                    final PreparedUpdate update,
                    final UpdateContext updateContext,
                    final RpslObject updatedObject,
                    final X509Certificate certificate) {
        final int keyLength = getKeyLength(certificate.getPublicKey()); // TODO: catch

        // TODO: validate key length
    }

    private void validateAlgorithm(
                    final PreparedUpdate update,
                    final UpdateContext updateContext,
                    final RpslObject updatedObject,
                    final X509Certificate certificate) {
        // TODO: validate algorithm
    }

    private static int getKeyLength(final PublicKey publicKey) {
        if  (publicKey instanceof RSAPublicKey) {
            return ((RSAPublicKey)publicKey).getModulus().bitLength();
        } else {
            if  (publicKey instanceof DSAPublicKey) {
                return ((DSAPublicKey)publicKey).getParams().getP().bitLength();
            } else {
                if (publicKey instanceof ECPublicKey) {
                    return (publicKey.getEncoded().length - 1) / 2 * 8;
                } else {
                    throw new IllegalArgumentException("Unknown public key type " + publicKey.getClass().getName());
                }
            }
        }
    }

    @Nullable
    private static X509Certificate getCertificate(final RpslObject rpslObject) {
        try {
            return X509CertificateWrapper.parse(rpslObject).getCertificate();
        } catch (Exception e) {
            return null;
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
