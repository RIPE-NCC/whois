package net.ripe.db.whois.api.elasticsearch;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;

import java.io.IOException;
import java.net.InetAddress;

public abstract class ElasticSearchAccountingCallback<T> {

    private final AccessControlListManager accessControlListManager;
    private final InetAddress remoteAddress;
    private final String ssoToken;
    private final Source source;

    private int accountingLimit = -1;
    private int accountedObjects = 0;

    private final boolean enabled;

    public ElasticSearchAccountingCallback(final AccessControlListManager accessControlListManager,
                                           final String ssoToken,
                                           final String remoteAddress,
                                           final Source source) {
        this.accessControlListManager = accessControlListManager;
        this.remoteAddress = InetAddresses.forString(remoteAddress);
        this.ssoToken = ssoToken;
        this.enabled = !accessControlListManager.isUnlimited(this.remoteAddress);
        this.source = source;
    }

    public T search() throws IOException {

        if (accessControlListManager.isDenied(remoteAddress, ssoToken)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(remoteAddress));
        } else if (!accessControlListManager.canQueryPersonalObjects(remoteAddress, ssoToken)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
        }

        try {
            return doSearch();
        } finally {
            if (enabled && accountedObjects > 0) {
                accessControlListManager.accountPersonalObjects(remoteAddress, null, accountedObjects);
            }
        }
    }

    protected abstract T doSearch() throws IOException;

    protected void account(final RpslObject rpslObject) {
        if (enabled && accessControlListManager.requiresAcl(rpslObject, source)) {
            if (accountingLimit == -1) {
                accountingLimit = accessControlListManager.getPersonalObjects(remoteAddress, ssoToken);
            }

            if (++accountedObjects > accountingLimit) {
                throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
            }
        }
    }
}