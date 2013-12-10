package net.ripe.db.whois.internal.api.sso;

import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Component
@Path("/sso")
public class AuthService {

    private final InverseOrgFinder orgFinder;
    private final WhoisObjectServerMapper whoisObjectMapper;

    @Autowired
    public AuthService(final InverseOrgFinder orgFinder) {
        this.orgFinder = orgFinder;
        this.whoisObjectMapper = new WhoisObjectServerMapper(null, "");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{auth}")
    public Response getOrganisationsForAuth(@PathParam("auth") final String auth) {

        final Set<RpslObject> organisationsForAuth = orgFinder.findOrganisationsForAuth(auth);
        if (organisationsForAuth.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(whoisObjectMapper.mapRpslObjects(organisationsForAuth)).build();
    }
}
