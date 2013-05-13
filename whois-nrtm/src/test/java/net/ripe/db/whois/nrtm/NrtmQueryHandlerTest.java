package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.TaskScheduler;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class NrtmQueryHandlerTest {

    @Mock private SerialDao serialDaoMock;
    @Mock private DummifierLegacy dummifierMock;
    @Mock private TaskScheduler mySchedulerMock;
    @Mock private ChannelHandlerContext contextMock;
    @Mock private ChannelStateEvent channelStateEventMock;
    @Mock private Channel channelMock;
    @Mock private MessageEvent messageEventMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private NrtmLog nrtmLogMock;

    private static final long UPDATE_INTERVAL = 1;
    private static final String SOURCE = "RIPE";
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

        when(serialDaoMock.getById(1)).thenReturn(new SerialEntry(Operation.UPDATE, true, 1, 1000, 1000, inetnum.toByteArray()));
        when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(true);
        when(dummifierMock.dummify(NrtmServer.NRTM_VERSION, inetnum)).thenReturn(inetnum);
        when(serialDaoMock.getById(2)).thenReturn(new SerialEntry(Operation.UPDATE, true, 2, 1000, 1000, person.toByteArray()));
        when(dummifierMock.isAllowed(NrtmServer.NRTM_VERSION, person)).thenReturn(false);

        when(mySchedulerMock.scheduleAtFixedRate(any(Runnable.class), anyLong())).thenAnswer(new Answer<ScheduledFuture<?>>() {
            @Override
            public ScheduledFuture<?> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((Runnable) args[0]).run();
                return null;
            }
        });

        subject = new NrtmQueryHandler(serialDaoMock, dummifierMock, mySchedulerMock, nrtmLogMock, VERSION, SOURCE, UPDATE_INTERVAL);
        subject.PENDING_WRITES.set(channelMock, new AtomicInteger());
    }

    @Test
    public void gFlagWithVersion2Works() {
        when(dummifierMock.isAllowed(2, person)).thenReturn(true);
        when(dummifierMock.isAllowed(2, inetnum)).thenReturn(true);
        when(dummifierMock.dummify(2, inetnum)).thenReturn(inetnum);
        when(dummifierMock.dummify(2, person)).thenReturn(DummifierLegacy.PLACEHOLDER_PERSON_OBJECT);

        when(messageEventMock.getMessage()).thenReturn("-g RIPE:2:1-2");

        subject.messageReceived(contextMock, messageEventMock);

        InOrder orderedChannelMock = inOrder(channelMock);

        orderedChannelMock.verify(channelMock).write("%START Version: 2 RIPE 1-2\n\n");
        orderedChannelMock.verify(channelMock).write("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
        orderedChannelMock.verify(channelMock).write("ADD\n\n");
        orderedChannelMock.verify(channelMock).write(inetnum + "\n");
        orderedChannelMock.verify(channelMock).write("ADD\n\n");
        orderedChannelMock.verify(channelMock).write(DummifierLegacy.PLACEHOLDER_PERSON_OBJECT + "\n");
    }

    @Test
    public void qFlagVersionArgument() {
        when(messageEventMock.getMessage()).thenReturn("-q version");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write("% nrtm-server-" + VERSION + "\n\n");
    }

    @Test
    public void qFlagSourcesArgument() {
        when(messageEventMock.getMessage()).thenReturn("-q sources");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write(SOURCE + ":3:X:1-2\n\n");
    }

    @Test
    public void gFlagValidRange() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-2");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock, times(1)).write("ADD 1\n\n");
        verify(channelMock, times(1)).write(inetnum.toString() + "\n");
        verify(channelMock, times(0)).write("ADD 2\n\n");
        verify(channelMock, times(0)).write(person.toString() + "\n");
        verify(channelMock, times(1)).write("%END RIPE\n\n");
    }

    @Test
    public void keepalive() throws Exception {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST -k");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write("%START Version: 3 RIPE 1-2\n\n");
        verify(mySchedulerMock, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong());
        verify(channelMock, times(1)).write("ADD 1\n\n");
        verify(channelMock, times(1)).write(inetnum.toString() + "\n");
    }

    @Test
    public void gFlagValidRangeToLast() {
        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write("ADD 1\n\n");
        verify(channelMock, times(1)).write(inetnum.toString() + "\n");
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
        when(serialDaoMock.getSerialAge(1)).thenReturn(NrtmQueryHandler.HISTORY_AGE_LIMIT + 1);
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

        verify(channelMock, times(1)).write("%WARNING: NRTM version 2 is deprecated, please consider migrating to version 3!\n\n");
    }

    @Test
    public void channelConnected() throws Exception {
        subject.channelConnected(contextMock, channelStateEventMock);

        verify(channelMock, times(1)).write(NrtmQueryHandler.TERMS_AND_CONDITIONS + "\n\n");
    }

    @Test
    public void throttleChannelKeepaliveQuery() {
        subject.PENDING_WRITES.set(channelMock, new AtomicInteger(subject.MAX_PENDING_WRITES + 1));

        when(messageEventMock.getMessage()).thenReturn("-g RIPE:3:1-LAST -k");

        subject.messageReceived(contextMock, messageEventMock);

        verify(channelMock, times(1)).write("%START Version: 3 RIPE 1-2\n\n");
        verify(channelMock, atMost(1)).write(any(String.class));
        verify(mySchedulerMock, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong());
    }
}
