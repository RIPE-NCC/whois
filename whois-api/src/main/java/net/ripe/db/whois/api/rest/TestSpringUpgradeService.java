package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.healthcheck.DatabaseHealthCheck;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbusePKey;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.AbuseContactMapper;
import net.ripe.db.whois.api.rest.marshal.StreamingHelper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Path("/test-upgrade")
public class TestSpringUpgradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSpringUpgradeService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public TestSpringUpgradeService(@Qualifier("nrtmMasterDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    //TODO [TP]: in case abuse contact is empty we should return 404 instead of 200 + empty string!
    @GET
    @Path("/retryTest")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response retry() {
        AtomicInteger retryCount = new AtomicInteger(0);

        retryTest(retryCount);

        if(retryCount.get() != 5) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "Retuy value should be 5 but " + retryCount.get()).build();
        }

        LOGGER.info("value of retry count is {}", retryCount.get());

        return Response.ok("Retry working fine").build();
    }

    @GET
    @Path("/transactionTest")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response transactionTest() {
        testTransaction();

        int count = jdbcTemplate.queryForObject("SELECT count(*) FROM key_pair where id=10", Integer.class);
        if(count > 0) {
            LOGGER.error("this should have been rolledback, id 10");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "this should have been rolledback, id 10").build();
        }

        count = jdbcTemplate.queryForObject("SELECT count(*) FROM key_pair where id=11", Integer.class);
        if(count > 0) {
            LOGGER.error("this should have been rolledback, id 11");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "this should have been rolledback, id 11").build();
        }

        return Response.ok("Transactional annotation working").build();
    }

    @GET
    @Path("/cacheTest")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cacheTest() {

        AtomicInteger cacheCount = new AtomicInteger(0);

        testCache("testName", cacheCount);
        testCache("testName", cacheCount);
        testCache("testName", cacheCount);
        testCache("testName", cacheCount);

        if(cacheCount.get() != 1) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "Caching is not working , count is {}" + cacheCount.get()).build();
        }

        return Response.ok("Caching works").build();
    }

    @RetryFor(attempts = 5, value = IllegalArgumentException.class)
    public void retryTest(AtomicInteger retryCount) {
        retryCount.incrementAndGet();
        LOGGER.info("Throwing exception to test retry");
        throw new IllegalArgumentException("testing");
    }

    @Transactional(transactionManager = "nrtmTransactionManager")
    void testTransaction() {
       try {
           saveKeyPair(10);
           saveKeyPair(11);

           //this will throw exception
           saveKeyPair(10);
       } catch(Exception ex) {
           LOGGER.error("duplicate ekey exception", ex);
       }
    }

    public void saveKeyPair(final int id) {
        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

        final String sql = """
        INSERT INTO key_pair (id, private_key, public_key, created, expires)
        VALUES (?, ?, ?, ?, ?)
        """;

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
        final long expires = dateTimeProvider.getCurrentDateTime().plusYears(1).toEpochSecond(ZoneOffset.UTC);
        jdbcTemplate.update(sql, id, privateKey, publicKey, createdTimestamp, expires);
    }
    @Cacheable(cacheNames="ssoUuid", key ="#userName")
    public String testCache(final String userName, AtomicInteger cacheCount) {
        LOGGER.info("Testing caching iside function", userName);
        cacheCount.incrementAndGet();
        return userName;
    }
}
