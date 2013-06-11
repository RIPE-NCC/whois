package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.api.whois.domain.WhoisTag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: andrew-old
 * Date: 11/06/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class RDAPStreamingOutput extends DefaultStreamingOutput {

    public RDAPStreamingOutput(StreamingMarshal sm, QueryHandler qh, Parameters p, Query q, InetAddress ra, int cid) {
        super(sm,qh,p,q,ra,cid);
    }


    protected void streamObject(@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
        if (rpslObject == null) {
            return;
        }

        final WhoisObject whoisObject = WhoisObjectMapper.map(rpslObject);

        // TODO [AK] Fix mapper API
        final List<WhoisTag> tags = WhoisObjectMapper.mapTags(tagResponseObjects).getTags();
        whoisObject.setTags(tags);

        streamingMarshal.write("object", whoisObject);
        tagResponseObjects.clear();
    }
}
