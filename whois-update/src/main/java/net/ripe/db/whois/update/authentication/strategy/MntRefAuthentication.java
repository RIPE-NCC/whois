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
public class MntRefAuthentication extends ReferenceAuthentication{

    private static final List<AttributeType> SUPPORTED_ATTRIBUTES = List.of(AttributeType.MBRS_BY_REF,
            AttributeType.MNT_BY, AttributeType.MNT_DOMAINS, AttributeType.MNT_ROUTES, AttributeType.MNT_LOWER,
            AttributeType.MNT_REF);

    public MntRefAuthentication(AuthenticationModule credentialValidators, RpslObjectDao rpslObjectDao) {
        super(credentialValidators, rpslObjectDao);
    }

    @Override
    public boolean supports(PreparedUpdate update) {
        return SUPPORTED_ATTRIBUTES.stream().map(update::getNewValues).anyMatch(values -> !values.isEmpty());
    }

    @Override
    public List<RpslObject> authenticate(PreparedUpdate update, UpdateContext updateContext) throws AuthenticationFailedException {
        return authenticate(update, updateContext, getCandidates(update, updateContext, ObjectType.MNTNER,
                SUPPORTED_ATTRIBUTES));
    }
}
