package net.ripe.db.whois.query.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class QueryHandler {
    private final WhoisLog whoisLog;
    private final AccessControlListManager accessControlListManager;
    private final SourceContext sourceContext;
    private final List<QueryExecutor> queryExecutors;

    @Autowired
    public QueryHandler(final WhoisLog whoisLog,
                        final AccessControlListManager accessControlListManager,
                        final SourceContext sourceContext,
                        final QueryExecutor... queryExecutors) {
        this.whoisLog = whoisLog;
        this.accessControlListManager = accessControlListManager;
        this.sourceContext = sourceContext;
        this.queryExecutors = Lists.newArrayList(queryExecutors);
    }

    public void streamResults(final Query query, final InetAddress remoteAddress, final int contextId, final ResponseHandler responseHandler) {
        new Runnable() {
            private final Stopwatch stopwatch = Stopwatch.createStarted();

            private InetAddress accountingAddress;
            private boolean useAcl;
            private int accountedObjects;
            private int notAccountedObjects;
            private int accountingLimit = -1;

            @Override
            public void run() {
                try {
                    final QueryExecutor queryExecutor = getQueryExecutor();
                    authenticate();
                    initAcl(queryExecutor);
                    executeQuery(queryExecutor);
                    logQuery(null);
                } catch (QueryException e) {
                    logQuery(e.getCompletionInfo());
                    throw e;
                } catch (RuntimeException e) {
                    logQuery(QueryCompletionInfo.EXCEPTION);
                    throw e;
                } finally {
                    if (accountedObjects > 0) {
                        accessControlListManager.accountPersonalObjects(accountingAddress, accountedObjects);
                    }
                }
            }

            private QueryExecutor getQueryExecutor() {
                for (final QueryExecutor queryExecutor : queryExecutors) {
                    if (queryExecutor.supports(query)) {
                        return queryExecutor;
                    }
                }

                throw new QueryException(QueryCompletionInfo.UNSUPPORTED_QUERY, QueryMessages.unsupportedQuery());
            }

            private void initAcl(final QueryExecutor queryExecutor) {
                if (queryExecutor.isAclSupported()) {
                    checkBlocked(remoteAddress);

                    if (query.hasProxyWithIp()) {
                        if (!accessControlListManager.isAllowedToProxy(remoteAddress)) {
                            throw new QueryException(QueryCompletionInfo.PROXY_NOT_ALLOWED, QueryMessages.notAllowedToProxy());
                        }

                        accountingAddress = InetAddresses.forString(query.getProxyIp());
                        checkBlocked(accountingAddress);
                    } else {
                        accountingAddress = remoteAddress;
                    }

                    useAcl = !accessControlListManager.isUnlimited(accountingAddress);
                }
            }

            private void checkBlocked(final InetAddress inetAddress) {
                if (accessControlListManager.isDenied(inetAddress)) {
                    throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(inetAddress));
                } else if (!accessControlListManager.canQueryPersonalObjects(inetAddress)) {
                    throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(inetAddress));
                }
            }

            private void authenticate() {
                // here be authenticated queries
                query.setTrusted(accessControlListManager.isTrusted(remoteAddress));
            }

            private void executeQuery(QueryExecutor queryExecutor) {
                queryExecutor.execute(query, new ResponseHandler() {
                    @Override
                    public String getApi() {
                        return responseHandler.getApi();
                    }

                    @Override
                    public void handle(final ResponseObject responseObject) {
                        if (responseObject instanceof RpslObject) {
                            if (useAcl && accessControlListManager.requiresAcl((RpslObject) responseObject, sourceContext.getCurrentSource())) {
                                if (accountingLimit == -1) {
                                    accountingLimit = accessControlListManager.getPersonalObjects(accountingAddress);
                                }

                                if (++accountedObjects > accountingLimit) {
                                    throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(accountingAddress));
                                }
                            } else {
                                notAccountedObjects++;
                            }
                        }

                        responseHandler.handle(responseObject);
                    }
                });
            }

            private void logQuery(@Nullable final QueryCompletionInfo completionInfo) {
                whoisLog.logQueryResult(responseHandler.getApi(), accountedObjects, notAccountedObjects, completionInfo, stopwatch.elapsed(TimeUnit.MILLISECONDS), remoteAddress, contextId, query.toString());
            }

        }.run();
    }

}
