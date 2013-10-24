package net.ripe.db.whois.internal.api.abusec;

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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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

        RpslObject organisation;
        try {
            organisation = objectDao.getByKey(ObjectType.ORGANISATION, orgkey);
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final CIString abuseContact = lookupAbuseMailbox(organisation);
            return Response.status(Response.Status.CONFLICT).entity(abuseContact.toString()).build();
        } catch (IllegalArgumentException e) {
            // abuse mailbox not found, continue
        }

        final RpslObject role = createAbuseCRole(organisation, email);
        final Map<String, String> credentials = Maps.newHashMap();
        credentials.put("abuseC-Creator","");

        final Origin origin = new InternalJob("AbuseCCreation");
        final RpslObject createdRole = updatePerformer.performUpdate(
                origin,
                updatePerformer.createOverrideUpdate(role, credentials, null),
                role.toString(),
                Keyword.NEW,
                loggerContext);

        final RpslObject updatedOrganisation = createOrganisationWithAbuseCAttribute(organisation, createdRole.getKey().toString());
        updatePerformer.performUpdate(
                origin,
                updatePerformer.createOverrideUpdate(updatedOrganisation, credentials, null),
                updatedOrganisation.toString(),
                Keyword.NONE,
                loggerContext);

        return Response.ok(String.format("http://apps.db.ripe.net/search/lookup.html?source=%s&key=%s&type=ORGANISATION", source.getName(), orgkey)).build();
    }

    @GET
    @Path("/{orgkey}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response lookupAbuseContact(@PathParam("orgkey") final String orgKey) {
        try {
            final RpslObject organisation = objectDao.getByKey(ObjectType.ORGANISATION, orgKey);
            try {
                final CIString abuseMailbox = lookupAbuseMailbox(organisation);
                return Response.ok(abuseMailbox.toString()).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private CIString lookupAbuseMailbox(final RpslObject organisation) {
        final CIString abusec = organisation.getValueForAttribute(AttributeType.ABUSE_C);
        final RpslObject role = objectDao.getByKey(ObjectType.ROLE, abusec);
        return role.getValueForAttribute(AttributeType.ABUSE_MAILBOX);
    }

    private RpslObject createAbuseCRole(final RpslObject organisation, final String abuseMailbox) {
        final List<RpslAttribute> attributes = Lists.newArrayList();
        attributes.add(new RpslAttribute(AttributeType.ROLE, "Abuse-c Role"));
        attributes.add(new RpslAttribute(AttributeType.NIC_HDL, "AUTO-1"));
        attributes.add(new RpslAttribute(AttributeType.ABUSE_MAILBOX, abuseMailbox));
        for (RpslAttribute mntRef : organisation.findAttributes(AttributeType.MNT_REF)) {
            attributes.add(new RpslAttribute(AttributeType.MNT_BY, mntRef.getValue()));
        }
        for (RpslAttribute address : organisation.findAttributes(AttributeType.ADDRESS)) {
            attributes.add(address);
        }
        final RpslAttribute email = organisation.findAttribute(AttributeType.E_MAIL);
        attributes.add(organisation.findAttribute(AttributeType.E_MAIL));
        attributes.add(new RpslAttribute(AttributeType.CHANGED, email.getValue()));
        final RpslAttribute source = organisation.findAttribute(AttributeType.SOURCE);
        attributes.add(source);
        return new RpslObject(attributes);
    }

    private RpslObject createOrganisationWithAbuseCAttribute(final RpslObject organisation, final String abusec) {
        final List<RpslAttribute> attributes = Lists.newArrayList(organisation.getAttributes());
        attributes.add(new RpslAttribute(AttributeType.ABUSE_C, abusec));
        return new RpslObject(organisation, attributes);
    }
}
