package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;

import java.util.Objects;

public class QueryCompletedEvent {
    private final Channel channel;
    private final QueryCompletionInfo completionInfo;

    public QueryCompletedEvent(final Channel channel) {
        this(channel, null);
    }

    public QueryCompletedEvent(final Channel channel, final QueryCompletionInfo completionInfo) {
        this.channel = channel;
        this.completionInfo = completionInfo;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelFuture getFuture() {
        return new DefaultChannelPromise(channel);
    }

    public boolean isForceClose() {
        return completionInfo != null && completionInfo.isForceClose();
    }

    public QueryCompletionInfo getCompletionInfo() {
        return completionInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final QueryCompletedEvent that = (QueryCompletedEvent) o;

        return Objects.equals(channel, that.channel) &&
                Objects.equals(completionInfo, that.completionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, completionInfo);
    }

    @Override
    public String toString() {
        return getChannel().toString() + " QUERY COMPLETED (completion info: " + completionInfo + ")";
    }
}
