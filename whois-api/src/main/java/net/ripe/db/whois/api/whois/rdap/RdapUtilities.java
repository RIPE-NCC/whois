package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.ApiResponseHandler;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;

import java.net.InetAddress;

public class RdapUtilities {
    public static RpslObject fetchObject(QueryHandler queryHandler, final String type, final String key, final String source) {
        final String qstring = String.format(
                "%s %s %s %s %s %s",
                QueryFlag.SOURCES.getLongFlag(),
                source,
                QueryFlag.SELECT_TYPES.getLongFlag(),
                type,
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                key);

        final Query query = Query.parse(qstring);

        final RpslObject ros[] = new RpslObject[1];

        final int contextId = System.identityHashCode(Thread.currentThread());

        queryHandler.streamResults(query, InetAddress.getLoopbackAddress(), contextId, new ApiResponseHandler() {
            @Override
            public void handle(final ResponseObject responseObject) {
                if ((responseObject instanceof RpslObject) && ((RpslObject) responseObject).getType().getName().equals(type)) {
                    ros[0] = (RpslObject) responseObject;
                }
            }
        });

        return ros[0];
    }
}
