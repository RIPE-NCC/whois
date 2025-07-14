package net.ripe.db.whois.nrtm;

import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.scheduling.TaskScheduler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.nrtm.NrtmQueryHandlerTest.StringMatcher.instanceofString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class NrtmQueryHandlerTest {

    @Mock private SerialDao serialDaoMock;
    @Mock private DummifierNrtm dummifierMock;
    @Mock private TaskScheduler mySchedulerMock;
    @Mock private ChannelHandlerContext contextMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channelMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private Attribute attributeMock;
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

    @BeforeEach
    public void setup() {
        lenient().when(contextMock.channel()).thenReturn(channelMock);
        lenient().when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(0));
        lenient().when(channelMock.isOpen()).thenReturn(true);
        lenient().when(channelMock.writeAndFlush(any())).thenReturn(channelFutureMock);
        lenient().when(channelMock.attr(any())).thenReturn(attributeMock);
        lenient().doNothing().when(attributeMock).set(any());
        lenient().when(serialDaoMock.getSerials()).thenReturn(new SerialRange(1, 2));
        lenient().when(serialDaoMock.getByIdForNrtm(1)).thenReturn(new SerialEntry(1, Operation.UPDATE, true, 1, 1000, 1000, inetnum.toByteArray(), "one"));
        lenient().when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(true);
        lenient().when(dummifierMock.dummify(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(inetnum);
        lenient().when(serialDaoMock.getByIdForNrtm(2)).thenReturn(new SerialEntry(2, Operation.UPDATE, true, 2, 1000, 1000, person.toByteArray(), "two"));
        lenient().when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, person)).thenReturn(false);
        lenient().when(applicationVersion.getVersion()).thenReturn("1.0-SNAPSHOT");

        lenient().when(mySchedulerMock.scheduleAtFixedRate(any(Runnable.class), anyLong())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Runnable) args[0]).run();
            return null;
        });

        subject = new NrtmQueryHandler(serialDaoMock, dummifierMock, mySchedulerMock, nrtmLogMock, applicationVersion, SOURCE, NONAUTH_SOURCE, UPDATE_INTERVAL, KEEPALIVE_END_OF_STREAM);
        NrtmQueryHandler.PendingWrites.add(channelMock);
    }

    @Test
    public void gFlagWithVersion2Works() {
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());
        when(dummifierMock.isAllowed(2, person)).thenReturn(true);
        when(dummifierMock.isAllowed(2, inetnum)).thenReturn(true);
        when(dummifierMock.dummify(2, inetnum)).thenReturn(inetnum);
        when(dummifierMock.dummify(2, person)).thenReturn(DummifierNrtm.getPlaceholderPersonObject());
        String msg = "-g RIPE:2:1-2";

        subject.channelRead(contextMock, msg);

        InOrder orderedChannelMock = inOrder(channelMock);

        verify(channelMock, times(7)).writeAndFlush(argThat(instanceofString()));
        orderedChannelMock.verify(channelMock).writeAndFlush("%START Version: 2 RIPE 1-2\n\n");
        orderedChannelMock.verify(channelMock).writeAndFlush("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
        orderedChannelMock.verify(channelMock).writeAndFlush("ADD\n\n");
        orderedChannelMock.verify(channelMock).writeAndFlush(inetnum + "\n");
        orderedChannelMock.verify(channelMock).writeAndFlush("ADD\n\n");
        orderedChannelMock.verify(channelMock).writeAndFlush(DummifierNrtm.getPlaceholderPersonObject() + "\n");
        orderedChannelMock.verify(channelMock).writeAndFlush("%END RIPE\n\n");
    }

    @Test
    public void qFlagVersionArgument() {
        String msg = "-q version";

        subject.channelRead(contextMock, msg);

        verify(channelMock).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("% nrtm-server-" + VERSION + "\n\n");
    }

    @Test
    public void qFlagSourcesArgument() {
        String msg = "-q sources";

        subject.channelRead(contextMock, msg);

        verify(channelMock).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush(SOURCE + ":3:X:1-2\n\n");
    }

    @Test
    public void gFlagValidRange() {
        String msg = "-g RIPE:3:1-2";
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());
        subject.channelRead(contextMock, msg);

        verify(channelMock, times(4)).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock).writeAndFlush("ADD 1\n\n");
        verify(channelMock).writeAndFlush(inetnum.toString() + "\n");
        verify(channelMock, never()).writeAndFlush("ADD 2\n\n");
        verify(channelMock, never()).writeAndFlush(person.toString() + "\n");
        verify(channelMock).writeAndFlush("%END RIPE\n\n");
    }

    @Test
    public void keepalive() {
        String msg = "-g RIPE:3:1-LAST -k";
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());

        subject.channelRead(contextMock, msg);

        verify(channelMock, times(3)).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("%START Version: 3 RIPE 1-2\n\n");
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
        verify(channelMock).writeAndFlush("ADD 1\n\n");
        verify(channelMock).writeAndFlush(inetnum.toString() + "\n");
    }

    @Test
    public void keepaliveEndOfStreamIndicator() {
        String msg = "-g RIPE:3:1-LAST -k";
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());

        subject = new NrtmQueryHandler(serialDaoMock, dummifierMock, mySchedulerMock, nrtmLogMock, applicationVersion, SOURCE, NONAUTH_SOURCE, UPDATE_INTERVAL, true);

        subject.channelRead(contextMock, msg);

        verify(channelMock, times(4)).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("%START Version: 3 RIPE 1-2\n\n");
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
        verify(channelMock).writeAndFlush("ADD 1\n\n");
        verify(channelMock).writeAndFlush(inetnum.toString() + "\n");
        verify(channelMock).writeAndFlush("%END 1 - 2\n\n");
    }

    @Test
    public void gFlagValidRangeToLast() {
        String msg = "-g RIPE:3:1-LAST";
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());

        subject.channelRead(contextMock, msg);

        verify(channelMock, times(4)).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock).writeAndFlush("ADD 1\n\n");
        verify(channelMock).writeAndFlush(inetnum.toString() + "\n");
        verify(channelMock).writeAndFlush("%END RIPE\n\n");
    }

    @Test
    public void gFlag_InvalidRange() {
        String msg = "-g RIPE:3:4-5";

        try {
            subject.channelRead(contextMock, msg);
            fail("Didn't catch NrtmException");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("%ERROR:401: invalid range: Not within 1-2"));
        }
    }

    @Test
    public void closedChannel() {
        when(channelMock.isOpen()).thenReturn(false);
        String msg = "-g RIPE:3:1-2";

        try {
            subject.channelRead(contextMock, msg);
            fail("expected ChannelException");
        } catch (ChannelException expected) {
            verify(channelMock, atLeast(1)).isOpen();
        }
    }

    @Test
    public void gFlagRequestOutOfDateSerial() {
        when(serialDaoMock.getAgeOfExactOrNextExistingSerial(1)).thenReturn(NrtmQueryHandler.HISTORY_AGE_LIMIT + 1);
        String msg = "-g RIPE:3:1-2";

        try {
            subject.channelRead(contextMock, msg);
            fail("expected NrtmException");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("%ERROR:401: (Requesting serials older than " +
                    (NrtmQueryHandler.HISTORY_AGE_LIMIT / NrtmQueryHandler.SECONDS_PER_DAY) +
                    " days will be rejected)"));
        }
    }

    @Test
    public void gFlagDeprecatedVersion() {
        String msg = "-g RIPE:2:1-1";
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());
        subject.channelRead(contextMock, msg);

        verify(channelMock, times(3)).writeAndFlush(argThat(instanceofString()));
        verify(channelMock).writeAndFlush("%START Version: 2 RIPE 1-1\n\n");
        verify(channelMock).writeAndFlush("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
        verify(channelMock).writeAndFlush("%END RIPE\n\n");
    }

    @Test
    public void channelConnected() throws Exception {
        subject.channelActive(contextMock);

        verify(channelMock).writeAndFlush(NrtmMessages.termsAndConditions() + "\n\n");
    }

    @Test
    public void throttleChannelKeepaliveQuery() {
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());
        setPending(channelMock);
        String msg = "-g RIPE:3:1-LAST -k";

        messageReceived(msg);
        unsetPending(channelMock);

        verify(channelMock).writeAndFlush("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock, atMost(1)).writeAndFlush(any(String.class));
        verify(mySchedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong());
    }

    // TODO: [ES] slow unit test (takes ~10s)
    @Test
    public void retryForAnnotation() {
        when(serialDaoMock.getByIdForNrtm(any(Integer.class))).thenThrow(CannotGetJdbcConnectionException.class);
        when(channelMock.attr(any()).get()).thenReturn(new AtomicInteger());
        String msg = "-g RIPE:3:1-LAST";

        try {
            subject.channelRead(contextMock, msg);
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

    private void messageReceived(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                subject.channelRead(contextMock, message);
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
