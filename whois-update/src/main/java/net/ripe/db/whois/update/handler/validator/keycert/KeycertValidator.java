package net.ripe.db.whois.update.handler.validator.keycert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.X509AutoKeyFactory;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import net.ripe.db.whois.update.keycert.KeyWrapper;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycertValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.KEY_CERT);

    private final KeyWrapperFactory keyWrapperFactory;
    private final X509AutoKeyFactory x509AutoKeyFactory;

    @Autowired
    public KeycertValidator(final KeyWrapperFactory keyWrapperFactory, final X509AutoKeyFactory x509AutoKeyFactory) {
        this.keyWrapperFactory = keyWrapperFactory;
        this.x509AutoKeyFactory = x509AutoKeyFactory;
    }

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();

        validateAutoKey(update, updateContext, updatedObject, customValidationMessages);
        return customValidationMessages;
    }

    private void validateAutoKey(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject updatedObject, final List<CustomValidationMessage> customValidationMessages) {
        if (x509AutoKeyFactory.isKeyPlaceHolder(updatedObject.getKey().toString())) {
            final KeyWrapper keyWrapper = keyWrapperFactory.createKeyWrapper(updatedObject, update, updateContext);
            if (keyWrapper instanceof PgpPublicKeyWrapper) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.autokeyForX509KeyCertsOnly(), update.getUpdatedObject().getTypeAttribute()));
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
