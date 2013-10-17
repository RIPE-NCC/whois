package net.ripe.db.whois.api.abusec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.InternalJob;
import net.ripe.db.whois.api.whois.InternalUpdatePerformer;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Component
@Path("/abusec")
public class AbuseCService {
    private final RpslObjectDao objectDao;
    private Source source;
    private final InternalUpdatePerformer updatePerformer;
    private final LoggerContext loggerContext;

    @Autowired
    public AbuseCService(final SourceContext sourceContext,
                         final RpslObjectDao objectDao,
                         final InternalUpdatePerformer updatePerformer,
                         final LoggerContext loggerContext) {
        this.objectDao = objectDao;
        this.source = sourceContext.getCurrentSource();
        this.updatePerformer = updatePerformer;
        this.loggerContext = loggerContext;
    }

    @POST
    @Path("/{orgkey}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createAbuseRole(
            @PathParam("orgkey") final String orgkey,
            @FormParam("email") final String email) {
        final List<RpslObject> organisation = objectDao.getByKeys(ORGANISATION, Lists.newArrayList(CIString.ciString(orgkey)));
        if (organisation.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final CIString abuseContact = getAbuseContact(organisation.get(0));
        if (abuseContact != null) {
            return Response.status(Response.Status.CONFLICT).entity(abuseContact.toString()).build();
        }

        final RpslObject role = buildRole(organisation.get(0), email);
        final Map<String, String> credentials = Maps.newHashMap();
        credentials.put("abuseC-Creator","");

        final Origin origin = new InternalJob("AbuseCCreation");
        final RpslObject createdRole = updatePerformer.performUpdate(
                origin,
                updatePerformer.createOverrideUpdate(role, credentials, null),
                role.toString(),
                Keyword.NEW,
                loggerContext);

        final RpslObject org = addAbuseCToOrganisation(organisation.get(0), createdRole.getKey().toString());
        updatePerformer.performUpdate(
                origin,
                updatePerformer.createOverrideUpdate(org, credentials, null),
                org.toString(),
                Keyword.NONE,
                loggerContext);

        return Response.ok(String.format("http://apps.db.ripe.net/search/lookup.html?source=%s&key=%s&type=ORGANISATION", source.getName(), orgkey)).build();
    }

    @GET
    @Path("/{orgkey}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response lookupAbuseContact(@PathParam("orgkey") final String orgkey) {
        final List<RpslObject> organisation = objectDao.getByKeys(ORGANISATION, Lists.newArrayList(CIString.ciString(orgkey)));
        if (organisation.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final CIString abuseContact = getAbuseContact(organisation.get(0));
        if (abuseContact == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Response.ok(abuseContact.toString()).build();
    }

    @Nullable
    private CIString getAbuseContact(final RpslObject organisation) {
        final List<RpslAttribute> abuseContactAttribute = organisation.findAttributes(ABUSE_C);
        return abuseContactAttribute.isEmpty() ?
                null :
                objectDao.getByKey(ObjectType.ROLE, abuseContactAttribute.get(0).getCleanValue().toString()).getValueForAttribute(AttributeType.ABUSE_MAILBOX);
    }

    private RpslObject buildRole(final RpslObject organisation, final String email) {
        final StringBuilder builder = new StringBuilder();
        builder.append("role: Abuse-c Role\nnic-hdl: AUTO-1\nabuse-mailbox:").append(email);
        for (final CIString mnt : organisation.getValuesForAttribute(MNT_REF)) {
            builder.append("\nmnt-by:").append(mnt);
        }
        for (final CIString address : organisation.getValuesForAttribute(ADDRESS)) {
            builder.append("\naddress:").append(address);
        }
        builder.append("\ne-mail:").append(organisation.getValueForAttribute(E_MAIL));
        builder.append("\nchanged:").append(organisation.getValueForAttribute(E_MAIL));
        builder.append("\nsource: ").append(source.getName());

        return RpslObject.parse(builder.toString());
    }

    private RpslObject addAbuseCToOrganisation(final RpslObject organisation, final String nic) {
        final List<RpslAttribute> attributes = Lists.newArrayList(organisation.getAttributes());
        attributes.add(new RpslAttribute(ABUSE_C, nic));

        return new RpslObject(organisation, attributes);
    }
}
