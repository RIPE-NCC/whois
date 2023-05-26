package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IrtRefAuthentication extends ReferenceAuthentication {

    public IrtRefAuthentication(AuthenticationModule credentialValidators, RpslObjectDao rpslObjectDao) {
        super(credentialValidators, rpslObjectDao);
    }

    @Override
    public boolean supports(PreparedUpdate update) { return !update.getNewValues(AttributeType.MNT_IRT).isEmpty(); }

    @Override
    public List<RpslObject> authenticate(PreparedUpdate update, UpdateContext updateContext) throws AuthenticationFailedException {
        return authenticate(update, updateContext, getCandidates(update, updateContext, ObjectType.IRT,
                List.of(AttributeType.MNT_IRT)));
    }
}
