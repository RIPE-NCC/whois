package net.ripe.db.whois.api.healthcheck;

import net.ripe.db.whois.common.ReadinessUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadinessHealthCheck implements HealthCheck {

    private final ReadinessUpdater readinessUpdater;

    @Autowired
    public ReadinessHealthCheck(final ReadinessUpdater readinessUpdater) {
        this.readinessUpdater = readinessUpdater;
    }

    @Override
    public boolean check() {
        return readinessUpdater.isLoadBalancerEnabled();
    }
}
