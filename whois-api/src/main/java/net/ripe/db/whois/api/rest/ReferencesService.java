package net.ripe.db.whois.api.rest;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Set;

@Component
@Path("/references")
public class ReferencesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferencesService.class);

    final RpslObjectDao rpslObjectDao;
    final RpslObjectUpdateDao rpslObjectUpdateDao;
    final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReferencesService(
        final RpslObjectDao rpslObjectDao,
        final RpslObjectUpdateDao rpslObjectUpdateDao,
        @Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Return all incoming references for a given object type and primary key
     *
     * @param sourceParam
     * @param objectTypeParam
     * @param keyParam
     */
    @GET
    @Path("/{source}/{objectType}/{key:.*}")
    public Response lookup(
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam) {

        final RpslObjectInfo primaryObject = rpslObjectDao.findByKey(ObjectType.getByName(objectTypeParam), keyParam);

        final Set<RpslObjectInfo> incomingReferences = findIncomingReferences(primaryObject);

        for (RpslObjectInfo reference : incomingReferences) {
            LOGGER.info("Reference from {} {}", reference.getObjectType(), reference.getKey());
        }

        return ok("ok");
    }

    /**
     * Find all incoming references to an object.
     *
     * TODO: move this logic to the DAO
     *
     * @return
     */
    private Set<RpslObjectInfo> findIncomingReferences(final RpslObjectInfo objectInfo) {
        final Set<RpslObjectInfo> results = Sets.newHashSet();

        LOGGER.info("Looking for references to {} {}", objectInfo.getObjectType(), objectInfo.getKey());

        final RpslObject rpslObject = rpslObjectDao.getById(objectInfo.getObjectId());
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectInfo.getObjectType());

        for (final AttributeType keyAttributeType : objectTemplate.getKeyAttributes()) {
            for (RpslAttribute keyAttribute : rpslObject.findAttributes(keyAttributeType)) {

                for (AttributeType attributeType : AttributeType.values()) {
                    if (attributeType.getReferences().contains(objectInfo.getObjectType())) {
                        final IndexStrategy indexStrategy = IndexStrategies.get(attributeType);
                        for (RpslObjectInfo referenceObjectInfo : indexStrategy.findInIndex(jdbcTemplate, keyAttribute.getCleanValue())) {
                            if (!referenceObjectInfo.equals(objectInfo)) {
                                LOGGER.info("\tfound reference from {} {}", referenceObjectInfo.getObjectType(), referenceObjectInfo.getKey());
                                results.add(referenceObjectInfo);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Delete an object, and also any incoming referencing objects.
     *
     * If any incoming references are themselves referenced from another object, then fail (the group must be closed).
     *
     * @param sourceParam
     * @param objectTypeParam
     * @param keyParam
     *
     */
    @DELETE
    @Path("/{source}/{objectType}/{key:.*}")
    public Response delete(
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam) {

        // TODO: these checks, and the deletions, must be in the same transaction. Move down to DAO layer, as a single operation.

        final RpslObjectInfo primaryObject = rpslObjectDao.findByKey(ObjectType.getByName(objectTypeParam), keyParam);

        final Set<RpslObjectInfo> references = findIncomingReferences(primaryObject);
        for (RpslObjectInfo reference : references) {
            for (RpslObjectInfo secondaryReference : findIncomingReferences(reference)) {
                if (!primaryObject.equals(secondaryReference) && !references.contains(secondaryReference)) {
                    return badRequest(
                        String.format("Found reference to %s %s from %s %s",
                            reference.getObjectType().getName(),
                            reference.getKey(),
                            secondaryReference.getObjectType().getName(),
                            secondaryReference.getKey()));
                }
            }
        }

        // TODO: perform per-object validation before deleting each object

        // TODO: add limit to size of group to delete? (i.e., two objects only - person and maintainer).

        // TODO: delete references
        // TODO: relete primary object

        return ok("ok");
    }

    // helper methods

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }



}
