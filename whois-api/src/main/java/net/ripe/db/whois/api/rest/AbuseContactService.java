package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.mapper.AbuseContactMapper;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.RpslAttributes;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("/abuse-contact")
public class AbuseContactService {

    private final AbuseContactMapper abuseContactMapper;
    private final QueryHandler queryHandler;
    private final SourceContext sourceContext;

    @Autowired
    public AbuseContactService(final AbuseContactMapper abuseContactMapper, final QueryHandler queryHandler, final SourceContext sourceContext) {
        this.abuseContactMapper = abuseContactMapper;
        this.queryHandler = queryHandler;
        this.sourceContext = sourceContext;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{key:.*}")
    public AbuseResources abuseContact(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("key") final String key) {

        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new IllegalArgumentException("Invalid source: " + source);
        }

        final String format = String.format("%s %s %s %s ",
                QueryFlag.ABUSE_CONTACT.getLongFlag(),
                QueryFlag.SOURCES.getLongFlag(),
                source,
                (key == null ? "" : key));
        final Query query = Query.parse(format);

        final List<AbuseResources> abuseResources = Lists.newArrayList();

        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, new ApiResponseHandler() {

            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof RpslAttributes) {
                    final RpslAttributes abuseContactInfo = (RpslAttributes)responseObject;
                    abuseResources.add(abuseContactMapper.mapAbuseContact(key, abuseContactInfo.getAttributes()));
                }
            }
        });

        if (abuseResources.isEmpty()) {
            throw new NotFoundException();
        }

        return abuseResources.get(0);
    }
}
