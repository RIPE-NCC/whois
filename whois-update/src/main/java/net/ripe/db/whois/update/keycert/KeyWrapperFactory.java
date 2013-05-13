package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;

@Component
public class KeyWrapperFactory {
    @CheckForNull
    public KeyWrapper createKeyWrapper(final RpslObject object, final UpdateContainer updateContainer, final UpdateContext updateContext) {
        try {
            if (PgpPublicKeyWrapper.looksLikePgpKey(object)) {
                return PgpPublicKeyWrapper.parse(object);
            } else if (X509CertificateWrapper.looksLikeX509Key(object)) {
                return X509CertificateWrapper.parse(object);
            } else {
                updateContext.addMessage(updateContainer, new Message(Messages.Type.ERROR, "The supplied object has no key"));
            }
        } catch (IllegalArgumentException e) {
            final Message errorMessage = new Message(Messages.Type.ERROR, e.getMessage());
            final List<RpslAttribute> attributes = object.findAttributes(AttributeType.CERTIF);
            if (attributes.isEmpty()) {
                updateContext.addMessage(updateContainer, errorMessage);
            } else {
                updateContext.addMessage(updateContainer, attributes.get(attributes.size() - 1), errorMessage);
            }
        }

        return null;
    }
}
