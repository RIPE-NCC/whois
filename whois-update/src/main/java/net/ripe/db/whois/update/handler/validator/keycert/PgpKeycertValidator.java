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
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class PgpKeycertValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private static final CIString METHOD_PGP = CIString.ciString("PGP");

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString method = updatedObject.getValueOrNullForAttribute(AttributeType.METHOD);
        if (!METHOD_PGP.equals(method)) {
            return;
        }

        final PGPPublicKey publicKey = getPublicKey(updatedObject);
        if (publicKey == null) {
            return;
        }

        validateKeyLength(update, updateContext, updatedObject, publicKey);
        validateKeyAlgorithm(update, updateContext, updatedObject, publicKey);
    }

    private void validateKeyLength(
                    final PreparedUpdate update,
                    final UpdateContext updateContext,
                    final RpslObject updatedObject,
                    final PGPPublicKey publicKey) {

        final int keyLength = getKeyLength(publicKey);
        // TODO: validate key length
    }

    private void validateKeyAlgorithm(
                    final PreparedUpdate update,
                    final UpdateContext updateContext,
                    final RpslObject updatedObject,
                    final PGPPublicKey publicKey) {
        // TODO: validate algorithm
    }

    @Nullable
    private PGPPublicKey getPublicKey(final RpslObject rpslObject) {
        try {
            return PgpPublicKeyWrapper.parse(rpslObject).getPublicKey();
        } catch (Exception e) {
            return null;
        }
    }

    private static int getKeyLength(final PGPPublicKey publicKey) {
        return publicKey.getBitStrength();
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
