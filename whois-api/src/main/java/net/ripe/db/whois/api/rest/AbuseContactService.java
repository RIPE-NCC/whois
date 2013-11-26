package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.mapper.AbuseContactMapper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.RpslAttributes;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/abuse-contact")
public class AbuseContactService {

    private final QueryHandler queryHandler;
    private final RpslObjectDao rpslObjectDao;
    private final InternalUpdatePerformer updatePerformer;
    private final LoggerContext loggerContext;

    @Autowired
    public AbuseContactService(final QueryHandler queryHandler, final RpslObjectDao rpslObjectDao, final InternalUpdatePerformer updatePerformer, final LoggerContext loggerContext) {
        this.queryHandler = queryHandler;
        this.rpslObjectDao = rpslObjectDao;
        this.updatePerformer = updatePerformer;
        this.loggerContext = loggerContext;
    }

    @GET
    @Path("/{key:.*}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AbuseResources lookup(
            @Context final HttpServletRequest request,
            @PathParam("key") final String key) {

        final String format = String.format("%s %s",
                QueryFlag.ABUSE_CONTACT.getLongFlag(),
                key);
        final Query query = Query.parse(format);

        final List<AbuseResources> abuseResources = Lists.newArrayList();

        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, new ApiResponseHandler() {

            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof RpslAttributes) {
                    final RpslAttributes responseAttributes = (RpslAttributes)responseObject;
                    abuseResources.add(AbuseContactMapper.mapAbuseContact(key, responseAttributes.getAttributes()));
                }
            }
        });

        if (abuseResources.isEmpty()) {
            throw new NotFoundException();
        }

        final AbuseResources result = abuseResources.get(0);

        final String parametersKey = result.getParameters().getPrimaryKey().getValue();
        if (parametersKey.equals("::/0") || parametersKey.equals("0.0.0.0 - 255.255.255.255")) {
            throw new NotFoundException();
        }

        return result;
    }

    @POST
    @Path("/{orgkey:.*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(
            @Context final HttpServletRequest request,
            @PathParam("orgkey") final String orgkey,
            @FormParam("email") final String email,
            @FormParam("password") final List<String> passwords,
            @FormParam("override") final String override) {

        if (StringUtils.isEmpty(orgkey)) {
            throw new IllegalArgumentException("orgkey is mandatory");
        }

        if (StringUtils.isEmpty(email)) {
            throw new IllegalArgumentException("email is mandatory");
        }

        final RpslObject originalOrganisation = rpslObjectDao.getByKey(ObjectType.ORGANISATION, orgkey);

        try {
            final CIString abuseContact = lookupAbuseMailbox(originalOrganisation);
            return Response.status(Response.Status.CONFLICT).entity(abuseContact.toString()).build();
        } catch (IllegalArgumentException e) {
            // abuse mailbox not found, continue
        }

        final RpslObject createRole = createAbuseCRole(originalOrganisation, email);

        final RpslObject createdRole = updatePerformer.performUpdate(
                updatePerformer.createOrigin(request),
                updatePerformer.createUpdate(createRole, passwords, null, override),
                updatePerformer.createContent(createRole, passwords, null, override),
                Keyword.NEW,
                loggerContext);

        final RpslObject updatedOrganisation = createOrganisationWithAbuseCAttribute(originalOrganisation, createdRole.getKey().toString());

        updatePerformer.performUpdate(
                updatePerformer.createOrigin(request),
                updatePerformer.createUpdate(updatedOrganisation, passwords, null, override),
                updatePerformer.createContent(updatedOrganisation, passwords, null, override),
                Keyword.NONE,
                loggerContext);

        return Response.ok().build();
    }

    private CIString lookupAbuseMailbox(final RpslObject organisation) {
        final String abuseRoleName = organisation.getValueForAttribute(AttributeType.ABUSE_C).toString();
        final RpslObject abuseRole = rpslObjectDao.getByKey(ObjectType.ROLE, abuseRoleName);
        return abuseRole.getValueForAttribute(AttributeType.ABUSE_MAILBOX);
    }

    private RpslObject createAbuseCRole(final RpslObject organisation, final String abuseMailbox) {
        final List<RpslAttribute> attributes = Lists.newArrayList();
        attributes.add(new RpslAttribute(AttributeType.ROLE, "Abuse-C Role"));
        attributes.add(new RpslAttribute(AttributeType.NIC_HDL, "AUTO-1"));
        attributes.add(new RpslAttribute(AttributeType.ABUSE_MAILBOX, abuseMailbox));
        for (RpslAttribute mntRef : organisation.findAttributes(AttributeType.MNT_REF)) {           // TODO: confirm this
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
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getType().equals(AttributeType.E_MAIL) || attributes.get(i).getType().equals(AttributeType.SOURCE)) {
                attributes.add(i + 1, new RpslAttribute(AttributeType.ABUSE_C, abusec));
                break;
            }
        }
        return new RpslObject(organisation, attributes);
    }

}
