package net.ripe.db.whois.api.abusec;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.InternalJob;
import net.ripe.db.whois.api.whois.InternalUpdatePerformer;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
        final RpslObject organisation = objectDao.getByKey(ORGANISATION, orgkey);
        if (hasAbuseC(organisation)) {
            return Response.status(Response.Status.CONFLICT).entity("This organisation already has an abuse contact").build();
        }

        final RpslObject role = buildRole(organisation, email);
        final List<String> overridePasswords = Lists.newArrayList();

        final Origin origin = new InternalJob("AbuseCCreation");
        final RpslObject createdRole = updatePerformer.performUpdate(
                origin,
                updatePerformer.createUpdate(role, overridePasswords, null),
                role.toString(),
                Keyword.NEW,
                loggerContext);

        final RpslObject org = addAbuseCToOrganisation(organisation, createdRole.getKey().toString());
        updatePerformer.performUpdate(
                origin,
                updatePerformer.createUpdate(org, overridePasswords, null),
                org.toString(),
                Keyword.NONE,
                loggerContext);

        // TODO: emit URLs for rest.db instead
        return Response.ok(String.format("http://apps.db.ripe.net/whois/lookup/%s/organisation/%s.html", source.getName(), orgkey)).build();
    }

    private boolean hasAbuseC(final RpslObject organisation) {
        return !organisation.findAttributes(ABUSE_C).isEmpty();
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
