package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.DefaultChannelFuture;

public class QueryCompletedEvent implements ChannelEvent {
    private final Channel channel;
    private final QueryCompletionInfo completionInfo;

    public QueryCompletedEvent(final Channel channel) {
        this(channel, null);
    }

    public QueryCompletedEvent(final Channel channel, final QueryCompletionInfo completionInfo) {
        this.channel = channel;
        this.completionInfo = completionInfo;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public ChannelFuture getFuture() {
        return new DefaultChannelFuture(channel, false);
    }

    public boolean isForceClose() {
        return completionInfo != null && completionInfo.isForceClose();
    }

    public QueryCompletionInfo getCompletionInfo() {
        return completionInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QueryCompletedEvent that = (QueryCompletedEvent) o;
        return channel.equals(that.channel) && completionInfo == that.completionInfo;
    }

    @Override
    public int hashCode() {
        int result = channel.hashCode();
        result = 31 * result + (completionInfo != null ? completionInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getChannel().toString() + " QUERY COMPLETED (completion info: " + completionInfo + ")";
    }
}
