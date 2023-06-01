package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrgRefAuthentication extends ReferenceAuthentication {

    @Autowired
    public OrgRefAuthentication(final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao) {
        super(credentialValidators, rpslObjectDao);
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return !update.getNewValues(AttributeType.ORG).isEmpty();
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        return authenticate(update, updateContext, getCandidates(update, updateContext, ObjectType.ORGANISATION,
                List.of(AttributeType.ORG)));
    }
}
