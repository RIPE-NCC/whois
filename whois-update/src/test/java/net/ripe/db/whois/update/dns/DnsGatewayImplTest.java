package net.ripe.db.whois.update.dns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoTest;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DnsGatewayImplTest extends AbstractUpdateDaoTest {
    private DnsGatewayImpl subject;
    private JdbcTemplate dnscheckTemplate;
    private DnsCheckRequest dnsCheckRequest;

    @Before
    public void setup() throws Exception {
        subject = new DnsGatewayImpl(databaseHelper.getDnsCheckDataSource());
        dnscheckTemplate = new JdbcTemplate(databaseHelper.getDnsCheckDataSource());
        truncateTables(dnscheckTemplate);
        dnsCheckRequest = new DnsCheckRequest("domain", "ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10");
    }

    @Test
    public void performDnsCheck_timeout() {
        subject.setTimeout(0);

        final DnsCheckResponse dnsCheckResponse = subject.performDnsCheck(dnsCheckRequest);
        assertThat(dnsCheckResponse.getMessages(), contains(UpdateMessages.dnsCheckTimeout()));
    }

    @Test
    public void performDnsChecks() {
        subject.setTimeout(1000);

        Set<DnsCheckRequest> dnsCheckRequests = Sets.newLinkedHashSet();
        dnsCheckRequests.add(dnsCheckRequest);
        dnsCheckRequests.add(new DnsCheckRequest("domain", "ns3.test.se/80.84.32.14 ns4.test.se/80.84.32.16"));
        dnsCheckRequests.add(new DnsCheckRequest("domain", "ns5.test.se/80.84.32.18 ns6.test.se/80.84.32.20"));

        final Map<DnsCheckRequest, DnsCheckResponse> dnsResults = subject.performDnsChecks(dnsCheckRequests);

        assertThat(dnsResults.values(), hasSize(3));
        assertThat(dnsResults.get(dnsCheckRequest).getMessages(), contains(UpdateMessages.dnsCheckTimeout()));
        assertThat(dnsResults.get(new DnsCheckRequest("domain", "ns3.test.se/80.84.32.14 ns4.test.se/80.84.32.16")).getMessages(), contains(UpdateMessages.dnsCheckTimeout()));
        assertThat(dnsResults.get(new DnsCheckRequest("domain", "ns5.test.se/80.84.32.18 ns6.test.se/80.84.32.20")).getMessages(), contains(UpdateMessages.dnsCheckTimeout()));
    }

    @Test
    public void singleErrorReturned() throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new DnsCheckStub(1, 0, ImmutableList.of(new TestMessage("ERROR", "This %sis%s.", "this%dis awesome.", "fruit ", "4"))), 100, 100, TimeUnit.MILLISECONDS);

        final DnsCheckResponse dnsCheckResponse = subject.performDnsCheck(dnsCheckRequest);
        executorService.shutdown();

        final List<Message> messages = dnsCheckResponse.getMessages();
        assertThat(messages, hasSize(1));
        assertThat(messages.get(0).toString(), is("This fruit is4.\n\nthis%dis awesome."));
        assertThat(messages.get(0).getType(), is(Messages.Type.ERROR));
    }

    @Test
    public void warningAndCriticalMessageReturned() throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new DnsCheckStub(1, 1, ImmutableList.of(new TestMessage("CRITICAL", "This is %s.", "This is description", "critical", null), new TestMessage("WARNING", "This %sis%s.", "this%dis awesome.", "fruit ", "4"))), 100, 100, TimeUnit.MILLISECONDS);

        final DnsCheckResponse dnsCheckResponse = subject.performDnsCheck(dnsCheckRequest);
        executorService.shutdown();

        final List<Message> messages = dnsCheckResponse.getMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.get(0).toString(), is("This is critical.\n\nThis is description"));
        assertThat(messages.get(0).getType(), is(Messages.Type.ERROR));

        assertThat(messages.get(1).toString(), is("This fruit is4.\n\nthis%dis awesome."));
        assertThat(messages.get(1).getType(), is(Messages.Type.WARNING));
    }

    private class DnsCheckStub implements Runnable {
        AtomicInteger resultsCounter = new AtomicInteger(1);
        final int count_error;
        final int count_critical;
        final List<TestMessage> messages;

        private DnsCheckStub(int count_error, int count_critical, List<TestMessage> messages) {
            this.count_error = count_error;
            this.count_critical = count_critical;
            this.messages = messages;
        }

        @Override
        public void run() {
            try {
                Map<String, Object> queue = dnscheckTemplate.queryForMap("SELECT DOMAIN, priority, source_id, source_data, fake_parent_glue FROM queue LIMIT 1");
                dnscheckTemplate.update("DELETE FROM queue WHERE source_data = ?", queue.get("source_data"));
                for (TestMessage testMessage : messages) {
                    final String s = "test" + resultsCounter.getAndIncrement();
                    dnscheckTemplate.update("INSERT INTO results (test_id, message, LEVEL, arg0, arg1) VALUES (1, ?, ?, ?, ?)", s, testMessage.getLevel(), testMessage.getArg0(), testMessage.getArg1());
                    dnscheckTemplate.update("INSERT INTO messages (tag, formatstring, description) VALUES (?, ?, ?)", s, testMessage.getFormatString(), testMessage.getDescription());
                }
                dnscheckTemplate.update("" +
                        "INSERT INTO tests (id, END, count_critical, count_error, source_id, source_data) " +
                        "VALUES (1, 1001, ?, ?, 2, ?)",
                        count_critical, count_error, queue.get("source_data"));

            } catch (EmptyResultDataAccessException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class TestMessage {
        final String level, formatString, description, arg0, arg1;

        private TestMessage(String level, String formatString, String description, String arg0, String arg1) {
            this.level = level;
            this.formatString = formatString;
            this.description = description;
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        public String getLevel() {
            return level;
        }

        public String getFormatString() {
            return formatString;
        }

        public String getDescription() {
            return description;
        }

        public String getArg0() {
            return arg0;
        }

        public String getArg1() {
            return arg1;
        }
    }
}
