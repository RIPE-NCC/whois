package net.ripe.db.whois.api.healthcheck;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.common.HealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/healthcheck")
public class HealthCheckService {

    private final List<HealthCheck> healthChecks;

    @Autowired
    public HealthCheckService(final List<HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

    @GET
    public Response check() {
        for (HealthCheck healthCheck : healthChecks) {
            if (!healthCheck.check()) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("DISABLED").build();
            }
        }

        return Response.ok().entity("OK").build();
    }


}
