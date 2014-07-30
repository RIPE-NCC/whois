package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.WhoisRestService;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class VersionsService {

    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final ObjectReferenceDao objectReferenceDao;
    private final VersionDao versionDao;
    private final VersionObjectMapper versionObjectMapper;

    @Autowired
    public VersionsService(final ObjectReferenceDao objectReferenceDao, final VersionDao versionDao, final VersionObjectMapper versionObjectMapper) {
        this.objectReferenceDao = objectReferenceDao;
        this.versionDao = versionDao;
        this.versionObjectMapper = versionObjectMapper;
    }

    public List<ObjectVersion> getVersions(final String key, final ObjectType type) {
        return objectReferenceDao.getVersions(key, type);
    }

    public StreamingOutput streamVersions(final String key, final ObjectType type, final String source, final HttpServletRequest request) {
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                final StreamingMarshal marshal = WhoisRestService.getStreamingMarshal(request, output);
                final StreamHandler streamHandler = new StreamHandler(marshal, source, versionObjectMapper);
                objectReferenceDao.streamVersions(key, type, streamHandler);
                streamHandler.flushAndGetErrors();
            }
        };
    }

    public RpslObjectWithTimestamp getRpslObjectWithTimestamp(final ObjectType type, final String key, final Integer revision) {

        final ObjectVersion version = objectReferenceDao.getVersion(type, key, revision);

        if (version == null) {
            return null;
        }

        final List<VersionInfo> entriesInSameVersion = lookupRpslObjectByVersion(version);
        final RpslObject rpslObject = versionDao.getRpslObject(entriesInSameVersion.get(0)); //latest is first
        final List<ObjectReference> outgoing = objectReferenceDao.getOutgoing(version.getVersionId());
        final List<ObjectReference> incoming = objectReferenceDao.getIncoming(version.getVersionId());

        return new RpslObjectWithTimestamp(
                decorateRpslObject(rpslObject),
                entriesInSameVersion.size(),
                new VersionDateTime(version.getInterval().getStartMillis()/1000L),
                outgoing,
                incoming);
    }

    private List<VersionInfo> lookupRpslObjectByVersion(final ObjectVersion version) {
       //TODO: [TP] A list is returned because there may be multiple modifications of an object on the same timestamp.

        final List<VersionInfo> versionInfos = versionDao.getVersionsForTimestamp(
                version.getType(),
                version.getPkey().toString(),
                version.getInterval().getStart().getMillis());

        if (CollectionUtils.isEmpty(versionInfos)) {
            throw new IllegalStateException("There should be one or more objects");
        }

        final VersionDateTime maxTimestamp = versionInfos.get(0).getTimestamp();

        final List<VersionInfo> latestVersionInfos = Lists.newArrayList(
                Iterables.filter(versionInfos, new Predicate<VersionInfo>() {
                    @Override
                    public boolean apply(@NotNull VersionInfo input) {
                        return input.getTimestamp().getTimestamp().
                                equals(maxTimestamp.getTimestamp())
                                && input.getOperation() != Operation.DELETE;
                    }
                }));

        if (latestVersionInfos.isEmpty()) {
            throw new IllegalStateException("There should be one or more objects");
        }

        // sort in reverse order, so that first item is the object with the highest timestamp.
        Collections.sort(latestVersionInfos, Collections.reverseOrder());

        return latestVersionInfos;
    }

    private RpslObject decorateRpslObject(final RpslObject rpslObject) {
        return FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply(rpslObject));
    }

    private Collection<? extends ResponseObject> makeListWithNoResultsMessage(final String key) {
        return Collections.singletonList(new MessageObject(InternalMessages.noVersion(key)));
    }
}
