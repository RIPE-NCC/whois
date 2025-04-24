package net.ripe.db.whois.query.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.hazelcast.IpBlockManager;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
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
    private final BasicSourceContext sourceContext;
    private final List<QueryExecutor> queryExecutors;
    private final IpBlockManager ipBlockManager;

    @Autowired
    public QueryHandler(final WhoisLog whoisLog,
                        final AccessControlListManager accessControlListManager,
                        final IpBlockManager ipBlockManager,
                        final BasicSourceContext sourceContext,
                        final QueryExecutor... queryExecutors) {
        this.whoisLog = whoisLog;
        this.accessControlListManager = accessControlListManager;
        this.sourceContext = sourceContext;
        this.queryExecutors = Lists.newArrayList(queryExecutors);
        this.ipBlockManager = ipBlockManager;
    }

    public void streamResults(final Query query, final InetAddress remoteAddress, final Integer contextId, final ResponseHandler responseHandler) {
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
                    if (ipBlockManager.isBlockedIp(remoteAddress)){
                        throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedForAbuse(remoteAddress.getHostAddress()));
                    }

                    final QueryExecutor queryExecutor = getQueryExecutor();
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
                        accessControlListManager.accountPersonalObjects(getAccountingIdentifier(), accountedObjects);
                    }
                }
            }

            private AccountingIdentifier getAccountingIdentifier() {
                return accessControlListManager.getAccountingIdentifier(accountingAddress, query.getUserSession(), query.getoAuthSession());
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
                    final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(remoteAddress, query.getUserSession(), query.getoAuthSession());
                    accessControlListManager.checkBlocked(accountingIdentifier);

                    if (query.hasProxyWithIp()) {
                        if (!accessControlListManager.isAllowedToProxy(remoteAddress)) {
                            throw new QueryException(QueryCompletionInfo.PROXY_NOT_ALLOWED, QueryMessages.notAllowedToProxy());
                        }

                        accountingAddress = InetAddresses.forString(query.getProxyIp());
                        accessControlListManager.checkBlocked(accountingIdentifier);
                    } else {
                        accountingAddress = remoteAddress;
                    }

                    useAcl = !accessControlListManager.isUnlimited(accountingAddress);
                }
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
                                    accountingLimit = accessControlListManager.getPersonalObjects(getAccountingIdentifier());
                                }

                                if (++accountedObjects > accountingLimit) {
                                    throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(accountingAddress.getHostAddress()));
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
