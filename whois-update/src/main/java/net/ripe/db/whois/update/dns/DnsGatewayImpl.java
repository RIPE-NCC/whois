package net.ripe.db.whois.update.dns;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@DeployedProfile
@Primary
@Component
class DnsGatewayImpl implements DnsGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(DnsGatewayImpl.class);

    private static final int DEFAULT_TIMEOUT = 5 * 60 * 1000;

    private static final Map<String, Messages.Type> ERRORLEVEL_CONVERSION = ImmutableMap.of(
            "ERROR", Messages.Type.ERROR,
            "CRITICAL", Messages.Type.ERROR,
            "WARNING", Messages.Type.WARNING);

    private final JdbcTemplate jdbcTemplate;
    private int timeout = DEFAULT_TIMEOUT;

    @Autowired
    public DnsGatewayImpl(@Qualifier("dnscheckDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        final Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();

        final Map<DnsCheckRequest, String> dnsRequestsPerProcessId = Maps.newHashMap();
        for (DnsCheckRequest dnsCheckRequest : dnsCheckRequests) {
            try {
                dnsRequestsPerProcessId.put(dnsCheckRequest, queueDnsCheck(dnsCheckRequest));
            } catch (IllegalStateException e) {
                dnsResults.put(dnsCheckRequest, new DnsCheckResponse(UpdateMessages.dnsCheckMessageParsingError()));
            }
        }

        for (Map.Entry<DnsCheckRequest, String> dnsRequestPerProcessId : dnsRequestsPerProcessId.entrySet()) {
            final DnsCheckRequest dnsCheckRequest = dnsRequestPerProcessId.getKey();
            try {
                final DnsResult dnsResult = pollResult(dnsRequestPerProcessId.getValue());
                dnsResults.put(dnsCheckRequest, getDnsResponseForPollResult(dnsResult));
            } catch (IllegalStateException e) {
                dnsResults.put(dnsCheckRequest, new DnsCheckResponse(UpdateMessages.dnsCheckMessageParsingError()));
            }
        }

        return dnsResults;
    }

    private DnsCheckResponse getDnsResponseForPollResult(final DnsResult dnsResult) {
        if (dnsResult == null) {
            return new DnsCheckResponse(UpdateMessages.dnsCheckTimeout());
        } else if (dnsResult.nrErrors > 0) {
            try {
                return new DnsCheckResponse(getMessages(dnsResult));
            } catch (IllegalStateException e) {
                LOGGER.error(String.format("Error in retrieving messages during DNS check for test_id (dnsResult.id): %s", dnsResult.id), e);
                return new DnsCheckResponse(UpdateMessages.dnsCheckMessageParsingError());
            }
        }
        return new DnsCheckResponse();
    }

    private List<Message> getMessages(final DnsResult dnsResult) {
        try {
            return jdbcTemplate.query(
                    "SELECT message,LEVEL,formatstring,description,arg0,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9 " +
                    "FROM (" +
                    "  SELECT * " +
                    "  FROM results " +
                    "  WHERE test_id = ? " +
                    "  AND LEVEL IN ('ERROR', 'CRITICAL', 'WARNING') " +
                    ") AS tmp " +
                    "LEFT JOIN messages ON tmp.message = messages.tag " +
                    "ORDER BY tmp.id ASC",
                    new RowMapper<Message>() {
                        @Override
                        public Message mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                            final StringBuilder messageBuilder = new StringBuilder();

                            final String formatstring = rs.getString("formatstring");
                            if (StringUtils.isNotBlank(formatstring)) {
                                messageBuilder.append(String.format(
                                        formatstring,
                                        rs.getObject("arg0"),
                                        rs.getObject("arg1"),
                                        rs.getObject("arg2"),
                                        rs.getObject("arg3"),
                                        rs.getObject("arg4"),
                                        rs.getObject("arg5"),
                                        rs.getObject("arg6"),
                                        rs.getObject("arg7"),
                                        rs.getObject("arg8"),
                                        rs.getObject("arg9")));
                            } else {
                                throw new IllegalStateException(String.format("Failed to parse message tag: '%s' for test_id: %s", rs.getString("message"), dnsResult.id));
                            }

                            final String description = rs.getString("description");
                            if (StringUtils.isNotEmpty(description)) {
                                messageBuilder.append("\n\n").append(description);
                            }

                            return new Message(ERRORLEVEL_CONVERSION.get(rs.getString("level")), messageBuilder.toString());
                        }
                    },
                    dnsResult.id);
        } catch (DataAccessException e) {
            throw new IllegalStateException("dnscheck get messages failed", e);
        }
    }

    private String queueDnsCheck(final DnsCheckRequest dnsCheckRequest) {
        final String processId = "RDP-" + UUID.randomUUID().toString();
        try {
            final int rows = jdbcTemplate.update(
                    "INSERT INTO queue (DOMAIN, priority, source_id, source_data, fake_parent_glue) " +
                    "VALUES (?, 1, 2, ?, ?) ",
                    dnsCheckRequest.getDomain(),
                    processId,
                    dnsCheckRequest.getGlue());
            Validate.isTrue(rows == 1);
            return processId;
        } catch (DataAccessException e) {
            LOGGER.error("dnscheck queue failed", e);
            throw new IllegalStateException("dnscheck queue failed", e);
        }
    }

    @Nullable
    private DnsResult pollResult(final String processId) {
        final long maxTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < maxTime) {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);


            final DnsResult dnsResult;
            try {
                dnsResult = jdbcTemplate.query(
                    "SELECT id, END, count_critical, count_error " +
                    "FROM tests " +
                    "WHERE source_id = 2 " +
                    "AND source_data = ?",
                    rs -> {
                        if (rs.next() && rs.getTimestamp("end") != null) {
                            return new DnsResult(rs.getInt("id"), rs.getInt("count_critical") + rs.getInt("count_error"));
                        } else {
                            return null;
                        }
                    },
                    processId);
            } catch (DataAccessException e) {
                LOGGER.error("dnscheck poll result failed", e);
                throw new IllegalStateException("dnscheck poll result failed", e);
            }

            if (dnsResult != null) {
                return dnsResult;
            }
        }

        return null;
    }

    private static final class DnsResult {
        private int id;
        private int nrErrors;

        private DnsResult(final int id, final int nrErrors) {
            this.id = id;
            this.nrErrors = nrErrors;
        }
    }
}
