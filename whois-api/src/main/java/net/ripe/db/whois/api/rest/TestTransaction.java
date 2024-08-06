package net.ripe.db.whois.api.rest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Path("/test-upgrade")
public class TestTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTransaction.class);

    private final JdbcTemplate jdbcTemplate;
    private final TestNrtmTransactionDao testNrtmTransactionDao;

    @Autowired
    public TestTransaction(@Qualifier("nrtmMasterDataSource") final DataSource dataSource, final TestNrtmTransactionDao testNrtmTransactionDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.testNrtmTransactionDao = testNrtmTransactionDao;
    }

    @GET
    @Path("/transactionNrtmv4")
    public Response transactionNrtmv4() {

        int maxId = testNrtmTransactionDao.getMaxId();
        try {
            testNrtmTransactionDao.testTransaction(maxId);
        } catch (Exception e) {
            LOGGER.info("error occurred {}",e);
        }

        return getResponse(maxId);
    }

    private Response getResponse(int maxId) {
        int count = testNrtmTransactionDao.getKeyForId(maxId + 1);
        if(count > 0) {
            LOGGER.error("this should have been rolledback, id {}", maxId);
        }

        count = testNrtmTransactionDao.getKeyForId(maxId + 2);
        if(count > 0) {
            LOGGER.error("this should have been rolledback, id {}", maxId + 1);
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "this should have been rolledback").build();
        }

        return Response.ok("Transactional annotation working").build();
    }
}
