package net.ripe.db.whois.api.acl;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static net.ripe.db.whois.api.acl.AclServiceHelper.getNormalizedPrefix;

/**
 * Managing ACL proxy authorisation.
 * <p/>
 * Requests from prefix ranges with proxy support are allowed to use the proxy option in queries.
 */
@ExternallyManagedLifecycle
@Component
@Path("/acl/proxies")
public class AclProxyService {
    private final AclServiceDao aclServiceDao;

    @Autowired
    public AclProxyService(final AclServiceDao aclServiceDao) {
        this.aclServiceDao = aclServiceDao;
    }

    /**
     * List all current prefixes with proxy authorisation.
     *
     * @return List of all prefixes with proxy authorisation.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Proxy.class)
    public List<Proxy> getProxies() {
        return aclServiceDao.getProxies();
    }

    /**
     * Get the current proxy authorisation for the specified prefix.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 address prefix range for which to get the proxy authorisation.
     * @return Current proxy authorisation for the specified prefix.
     */
    @GET
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Proxy.class)
    @StatusCodes(@ResponseCode(code = 404, condition = "Proxy with the specified prefix does not exist"))
    public Response getProxy(@PathParam("prefix") final String prefix) {
        final String normalizedPrefix = getNormalizedPrefix(prefix);

        try {
            return Response.ok(aclServiceDao.getProxy(normalizedPrefix)).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Save the specified proxy.
     * <p/>
     * If the proxy does not already exist it will be created.
     *
     * @param proxy The IPv4 or IPv6 proxy authorisation.
     * @return The updated proxy.
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Proxy.class)
    public Response saveProxy(final Proxy proxy) {
        final String normalizedPrefix = getNormalizedPrefix(proxy.getPrefix());
        proxy.setPrefix(normalizedPrefix);

        try {
            aclServiceDao.getProxy(normalizedPrefix);
            aclServiceDao.updateProxy(proxy);
        } catch (EmptyResultDataAccessException e) {
            aclServiceDao.createProxy(proxy);
        }

        return Response.ok(proxy).build();
    }

    /**
     * Delete the specified proxy authorisation.
     * <p/>
     *
     * @param prefix The IPv4 or IPv6 prefix range to delete.
     * @return The deleted proxy authorisation.
     */
    @DELETE
    @Path("/{prefix}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Proxy.class)
    @StatusCodes(@ResponseCode(code = 404, condition = "Proxy with the specified prefix does not exist"))
    public Response deleteProxy(@PathParam("prefix") final String prefix) {
        try {
            final String normalizedPrefix = getNormalizedPrefix(prefix);

            final Proxy proxy = aclServiceDao.getProxy(normalizedPrefix);
            aclServiceDao.deleteProxy(normalizedPrefix);
            return Response.ok(proxy).build();
        } catch (EmptyResultDataAccessException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
