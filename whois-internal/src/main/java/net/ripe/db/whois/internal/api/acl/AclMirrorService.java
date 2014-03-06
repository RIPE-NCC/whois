package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.common.ip.IpInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static net.ripe.db.whois.internal.api.acl.AclServiceHelper.decode;
import static net.ripe.db.whois.internal.api.acl.AclServiceHelper.getNormalizedPrefix;

/**
 * Manage ACL mirror authorisation.
 * <p/>
 * Requests from prefix ranges with mirror support are allowed to use the mirror option in queries.
 */
@Component
@Path("/acl/mirrors")
public class AclMirrorService {
    private final AclServiceDao aclServiceDao;

    @Autowired
    public AclMirrorService(AclServiceDao aclServiceDao) {
        this.aclServiceDao = aclServiceDao;
    }

    /**
     * List all current prefixes with mirror authorisation.
     *
     * @return List of all prefixes with mirror authorisation.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Mirror> getMirrors() {
        return aclServiceDao.getMirrors();
    }

    /**
     * Get the current mirror authorisation for the specified prefix.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 address prefix range for which to get the mirror authorisation.
     * @return Current mirror authorisation for the specified prefix.
     */
    @GET
    @Path("/{prefix:.*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMirror(@PathParam("prefix") String prefix) {

        final IpInterval<?> normalizedPrefix = getNormalizedPrefix(decode(prefix));

        try {
            return Response.ok(aclServiceDao.getMirror(normalizedPrefix)).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Save the specified mirror.
     * <p/>
     * If the mirror does not exist, a new mirror will be created.
     *
     * @param mirror The IPv4 or IPv6 mirror authorisation.
     * @return The updated/created mirror.
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveMirror(final Mirror mirror) {
        final IpInterval<?> normalizedPrefix = getNormalizedPrefix(mirror.getPrefix());
        mirror.setPrefix(normalizedPrefix.toString());

        try {
            aclServiceDao.getMirror(normalizedPrefix);
            aclServiceDao.updateMirror(mirror);
        } catch (EmptyResultDataAccessException e) {
            aclServiceDao.createMirror(mirror);
        }

        return Response.ok(mirror).build();
    }

    /**
     * Delete the specified mirror authorisation.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 prefix range to be deleted.
     * @return The deleted mirror authorisation.
     */
    @DELETE
    @Path("/{prefix:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteMirror(@PathParam("prefix") final String prefix) {
        try {
            final IpInterval<?> normalizedPrefix = getNormalizedPrefix(decode(prefix));

            final Mirror mirror = aclServiceDao.getMirror(normalizedPrefix);
            aclServiceDao.deleteMirror(normalizedPrefix);
            return Response.ok(mirror).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
