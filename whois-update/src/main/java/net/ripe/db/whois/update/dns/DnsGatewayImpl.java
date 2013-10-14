package net.ripe.db.whois.update.dns;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@DeployedProfile
@Primary
@Component
class DnsGatewayImpl implements DnsGateway {
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
        Map<DnsCheckRequest, String> dnsRequestsPerProcessId = Maps.newHashMap();
        for (DnsCheckRequest dnsCheckRequest : dnsCheckRequests) {
            dnsRequestsPerProcessId.put(dnsCheckRequest, queueDnsCheck(dnsCheckRequest));
        }

        Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();
        for (Map.Entry<DnsCheckRequest, String> dnsRequestPerProcessId : dnsRequestsPerProcessId.entrySet()) {
            final DnsResult dnsResult = pollResult(dnsRequestPerProcessId.getValue());
            dnsResults.put(dnsRequestPerProcessId.getKey(), getDnsResponseForPollResult(dnsResult));
        }

        return dnsResults;
    }

    private DnsCheckResponse getDnsResponseForPollResult(final DnsResult dnsResult) {
        if (dnsResult == null) {
            return new DnsCheckResponse(UpdateMessages.dnsCheckTimeout());
        } else if (dnsResult.nrErrors > 0) {
            return new DnsCheckResponse(getMessages(dnsResult));
        }

        return new DnsCheckResponse();
    }

    @Override
    public DnsCheckResponse performDnsCheck(final DnsCheckRequest dnsCheckRequest) {
        final String processId = queueDnsCheck(dnsCheckRequest);
        final DnsResult dnsResult = pollResult(processId);

        return getDnsResponseForPollResult(dnsResult);
    }

    private List<Message> getMessages(final DnsResult dnsResult) {
        return jdbcTemplate.query("" +
                "SELECT LEVEL,formatstring,description,arg0,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9 " +
                "FROM (" +
                "  SELECT * " +
                "  FROM results " +
                "  WHERE test_id=? " +
                "  AND LEVEL IN ('ERROR', 'CRITICAL', 'WARNING') " +
                ") AS tmp " +
                "LEFT JOIN messages ON tmp.message = messages.tag " +
                "ORDER BY tmp.id ASC",
                new RowMapper<Message>() {
                    @Override
                    public Message mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        final StringBuilder messageBuilder = new StringBuilder(String.format(
                                rs.getString("formatstring"),
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

                        final String description = rs.getString("description");
                        if (StringUtils.isNotEmpty(description)) {
                            messageBuilder.append("\n\n").append(description);
                        }

                        return new Message(ERRORLEVEL_CONVERSION.get(rs.getString("level")), messageBuilder.toString());
                    }
                }, dnsResult.id);
    }

    private String queueDnsCheck(final DnsCheckRequest dnsCheckRequest) {
        final String processId = "RDP-" + UUID.randomUUID().toString();

        final int rows = jdbcTemplate.update("" +
                "INSERT INTO queue (DOMAIN, priority, source_id, source_data, fake_parent_glue) " +
                "VALUES (?, 1, 2, ?, ?) ",
                dnsCheckRequest.getDomain(),
                processId,
                dnsCheckRequest.getGlue());
        Validate.isTrue(rows == 1);
        return processId;
    }

    private DnsResult pollResult(final String processId) {
        final long maxTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < maxTime) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);
            }

            final DnsResult dnsResult = jdbcTemplate.query("" +
                    "SELECT id, END, count_critical, count_error " +
                    "FROM tests " +
                    "WHERE source_id=2 AND source_data=?",
                    new ResultSetExtractor<DnsResult>() {
                        @Override
                        public DnsResult extractData(final ResultSet rs) throws SQLException, DataAccessException {
                            if (rs.next() && rs.getTimestamp("end") != null) {
                                return new DnsResult(rs.getInt("id"), rs.getInt("count_critical") + rs.getInt("count_error"));
                            }

                            return null;
                        }
                    },
                    processId);

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
