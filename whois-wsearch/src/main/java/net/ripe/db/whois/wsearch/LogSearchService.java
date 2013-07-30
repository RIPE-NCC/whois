package net.ripe.db.whois.wsearch;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.common.domain.Hosts;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * Search log files.
 */
@ExternallyManagedLifecycle
@Component
@Path("/logs")
public class LogSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSearchService.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");

    private static final int CLUSTER_TIMEOUT = 10000;

    private final Hosts host = Hosts.getLocalHost();
    private final LogFileSearch logFileSearch;
    private final Client client;
    private final int streamResultsLimit;

    @Autowired
    public LogSearchService(
            final LogFileSearch logFileSearch,
            @Value("${wsearch.result.limit}") final int streamResultLimit) {
        this.logFileSearch = logFileSearch;
        this.streamResultsLimit = streamResultLimit;

        final ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJaxbJsonProvider.class);
        client = Client.create(cc);
        client.setConnectTimeout(CLUSTER_TIMEOUT);
        client.setReadTimeout(CLUSTER_TIMEOUT);
    }

    @GET
    public Response getUpdates(
            @QueryParam("search") final String search,
            @DefaultValue("") @QueryParam("todate") final String toDate,
            @DefaultValue("") @QueryParam("fromdate") final String fromDate) throws IOException {

        final List<Update> updateIds = getUpdateIds(search, toDate, fromDate);
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    final Writer writer = new BufferedWriter(new OutputStreamWriter(output, Charsets.UTF_8));
                    if (updateIds.size() > streamResultsLimit) {
                        writer.write(String.format("!!! Found %s update logs, limiting to %s", updateIds.size(), streamResultsLimit));
                    } else {
                        writer.write(String.format("*** Found %s update log(s)", updateIds.size()));
                    }

                    int count = 1;
                    for (final Update updateId : updateIds) {
                        writer.write(String.format("\n\n*** %03d ***\n\n%s %s\n\n", count, updateId.getHost(), updateId.getId()));
                        final String path = updateId.getPath();
                        logFileSearch.writeLoggedUpdates(LoggedUpdateId.parse(updateId.getId(), path), writer);

                        writer.flush();
                        if (++count > streamResultsLimit) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                } catch (RuntimeException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).build();
    }

    /**
     * Search for updates on all machines based on query string
     *
     * @param search The search query
     * @param fromDate   date the updates was handled or lower part of a range
     * @param toDate   uppder part of a range
     * @return List of updateIds matching search query
     */
    @GET
    @Path("/ids")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Update.class)
    public List<Update> getUpdateIds(
            @QueryParam("search") final String search,
            @DefaultValue("") @QueryParam("fromdate") final String fromDate,
            @DefaultValue("") @QueryParam("todate") final String toDate) throws IOException {

        try {
            final LocalDate localDateFrom = StringUtils.isEmpty(fromDate) ? null : DATE_FORMAT.parseLocalDate(fromDate);
            final LocalDate localDateTo = StringUtils.isEmpty(toDate) ? null : DATE_FORMAT.parseLocalDate(toDate);

            final Set<LoggedUpdateId> updateIds = logFileSearch.searchLoggedUpdateIds(search, localDateFrom, localDateTo);
            final List<Update> result = Lists.newArrayListWithExpectedSize(updateIds.size());
            for (final LoggedUpdateId updateId : updateIds) {
                result.add(new Update(host.name(), updateId.toString(), updateId.getFullPathToLogFolder()));
            }

            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid query: %s", search));
        }
    }

    /**
     * Get the logged update on the local machine with the specified id
     *
     * @param updateId The update id
     * @return Plain text representation of logged files
     */
    @GET
    @Path("/current/{updateId}")
    @Produces({MediaType.TEXT_PLAIN})
    @TypeHint(String.class)
    public Response getUpdateLogs(
            @PathParam("updateId") final String updateId) throws IOException {
        return getUpdateLogs(LoggedUpdateId.parse(updateId));
    }

    /**
     * Get the logged update with the specified date and id.
     * <p/>
     * This is a convenience function because the updateId contains a "/" character
     *
     * @param date     The date in the format YYMMDDDD
     * @param updateId The update id.
     * @return Plain text representation of logged files
     */
    @GET
    @Path("/current/{date}/{updateId}")
    @Produces({MediaType.TEXT_PLAIN})
    @TypeHint(String.class)
    public Response getUpdateLogs(
            @PathParam("date") final String date,
            @PathParam("updateId") final String updateId) throws IOException {
        return getUpdateLogs(new LoggedUpdateId(date, updateId));
    }

    private Response getUpdateLogs(final LoggedUpdateId loggedUpdateId) {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                try {
                    final Writer writer = new BufferedWriter(new OutputStreamWriter(output, Charsets.UTF_8));
                    logFileSearch.writeLoggedUpdates(loggedUpdateId, writer);
                    writer.flush();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }
                catch (RuntimeException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).build();
    }
}
