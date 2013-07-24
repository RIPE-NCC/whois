package net.ripe.db.whois.wsearch;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
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
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private static final int STREAM_RESULTS_LIMIT = 100;

    private final WSearchJettyConfig jettyConfig;
    private final Hosts host = Hosts.getLocalHost();
    private final LogFileSearch logFileSearch;
    private final String apiKey;
    private final Client client;

    @Autowired
    public LogSearchService(
            final WSearchJettyConfig jettyConfig,
            final LogFileSearch logFileSearch,
            @Value("${api.key}") final String apiKey) {
        this.jettyConfig = jettyConfig;
        this.logFileSearch = logFileSearch;
        this.apiKey = apiKey;

        final ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJaxbJsonProvider.class);
        client = Client.create(cc);
        client.setConnectTimeout(CLUSTER_TIMEOUT);
        client.setReadTimeout(CLUSTER_TIMEOUT);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUpdates(
            @QueryParam("search") final String search,
            @DefaultValue("") @QueryParam("date") final String date) throws IOException {

        final List<Update> updateIds = getUpdateIds(search, date);
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                try {
                    final Writer writer = new BufferedWriter(new OutputStreamWriter(output, Charsets.UTF_8));
                    if (updateIds.size() > 100) {
                        writer.write(String.format("!!! Found %s update logs, limiting to %s", updateIds.size(), STREAM_RESULTS_LIMIT));
                    } else {
                        writer.write(String.format("*** Found %s update log(s)", updateIds.size()));
                    }

                    int count = 1;
                    for (final Update updateId : updateIds) {
                        writer.write(String.format("\n\n*** %03d ***\n\n%s %s\n\n", count, updateId.getHost(), updateId.getId()));

                        if (host.name().equals(updateId.getHost())) {
                            logFileSearch.writeLoggedUpdates(LoggedUpdateId.parse(updateId.getId()), writer);
                        } else {
                            writer.write(getRemoteUpdateLogs(updateId.getHost(), updateId.getId()));
                        }

                        writer.flush();
                        if (++count > STREAM_RESULTS_LIMIT) {
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
     * @param date   date the updates was handled
     * @return List of updateIds matching search query
     */
    @GET
    @Path("/ids")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Update.class)
    public List<Update> getUpdateIds(
            @QueryParam("search") final String search,
            @DefaultValue("") @QueryParam("date") final String date) throws IOException {

        final List<Update> updates = getCurrentUpdateIds(search, date);

        final List<Hosts> clusterMembers = host.getClusterMembers();
        if (clusterMembers.isEmpty()) {
            return updates;
        }

        final Map<Hosts, Future<List<Update>>> futures = Maps.newEnumMap(Hosts.class);
        for (final Hosts clusterMember : clusterMembers) {
            if (clusterMember.equals(host)) {
                continue;
            }

            final String url = String.format("http://%s:%s/api/logs/current?search=%s&date=%s&apiKey=%s",
                    clusterMember.getHostName(),
                    jettyConfig.getPort(),
                    URLEncoder.encode(search, "ISO-8859-1"),
                    date, apiKey);
            final Future<List<Update>> future = client.asyncResource(url)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<Update>>() {
                    });

            futures.put(clusterMember, future);
        }

        for (final Map.Entry<Hosts, Future<List<Update>>> futureEntry : futures.entrySet()) {
            final Hosts host = futureEntry.getKey();
            final Future<List<Update>> future = futureEntry.getValue();
            try {
                updates.addAll(future.get(CLUSTER_TIMEOUT, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                updates.add(new Update(host.name(), remoteError(host.name(), e)));
            } catch (ExecutionException e) {
                updates.add(new Update(host.name(), remoteError(host.name(), e)));
            } catch (TimeoutException e) {
                updates.add(new Update(host.name(), remoteError(host.name(), e)));
            }
        }

        Collections.sort(updates, new Comparator<Update>() {
            @Override
            public int compare(final Update update1, final Update update2) {
                return update1.getId().compareTo(update2.toString());
            }
        });

        return updates;
    }

    private String remoteError(final String msg, final Exception e) {
        LOGGER.error("Remote error", e);
        return String.format("%s FAILED: %s", msg, e.getMessage());
    }

    /**
     * Get the logged update on the remote machine with the specified id
     *
     * @param host     The name of the host where the update is logged
     * @param updateId The update id
     * @return Plain text representation of logged files
     */
    @GET
    @Path("/{host}/{updateId}")
    @Produces({MediaType.TEXT_PLAIN})
    public String getRemoteUpdateLogs(
            @PathParam("host") final String host,
            @PathParam("updateId") final String updateId) throws IOException {
        return getRemoteUpdateLogs(host, LoggedUpdateId.parse(updateId));
    }

    /**
     * Get the logged update on the remote machine with the specified date and id
     * <p/>
     * This is a convenience function because the updateId contains a "/" character
     *
     * @param date     The date in the format YYMMDDDD
     * @param updateId The update id.
     * @return Plain text representation of logged files
     */
    @GET
    @Path("/{host}/{date}/{updateId}")
    @Produces({MediaType.TEXT_PLAIN})
    @TypeHint(String.class)
    public String getRemoteUpdateLogs(
            @PathParam("host") final String host,
            @PathParam("date") final String date,
            @PathParam("updateId") final String updateId) throws IOException {

        return getRemoteUpdateLogs(host, new LoggedUpdateId(date, updateId));
    }

    private String getRemoteUpdateLogs(final String h, final LoggedUpdateId loggedUpdateId) {
        final Hosts host = Hosts.valueOf(h);
        if (Hosts.UNDEFINED.equals(host)) {
            return "";
        }

        final String url = String.format("http://%s:%s/api/logs/current/%s/%s?apiKey=%s",
                host.getHostName(),
                jettyConfig.getPort(),
                loggedUpdateId.getDailyLogFolder(),
                loggedUpdateId.getUpdateFolder(),
                apiKey);

        try {
            final String response = client.resource(url).accept(MediaType.TEXT_PLAIN).get(String.class);
            return response;
        } catch (RuntimeException e) {
            return remoteError(url, e);
        }
    }

    /**
     * Search for updates on the local machine based on query string
     *
     * @param date   date the updates was handled
     * @param search The search query
     * @return List of updateIds matching search query
     */
    @GET
    @Path("/current")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @TypeHint(Update.class)
    public List<Update> getCurrentUpdateIds(
            @QueryParam("search") final String search,
            @DefaultValue("") @QueryParam("date") final String date) throws IOException {

        try {
            final LocalDate localDate = StringUtils.isEmpty(date) ? null : DATE_FORMAT.parseLocalDate(date);

            final Set<LoggedUpdateId> updateIds = logFileSearch.searchLoggedUpdateIds(search, localDate);
            final List<Update> result = Lists.newArrayListWithExpectedSize(updateIds.size());
            for (final LoggedUpdateId updateId : updateIds) {
                result.add(new Update(host.name(), updateId.toString()));
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
