package net.ripe.db.whois.api.whois;

import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRDAPService extends WhoisRestService {
    @Autowired
    private SourceContext sourceContext;

    @Autowired
    public WhoisRDAPService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        super(dateTimeProvider, updateRequestHandler, loggerContext, rpslObjectDao, rpslObjectUpdateDao, sourceContext, queryHandler);
    }

    @GET
    @TypeHint(WhoisResources.class)
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/rdap/{objectType}/{key}")
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        Set<CIString> sources = getSources();

        for (CIString source : sources) {
            try {
                return lookupObject(request, source.toString(), objectType, key, false);
            } catch (NotFoundException e) {}
        }

        throw new NotFoundException("Nothing found");
    }

    private Set<CIString> getSources() {
        return sourceContext.getAllSourceNames();
    }

}
