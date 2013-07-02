package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.ws.rs.core.StreamingOutput;
import java.net.InetAddress;

public abstract class WhoisStreamingOutput implements StreamingOutput {

    protected boolean found;
    protected static final int STATUS_TOO_MANY_REQUESTS = 429;
    protected StreamingMarshal streamingMarshal;
    protected QueryHandler queryHandler;
    protected Parameters parameters;
    protected Query query;
    protected InetAddress remoteAddress;
    protected int contextId;

    public WhoisStreamingOutput(StreamingMarshal sm, QueryHandler qh, Parameters p, Query q, InetAddress ra, int cid) {
        streamingMarshal = sm;
        queryHandler = qh;
        parameters = p;
        query = q;
        remoteAddress = ra;
        contextId = cid;
    }
}
