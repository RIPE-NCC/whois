package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.api.rest.StreamingHelper;
import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.rest.WhoisInternalResources;
import net.ripe.db.whois.query.VersionDateTime;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
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

    public StreamingOutput streamVersions(final String key, final ObjectType type, final String source, final HttpServletRequest request) {
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                final StreamingMarshal marshal = StreamingHelper.getStreamingMarshal(request, output);
                final VersionsStreamHandler versionsStreamHandler = new VersionsStreamHandler(marshal, source, versionObjectMapper);
                objectReferenceDao.streamVersions(key, type, versionsStreamHandler);
                if (!versionsStreamHandler.flushHasStreamedObjects()) {
                    throw new WebApplicationException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(notFoundWhoisInternalResources(request, InternalMessages.noVersion(key)))
                            .build());
                }
            }
        };
    }

    public StreamingOutput streamVersion(final ObjectType type, final String key, final String source, final Integer revision, final HttpServletRequest request) {
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                final StreamingMarshal marshal = StreamingHelper.getStreamingMarshal(request, output);
                final ReferenceStreamHandler streamHandler = new ReferenceStreamHandler(marshal, source, versionObjectMapper);

                ObjectVersion version = null;
                RpslObject rpslObject = null;
                List<VersionInfo> entriesInSameVersion = null;
                try {
                    version = objectReferenceDao.getVersion(type, key, revision);
                    entriesInSameVersion = lookupRpslObjectByVersion(version);
                    rpslObject = versionDao.getRpslObject(entriesInSameVersion.get(0)); //latest is first
                    rpslObject = decorateRpslObject(rpslObject);
                } catch (DataAccessException e) {
                    throw new WebApplicationException(Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(notFoundWhoisInternalResources(request, InternalMessages.noVersion(key)))
                            .build());
                }

                streamHandler.streamWhoisObject(rpslObject);
                streamHandler.streamVersion(version);

                if (entriesInSameVersion.size() > 1){
                    streamHandler.streamErrorMessage(InternalMessages.multipleVersionsForTimestamp(entriesInSameVersion.size()));
                }
                objectReferenceDao.streamIncoming(version, streamHandler);
                streamHandler.endStreamingIncoming();
                objectReferenceDao.streamOutgoing(version, streamHandler);
                streamHandler.flush();
            }
        };
    }

    private WhoisInternalResources notFoundWhoisInternalResources(final HttpServletRequest request, Message message) {
        final WhoisInternalResources whoisResources = new WhoisInternalResources();
        whoisResources.setErrorMessages(Lists.newArrayList(new ErrorMessage(message)));
        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request)));
        whoisResources.includeTermsAndConditions();

        return whoisResources;
    }

    private List<VersionInfo> lookupRpslObjectByVersion(final ObjectVersion version) {
        final List<VersionInfo> versionInfos = versionDao.getVersionsForTimestamp(
                version.getType(),
                version.getPkey().toString(),
                version.getFromDate().getMillis());

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
}
