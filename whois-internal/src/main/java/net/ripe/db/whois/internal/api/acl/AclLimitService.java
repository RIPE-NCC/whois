package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.IpResourceTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static net.ripe.db.whois.internal.api.acl.AclServiceHelper.decode;

/**
 * Managing ACL limits.
 *
 * The limit prefix length may or may not be /64, normalize but do not validate
 */
@Component
@Path("/acl/limits")
public class AclLimitService {
    private final AclServiceDao aclServiceDao;

    @Autowired
    public AclLimitService(final AclServiceDao aclServiceDao) {
        this.aclServiceDao = aclServiceDao;
    }

    /**
     * List all current limits.
     *
     * @return List of all current limits.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Limit> getLimits() {
        return aclServiceDao.getLimits();
    }

    /**
     * Get the current limit for the specified prefix.
     * <p/>
     * If there is no exact match the limit for the most specific parent is returned.
     *
     * @param prefix The IPv4 or IPv6 address prefix range for which to get the limit.
     * @return Current limit for the specified prefix.
     */
    @GET
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getLimit(@PathParam("prefix") final String prefix) {

        final IpInterval<?> ipInterval = IpInterval.parse(decode(prefix));
        final Limit limit = getLimitsTree().getValue(ipInterval);
        return Response.ok(limit).build();
    }

    /**
     * Save the specified limit.
     * <p/>
     * If the limit does not already exist it will be created.
     * The limit can overlap with an existing limit. Intersecting limits are not allowed.
     *
     * @param limit The IPv4 or IPv6 limit to create or update.
     * @return The updated limit.
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveLimit(final Limit limit) {
        final IpInterval<?> ipInterval = IpInterval.parse(limit.getPrefix());
        limit.setPrefix(ipInterval.toString());

        final IpResourceTree<Limit> limitsTree = getLimitsTree();
        final Limit existingLimit = limitsTree.getValue(ipInterval);
        if (existingLimit != null && IpInterval.parse(existingLimit.getPrefix()).equals(ipInterval)) {
            aclServiceDao.updateLimit(limit);
        } else {
            limitsTree.add(ipInterval, limit);
            aclServiceDao.createLimit(limit);
        }

        return Response.ok(limit).build();
    }

    private IpResourceTree<Limit> getLimitsTree() {
        final IpResourceTree<Limit> ipResourceTree = new IpResourceTree<>();
        for (final Limit limit : aclServiceDao.getLimits()) {
            ipResourceTree.add(IpInterval.parse(limit.getPrefix()), limit);
        }

        return ipResourceTree;
    }

    /**
     * Delete the specified limit.
     *
     * @param prefix The IPv4 or IPv6 prefix range to delete.
     * @return The deleted limit.
     */
    @DELETE
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteLimit(@PathParam("prefix") final String prefix) {

        final IpInterval<?> ipInterval = IpInterval.parse(decode(prefix));

        for (final Limit limit : aclServiceDao.getLimits()) {
            final IpInterval<?> existingIpInterval = IpInterval.parse(limit.getPrefix());
            if (ipInterval.equals(existingIpInterval)) {
                aclServiceDao.deleteLimit(existingIpInterval);
                return Response.ok(limit).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
