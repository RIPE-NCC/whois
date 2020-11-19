package net.ripe.db.whois.nrtm;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.scheduling.TaskScheduler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.nrtm.NrtmQueryHandlerTest.StringMatcher.instanceofString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class NrtmQueryHandlerTest {

    @Mock private SerialDao serialDaoMock;
    @Mock private DummifierNrtm dummifierMock;
    @Mock private TaskScheduler mySchedulerMock;
    @Mock private ChannelHandlerContext contextMock;
    @Mock private ChannelStateEvent channelStateEventMock;
    @Mock private Channel channelMock;
    @Mock private MessageEvent messageEventMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private NrtmLog nrtmLogMock;
    @Mock private ApplicationVersion applicationVersion;

    private static final long UPDATE_INTERVAL = 1;
    private static final boolean KEEPALIVE_END_OF_STREAM = false;
    private static final String SOURCE = "RIPE";
    private static final String NONAUTH_SOURCE = "";
    private static final String VERSION = "1.0-SNAPSHOT";

    private static final RpslObject inetnum = RpslObject.parse("inetnum:10.0.0.1");
    private static final RpslObject person = RpslObject.parse("person:one\nnic-hdl:ONE");

    private NrtmQueryHandler subject;

    @Before
    public void setup() {
        when(contextMock.getChannel()).thenReturn(channelMock);
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(any())).thenReturn(channelFutureMock);
        when(serialDaoMock.getSerials()).thenReturn(new SerialRange(1, 2));
        when(serialDaoMock.getByIdForNrtm(1)).thenReturn(new SerialEntry(Operation.UPDATE, true, 1, 1000, 1000, inetnum.toByteArray()));
        when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(true);
        when(dummifierMock.dummify(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(inetnum);
        when(serialDaoMock.getByIdForNrtm(2)).thenReturn(new SerialEntry(Operation.UPDATE, true, 2, 1000, 1000, person.toByteArray()));
        when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, person)).thenReturn(false);
        when(applicationVersion.getVersion()).thenReturn("1.0-SNAPSHOT");

        when(mySchedulerMock.scheduleAtFixedRate(any(Runnable.class), anyLong())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Runnable) args[0]).run();
            return null;
        });

        subject = new NrtmQueryHandler(serialDaoMock, dummifierMock, mySchedulerMock, nrtmLogMock, applicationVersion, SOURCE, NONAUTH_SOURCE, UPDATE_INTERVAL, KEEPALIVE_END_OF_STREAM);
        NrtmQueryHandler.PendingWrites.add(channelMock);
    }

    @Test
    public void gFlagWithVersion2Works() {
        when(dummifierMock.isAllowed(2, person)).thenReturn(true);
        when(dummifierMock.isAllowed(2, inetnum)).thenReturn(true);
        when(dummifierMock.dummify(2, inetnum)).thenReturn(inetnum);
        when(dummifierMock.dummify(2, person)).thenReturn(DummifierNrtm.getPlaceholderPersonObject());

        when(messageEventMock.getMessage()).thenReturn("-g RIPE:2:1-2");

        subject.messageReceived(contextMock, messageEventMock);

        InOrder orderedChannelMock = inOrder(channelMock);

        verify(channelMock, times(7)).write(argThat(instanceofString()));
        orderedChannelMock.verify(channelMock).write("%START Version: 2 RIPE 1-2\n\n");
        orderedChannelMock.verify(channelMock).write("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
        orderedChannelMock.verify(channelMock).write("ADD\n\n");
        orderedChannelMock.verify(channelMock).write(inetnum + "\n");
        orderedChannelMock.verify(channelMock).write("ADD\n\n");
        orderedChannelMock.verify(channelMock).write(DummifierNrtm.getPlaceholderPersonObject() + "\n");
        orderedChannelMock.verify(channelMock).write("%END RIPE\n\n");
    }

    @Test
    public void qFlagVersionArgument() {
        when(messageEventMock.getMessage()).thenReturn("-q version");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock).write(argThat(instanceofString()));
        verify(channelMock).write("% nrtm-server-" + VERSION + "\n\n");
    }

    @Test
    public void qFlagSourcesArgument() {
        when(messageEventMock.getMessage()).thenReturn("-q sources");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock).write(argThat(instanceofString()));
        verify(channelMock).write(SOURCE + ":3:X:1-2\n\n");
    }

    @Test
    public void gFlagValidRange() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-2");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(4)).write(argThat(instanceofString()));
        verify(channelMock).write("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock).write("ADD 1\n\n");
        verify(channelMock).write(inetnum.toString() + "\n");
        verify(channelMock, never()).write("ADD 2\n\n");
        verify(channelMock, never()).write(person.toString() + "\n");
        verify(channelMock).write("%END RIPE\n\n");
    }

    @Test
    public void keepalive() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST -k");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(3)).write(argThat(instanceofString()));
        verify(channelMock).write("%START Version: 3 RIPE 1-2\n\n");
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
        verify(channelMock).write("ADD 1\n\n");
        verify(channelMock).write(inetnum.toString() + "\n");
    }

    @Test
    public void keepaliveEndOfStreamIndicator() {
        subject = new NrtmQueryHandler(serialDaoMock, dummifierMock, mySchedulerMock, nrtmLogMock, applicationVersion, SOURCE, NONAUTH_SOURCE, UPDATE_INTERVAL, true);

        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST -k");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(4)).write(argThat(instanceofString()));
        verify(channelMock).write("%START Version: 3 RIPE 1-2\n\n");
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
        verify(channelMock).write("ADD 1\n\n");
        verify(channelMock).write(inetnum.toString() + "\n");
        verify(channelMock).write("%END 1 - 2\n\n");
    }

    @Test
    public void gFlagValidRangeToLast() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(4)).write(argThat(instanceofString()));
        verify(channelMock).write("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock).write("ADD 1\n\n");
        verify(channelMock).write(inetnum.toString() + "\n");
        verify(channelMock).write("%END RIPE\n\n");
    }

    @Test
    public void gFlag_InvalidRange() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:4-5");

        try {
            subject.messageReceived(contextMock, messageEventMock);
            fail("Didn't catch IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("%ERROR:401: invalid range: Not within 1-2"));
        }
    }

    @Test
    public void closedChannel() {
        when(channelMock.isOpen()).thenReturn(false);
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-2");

        try {
            subject.messageReceived(contextMock, messageEventMock);
            fail("expected ChannelException");
        } catch (ChannelException expected) {
            verify(channelMock, atLeast(1)).isOpen();
        }
    }

    @Test
    public void gFlagRequestOutOfDateSerial() {
        when(serialDaoMock.getAgeOfExactOrNextExistingSerial(1)).thenReturn(NrtmQueryHandler.HISTORY_AGE_LIMIT + 1);
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-2");

        try {
            subject.messageReceived(contextMock, messageEventMock);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("%ERROR:401: (Requesting serials older than " +
                    (NrtmQueryHandler.HISTORY_AGE_LIMIT / NrtmQueryHandler.SECONDS_PER_DAY) +
                    " days will be rejected)"));
        }
    }

    @Test
    public void gFlagDeprecatedVersion() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:2:1-1");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(3)).write(argThat(instanceofString()));
        verify(channelMock).write("%START Version: 2 RIPE 1-1\n\n");
        verify(channelMock).write("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
        verify(channelMock).write("%END RIPE\n\n");
    }

    @Test
    public void channelConnected() throws Exception {
        subject.channelConnected(contextMock, channelStateEventMock);

        verify(channelMock).write(NrtmMessages.termsAndConditions() + "\n\n");
    }

    @Test
    public void throttleChannelKeepaliveQuery() {
        setPending(channelMock);
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST -k");

        messageReceived();
        unsetPending(channelMock);

        verify(channelMock).write("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock, atMost(1)).write(any(String.class));
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
    }

    // TODO: [ES] slow unit test (takes ~10s)
    @Test
    public void retryForAnnotation() {
        when(serialDaoMock.getByIdForNrtm(any(Integer.class))).thenThrow(CannotGetJdbcConnectionException.class);
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST");

        try {
            subject.messageReceived(contextMock, messageEventMock);
            fail();
        } catch (CannotGetJdbcConnectionException e) {
            verify(serialDaoMock, times(10)).getByIdForNrtm(1);
        }
    }

    private void setPending(final Channel channelMock) {
        NrtmQueryHandler.PendingWrites.add(channelMock);
        while (!NrtmQueryHandler.PendingWrites.isPending(channelMock)) {
            NrtmQueryHandler.PendingWrites.increment(channelMock);
        }
    }

    private void unsetPending(final Channel channelMock) {
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        NrtmQueryHandler.PendingWrites.decrement(channelMock);
    }

    private void messageReceived() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                subject.messageReceived(contextMock, messageEventMock);
            }
        }).start();
    }

    /**
     * Check that an argument is an instanceof String.
     * any(String.class) is also matched by Object.class, if the method accepts Object.
     */
    static class StringMatcher implements ArgumentMatcher<Object> {

        static final StringMatcher instance = new StringMatcher();

        @Override
        public boolean matches(final Object argument) {
            return (argument instanceof String);
        }

        static StringMatcher instanceofString() {
            return instance;
        }
    }
}
