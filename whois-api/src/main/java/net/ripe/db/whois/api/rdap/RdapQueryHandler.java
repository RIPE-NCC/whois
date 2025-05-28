package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.mchange.v1.util.UnexpectedException;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.api.rest.ApiResponseHandler;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Stream;

@Component
public class RdapQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapQueryHandler.class);

    private final QueryHandler queryHandler;

    @Autowired
    public RdapQueryHandler(final QueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public Stream<RpslObject> handleQueryStream(final Query query, final HttpServletRequest request) {
        final Stream.Builder<RpslObject> out = Stream.builder();
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        try {
            queryHandler.streamResults(query, remoteAddress, 0, new ApiResponseHandler() {
                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof RpslObject) {
                        final ObjectType objectType = ((RpslObject) responseObject).getType();

                        switch (objectType) {
                            case PERSON:
                            case MNTNER:
                            case ROLE: {
                                addIfPrimaryObject((RpslObject) responseObject);
                                break;
                            }
                            default: {
                                out.add((RpslObject) responseObject);
                            }
                        }
                    }
                }
                private void addIfPrimaryObject(final RpslObject responseObject) {
                    if(responseObject.getKey().equals(query.getSearchValue())) {
                        out.add(responseObject);
                    }
                }
            });
        } catch (final QueryException e) {
            return handleQueryException(e).stream();
        }
        return out.build();
    }

    public List<RpslObject> handleAutNumQuery(final Query query, final HttpServletRequest request) {
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final List<RpslObject> resultAutNum = Lists.newArrayList();
        final List<RpslObject> resultAsBlock = Lists.newArrayList();
        try {
            queryHandler.streamResults(query, remoteAddress, 0, new ApiResponseHandler() {
                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof RpslObject) {
                        final ObjectType objectType = ((RpslObject) responseObject).getType();
                        switch (objectType) {
                            case AUT_NUM: {
                                resultAutNum.add((RpslObject) responseObject);
                                break;
                            }
                            case AS_BLOCK: {
                                resultAsBlock.add((RpslObject) responseObject);
                                break;
                            }
                            default: {
                                throw new UnexpectedException("Expected AUT_NUM or AS_Block but found " + objectType);
                            }
                        }
                    }
                }
            });
            return resultAutNum.isEmpty() ? resultAsBlock : resultAutNum;

        } catch (final QueryException e) {
            return handleQueryException(e);
        }
    }

    private List<RpslObject> handleQueryException(final QueryException e) {
        if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
            throw tooManyRequests(e.getMessage());
        } else {
            LOGGER.error(e.getMessage(), e);
            throw new RdapException("500 Internal Server Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    private RdapException tooManyRequests(final String message) {
        return new RdapException("429 Too Many Requests", message, HttpStatus.TOO_MANY_REQUESTS_429);
    }
}
