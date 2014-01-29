package net.ripe.db.whois.internal.api.sso;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

@Component
@Path("/user")
public class OrganisationsForSSOAuthService {

    private final InverseOrgFinder orgFinder;
    private final WhoisObjectServerMapper whoisObjectMapper;

    @Autowired
    public OrganisationsForSSOAuthService(final InverseOrgFinder orgFinder) {
        this.orgFinder = orgFinder;
        // TODO: [AH] autowire
        this.whoisObjectMapper = new WhoisObjectServerMapper(null, "");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{uuid}/organisations")
    public Response getOrganisationsForAuth(@PathParam("uuid") final String uuid) {
        final Set<RpslObject> organisationsForAuth = orgFinder.findOrganisationsForAuth("SSO " + uuid);
        if (organisationsForAuth.isEmpty()) {
            final WhoisResources whoisResources = new WhoisResources();
            whoisResources.setErrorMessages(Collections.singletonList(new ErrorMessage(new Message(Messages.Type.ERROR, "No organisations found"))));
            return Response.status(Response.Status.NOT_FOUND).entity(whoisResources).build();
        }

        return Response.ok(whoisObjectMapper.mapRpslObjects(organisationsForAuth)).build();
    }
}
