package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.List;

public interface AuthenticationStrategy {
    /**
     * Check if this authentication strategy should be used for the passed in update.
     *
     * @param update The update to check.
     * @return {@code true} if this authentication strategy should be used for the passed in update.
     */
    boolean supports(PreparedUpdate update);

    /**
     * Perform one or more authentication steps for the provided update.
     * <p/>
     * If authentication fails, this method must throw an AuthenticationFailedException
     *
     * @param update        The update to authenticate.
     * @param updateContext The update context.
     * @return All succesfully authenticated objects
     * @throws AuthenticationFailedException In case authentication fails.
     */
    List<RpslObject> authenticate(PreparedUpdate update, UpdateContext updateContext) throws AuthenticationFailedException;
}
