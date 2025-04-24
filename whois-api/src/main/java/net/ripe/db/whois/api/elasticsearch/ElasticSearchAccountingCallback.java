package net.ripe.db.whois.api.elasticsearch;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;

import java.io.IOException;
import java.net.InetAddress;

public abstract class ElasticSearchAccountingCallback<T> {

    private final AccessControlListManager accessControlListManager;
    private final InetAddress remoteAddress;
    private final UserSession userSession;
    private final Source source;

    private int accountingLimit = -1;
    private int accountedObjects = 0;

    private final boolean enabled;

    public ElasticSearchAccountingCallback(final AccessControlListManager accessControlListManager,
                                           final String remoteAddress,
                                           final UserSession userSession,
                                           final Source source) {
        this.accessControlListManager = accessControlListManager;
        this.remoteAddress = InetAddresses.forString(remoteAddress);
        this.userSession = userSession;
        this.enabled = !accessControlListManager.isUnlimited(this.remoteAddress);
        this.source = source;
    }

    public T search() throws IOException {

        final AccountingIdentifier accountingIdentifier = getAccountingIdentifier();
        accessControlListManager.checkBlocked(accountingIdentifier);

        try {
            return doSearch();
        } finally {
            if (enabled && accountedObjects > 0) {
                accessControlListManager.accountPersonalObjects(accountingIdentifier, accountedObjects);
            }
        }
    }

    protected abstract T doSearch() throws IOException;

    protected void account(final RpslObject rpslObject) {
        if (enabled && accessControlListManager.requiresAcl(rpslObject, source, getAccountingIdentifier())) {
            if (accountingLimit == -1) {
                accountingLimit = accessControlListManager.getPersonalObjects(getAccountingIdentifier());
            }

            if (++accountedObjects > accountingLimit) {
                throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress.getHostAddress()));
            }
        }
    }

    private AccountingIdentifier getAccountingIdentifier() {
        return accessControlListManager.getAccountingIdentifier(remoteAddress, userSession == null ? null : userSession.getUsername());
    }
}