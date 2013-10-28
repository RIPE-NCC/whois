package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Manage permanent bans.
 */
@Component
@Path("/acl/bans")
public class AclBanService {
    private final AclServiceDao aclServiceDao;

    @Autowired
    public AclBanService(final AclServiceDao aclServiceDao) {
        this.aclServiceDao = aclServiceDao;
    }

    /**
     * List all current permanent bans.
     *
     * @return List of all current permanent bans.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Ban> getBans() {
        return aclServiceDao.getBans();
    }

    /**
     * Get the current permanent ban for the specified prefix.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 address prefix range for which to get the permanent ban.
     * @return Current permanent ban for the specified prefix.
     */
    @GET
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBan(@PathParam("prefix") final String prefix) {

        final IpInterval normalizedPrefix = AclServiceHelper.getNormalizedPrefix(AclServiceHelper.decode(prefix));

        try {
            return Response.ok(aclServiceDao.getBan(normalizedPrefix)).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Save the specified permanent ban.
     * <p/>
     * If the permanent ban does not already exist it will be created.
     *
     * @param ban The IPv4 or IPv6 permanent ban to create or update.
     * @return The updated permanent ban.
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveBan(final Ban ban) {
        final IpInterval normalizedPrefix = AclServiceHelper.getNormalizedPrefix(ban.getPrefix());
        ban.setPrefix(normalizedPrefix.toString());

        try {
            aclServiceDao.getBan(normalizedPrefix);
            aclServiceDao.updateBan(ban);
        } catch (EmptyResultDataAccessException e) {
            aclServiceDao.createBanEvent(normalizedPrefix, BlockEvent.Type.BLOCK_PERMANENTLY);
            aclServiceDao.createBan(ban);
        }

        return Response.ok(ban).build();
    }

    /**
     * Delete the specified permanent ban.
     *
     * @param prefix The IPv4 or IPv6 prefix range to delete.
     * @return The deleted permanent ban.
     */
    @DELETE
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteBan(@PathParam("prefix") final String prefix) {

        try {
            final IpInterval<?> normalizedPrefix = AclServiceHelper.getNormalizedPrefix(AclServiceHelper.decode(prefix));

            final Ban ban = aclServiceDao.getBan(normalizedPrefix);
            aclServiceDao.createBanEvent(normalizedPrefix, BlockEvent.Type.UNBLOCK);
            aclServiceDao.deleteBan(normalizedPrefix);
            return Response.ok(ban).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Get the current permanent ban for the specified prefix.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 address prefix range for which to get the permanent ban.
     * @return Current permanent ban for the specified prefix.
     */
    @GET
    @Path("/{prefix}/events")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<BanEvent> getBanEvents(@PathParam("prefix") final String prefix) {

        final IpInterval normalizedPrefix = AclServiceHelper.getNormalizedPrefix(AclServiceHelper.decode(prefix));
        return aclServiceDao.getBanEvents(normalizedPrefix);
    }
}
