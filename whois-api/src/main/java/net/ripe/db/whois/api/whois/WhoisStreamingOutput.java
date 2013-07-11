package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.ws.rs.core.StreamingOutput;
import java.net.InetAddress;

public abstract class WhoisStreamingOutput implements StreamingOutput {

    protected static final int STATUS_TOO_MANY_REQUESTS = 429;
    protected StreamingMarshal streamingMarshal;
    protected QueryHandler queryHandler;
    protected Parameters parameters;
    protected Query query;
    protected InetAddress remoteAddress;
    protected int contextId;
    protected boolean found;

    public WhoisStreamingOutput(StreamingMarshal streamingMarshal, QueryHandler queryHandler, Parameters parameters, Query query, InetAddress remoteAddress, int contextId) {
        this.streamingMarshal = streamingMarshal;
        this.queryHandler = queryHandler;
        this.parameters = parameters;
        this.query = query;
        this.remoteAddress = remoteAddress;
        this.contextId = contextId;
    }
}
