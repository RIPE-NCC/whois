package net.ripe.db.whois.nrtm;

import com.google.common.util.concurrent.Uninterruptibles;
import joptsimple.OptionException;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NrtmQueryHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmQueryHandler.class);

    static final int SECONDS_PER_DAY = 60 * 60 * 24;
    static final int HISTORY_AGE_LIMIT = 14 * SECONDS_PER_DAY;
    static final int MAX_PENDING_WRITES = 16;

    private final SerialDao serialDao;
    private final Dummifier dummifier;
    private final TaskScheduler clientSynchronisationScheduler;

    private final NrtmLog nrtmLog;
    private final String applicationVersion;
    private final String source;
    private final long updateInterval;

    static final ChannelLocal<AtomicInteger> PENDING_WRITES = new ChannelLocal<>();

    private volatile ScheduledFuture<?> scheduledFuture;

    static final String TERMS_AND_CONDITIONS = "" +
            "% The RIPE Database is subject to Terms and Conditions.\n" +
            "% See http://www.ripe.net/db/support/db-terms-conditions.pdf";

    public NrtmQueryHandler(final SerialDao serialDao, final Dummifier dummifier, final TaskScheduler clientSynchronisationScheduler, final NrtmLog nrtmLog, final String applicationVersion, final String source, final long updateInterval) {
        this.serialDao = serialDao;
        this.dummifier = dummifier;
        this.clientSynchronisationScheduler = clientSynchronisationScheduler;
        this.nrtmLog = nrtmLog;
        this.applicationVersion = applicationVersion;
        this.source = source;
        this.updateInterval = updateInterval;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
        if (isKeepAlive()) {
            return;
        }

        final String queryString = e.getMessage().toString().trim();

        nrtmLog.log(ChannelUtil.getRemoteAddress(ctx.getChannel()), queryString);
        LOGGER.debug("Received query: {}", queryString);

        final Query query = parseQueryString(queryString);
        final Channel channel = ctx.getChannel();

        if (query.isMirrorQuery()) {
            final SerialRange range = serialDao.getSerials();

            if (query.getSerialEnd() == -1 || query.isKeepalive()) {
                query.setSerialEnd(range.getEnd());
            }

            if (!isRequestedSerialInRange(query, range)) {
                throw new IllegalArgumentException("%ERROR:401: invalid range: Not within " + range.getBegin() + "-" + range.getEnd());
            }

            final Integer serialAge = serialDao.getAgeOfExactOrNextExistingSerial(query.getSerialBegin());

            if (serialAge == null || serialAge > HISTORY_AGE_LIMIT) {
                throw new IllegalArgumentException(String.format("%%ERROR:401: (Requesting serials older than %d days will be rejected)", HISTORY_AGE_LIMIT / SECONDS_PER_DAY));
            }

            final int version = query.getVersion();
            writeMessage(channel, String.format("%%START Version: %d %s %d-%d",
                    version,
                    source,
                    query.getSerialBegin(),
                    query.getSerialEnd()
            ));

            if (version < NrtmServer.NRTM_VERSION) {
                writeMessage(channel, String.format("%%WARNING: NRTM version %d is deprecated, please consider migrating to version %d!", version, NrtmServer.NRTM_VERSION));
            }

            if (query.isKeepalive()) {
                handleMirrorQueryWithKeepalive(query, channel);
                return;
            } else {
                handleMirrorQuery(query, channel);
            }
        } else if (query.isInfoQuery()) {
            switch (query.getQueryOption()) {
                case SOURCES:
                    handleSourcesQuery(channel);
                    break;
                case VERSION:
                    handleVersionQuery(channel);
                    break;
            }
        }

        channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private boolean isKeepAlive() {
        return scheduledFuture != null;
    }

    private Query parseQueryString(final String queryString) {
        try {
            return new Query(source, queryString);
        } catch (OptionException e) {
            throw new IllegalArgumentException("%ERROR:405: syntax error: " + e.getMessage());
        }
    }

    void handleMirrorQueryWithKeepalive(final Query query, final Channel channel) {
        final int version = query.getVersion();

        final Runnable instance = new Runnable() {
            private int serial = query.getSerialBegin();

            @Override
            public void run() {
                try {
                    final SerialRange range = serialDao.getSerials();
                    serial = writeSerials(serial, range.getEnd(), version, channel);
                } catch (ChannelException e) {
                    LOGGER.debug("writeSerials: closed channel");
                } catch (Exception e) {
                    // [EB]: no rethrowing else the repeating scheduler unschedules us
                    LOGGER.info("Exception in scheduled task:", e);
                }
            }
        };

        scheduledFuture = clientSynchronisationScheduler.scheduleAtFixedRate(instance, updateInterval * 1000);
    }

    private void handleMirrorQuery(final Query query, final Channel channel) {
        for (int serial = query.getSerialBegin(); serial <= query.getSerialEnd(); ) {
            serial = writeSerials(serial, query.getSerialEnd(), query.getVersion(), channel);
            if (serial <= query.getSerialEnd()) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
        }

        writeMessage(channel, "%END " + source);
    }

    private int writeSerials(final int begin, final int end, final int version, final Channel channel) {
        int serial = begin;

        while (serial <= end) {

            if (PENDING_WRITES.get(channel).get() > MAX_PENDING_WRITES) {
                break;
            }

            final SerialEntry serialEntry = serialDao.getByIdForNrtm(serial);
            if (serialEntry != null) {
                if (dummifier.isAllowed(version, serialEntry.getRpslObject())) {
                    final String operation = serialEntry.getOperation().toString();
                    final String message;
                    if (version == NrtmServer.NRTM_VERSION) {
                        message = operation + " " + serial;
                    } else {
                        message = operation;
                    }

                    writeMessage(channel, message);
                    writeMessage(channel, dummifier.dummify(version, serialEntry.getRpslObject()).toString().trim());
                }
            }

            serial++;
        }

        return serial;
    }

    private boolean isRequestedSerialInRange(final Query query, final SerialRange range) {
        return query.getSerialBegin() >= range.getBegin() && query.getSerialBegin() <= range.getEnd() &&
                query.getSerialEnd() >= range.getBegin() && query.getSerialEnd() <= range.getEnd();
    }

    private void handleSourcesQuery(final Channel channel) {
        writeMessage(channel, source + ":" + NrtmServer.NRTM_VERSION + ":X:" + serialDao.getSerials());
    }

    private void handleVersionQuery(final Channel channel) {
        writeMessage(channel, "% nrtm-server-" + applicationVersion);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        PENDING_WRITES.set(ctx.getChannel(), new AtomicInteger());

        writeMessage(ctx.getChannel(), TERMS_AND_CONDITIONS);

        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        PENDING_WRITES.remove(ctx.getChannel());

        super.channelDisconnected(ctx, e);
    }

    private void writeMessage(final Channel channel, final String message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        final AtomicInteger pending = PENDING_WRITES.get(channel);
        if (pending != null) {
            pending.incrementAndGet();
        }

        channel.write(message + "\n\n").addListener(LISTENER);
    }

    private static final ChannelFutureListener LISTENER = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            final AtomicInteger pending = PENDING_WRITES.get(future.getChannel());
            if (pending != null) {
                pending.decrementAndGet();
            }
        }
    };
}
