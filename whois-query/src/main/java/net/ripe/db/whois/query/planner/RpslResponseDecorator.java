package net.ripe.db.whois.query.planner;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.x509.ClientAuthCertificateValidator;
import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.executor.decorators.DummifyDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterPersonalDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterPlaceholdersDecorator;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// TODO [AK] Wrap related response objects (messages + rpsl) in a single response object

// TODO [AK] Merge into SearchQueryExecutor

/**
 * Converts a query into an query result. Typically queries results are stored as proxies
 * and object data is loaded as the result is being written to the client (lazy evaluation).
 */
@Component
public class RpslResponseDecorator {
    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();
    private static final FilterChangedFunction FILTER_CHANGED_FUNCTION = new FilterChangedFunction();

    private final RpslObjectDao rpslObjectDao;
    private final FilterPersonalDecorator filterPersonalDecorator;
    private final DummifyDecorator dummifyDecorator;
    private final SourceContext sourceContext;
    private final BriefAbuseCFunction briefAbuseCFunction;
    private final SyntaxFilterFunction validSyntaxFilterFunction;
    private final SyntaxFilterFunction invalidSyntaxFilterFunction;
    private final FilterPlaceholdersDecorator filterPlaceholdersDecorator;
    private final AbuseCInfoDecorator abuseCInfoDecorator;
    private final Set<PrimaryObjectDecorator> decorators;
    private final SsoTokenTranslator ssoTokenTranslator;
    private final AuthServiceClient authServiceClient;
    private final ToShorthandFunction toShorthandFunction;
    private final ToKeysFunction toKeysFunction;
    private final ClientAuthCertificateValidator clientAuthCertificateValidator;

    @Autowired
    public RpslResponseDecorator(final RpslObjectDao rpslObjectDao,
                                 final FilterPersonalDecorator filterPersonalDecorator,
                                 final DummifyDecorator dummifyDecorator,
                                 final SourceContext sourceContext,
                                 final AbuseCFinder abuseCFinder,
                                 final FilterPlaceholdersDecorator filterPlaceholdersDecorator,
                                 final AbuseCInfoDecorator abuseCInfoDecorator,
                                 final SsoTokenTranslator ssoTokenTranslator,
                                 final AuthServiceClient authServiceClient,
                                 final ClientAuthCertificateValidator clientAuthCertificateValidator,
                                 final PrimaryObjectDecorator... decorators) {
        this.rpslObjectDao = rpslObjectDao;
        this.filterPersonalDecorator = filterPersonalDecorator;
        this.dummifyDecorator = dummifyDecorator;
        this.sourceContext = sourceContext;
        this.abuseCInfoDecorator = abuseCInfoDecorator;
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.authServiceClient = authServiceClient;
        this.validSyntaxFilterFunction = new SyntaxFilterFunction(true);
        this.invalidSyntaxFilterFunction = new SyntaxFilterFunction(false);
        this.filterPlaceholdersDecorator = filterPlaceholdersDecorator;
        this.briefAbuseCFunction = new BriefAbuseCFunction(abuseCFinder);
        this.decorators = Sets.newHashSet(decorators);
        this.toShorthandFunction = new ToShorthandFunction();
        this.toKeysFunction = new ToKeysFunction();
        this.clientAuthCertificateValidator = clientAuthCertificateValidator;
    }

    public Iterable<? extends ResponseObject> getResponse(final Query query, Iterable<? extends ResponseObject> result) {
        Iterable<? extends ResponseObject> decoratedResult = filterPlaceholdersDecorator.decorate(query, result);
        decoratedResult = dummifyDecorator.decorate(query, decoratedResult);

        decoratedResult = groupRelatedObjects(query, decoratedResult);
        decoratedResult = filterPersonalDecorator.decorate(query, decoratedResult);
        decoratedResult = abuseCInfoDecorator.decorate(query, decoratedResult);

        decoratedResult = applySyntaxFilter(query, decoratedResult);
        decoratedResult = filterEmail(query, decoratedResult);
        decoratedResult = filterAuth(query, decoratedResult);
        decoratedResult = filterChanged(decoratedResult);

        decoratedResult = applyOutputFilters(query, decoratedResult);

        return decoratedResult;
    }

    private Iterable<? extends ResponseObject> applySyntaxFilter(final Query query, final Iterable<? extends ResponseObject> result) {
        if (query.isValidSyntax()) {
            return Iterables.concat(Iterables.transform(result, validSyntaxFilterFunction::apply));
        }
        if (query.isNoValidSyntax()) {
            return Iterables.concat(Iterables.transform(result, invalidSyntaxFilterFunction::apply));
        }

        return result;
    }

    private Iterable<? extends ResponseObject> groupRelatedObjects(final Query query, Iterable<? extends ResponseObject> primaryObjects) {
        final GroupFunction groupFunction = getGroupFunction(query);
        if (groupFunction == null) {
            return primaryObjects;
        }

        final Iterable<ResponseObject> groupInline = Iterables.concat(Iterables.transform(primaryObjects, groupFunction::apply));

        return Iterables.concat(
                groupInline,
                groupFunction.getGroupedAfter());
    }

    @CheckForNull
    private GroupFunction getGroupFunction(final Query query) {
        if (query.isPrimaryObjectsOnly()) {
            return null;
        }

        if (query.isGrouping()) {
            return new GroupRelatedFunction(rpslObjectDao, query, decorators);
        }

        return new GroupObjectTypesFunction(rpslObjectDao, query, decorators);
    }

    private Iterable<? extends ResponseObject> filterChanged(final Iterable<? extends ResponseObject> objects) {

        return Iterables.transform(objects, input -> {
                if (input instanceof RpslObject) {
                    return FILTER_CHANGED_FUNCTION.apply((RpslObject) input);
                }

                return input;
            });
    }

    private Iterable<? extends ResponseObject> filterAuth(Query query, final Iterable<? extends ResponseObject> objects) {
        List<String> passwords = query.getPasswords();
        final String ssoToken = query.getSsoToken();
        final OAuthSession oAuthSession = query.getoAuthSession();
        final List<X509CertificateWrapper> certificates = query.getCertificates();

        final FilterAuthFunction filterAuthFunction =
                (CollectionUtils.isEmpty(passwords) && StringUtils.isBlank(ssoToken) && hasNotCertificates(certificates))?
                        FILTER_AUTH_FUNCTION :
                        new FilterAuthFunction(passwords, oAuthSession, ssoToken, ssoTokenTranslator, authServiceClient,
                                rpslObjectDao, certificates, clientAuthCertificateValidator);

        return Iterables.transform(objects, input -> {
            if (input instanceof RpslObject) {
                return filterAuthFunction.apply((RpslObject) input);
            }

            return input;
        });
    }

    private static boolean hasNotCertificates(final List<X509CertificateWrapper> certificates) {
        return certificates == null || certificates.isEmpty();
    }

    private Iterable<? extends ResponseObject> filterEmail(final Query query, final Iterable<? extends ResponseObject> groupedObjects) {
        if (!sourceContext.isAcl() || !query.isFiltered() || query.isBriefAbuseContact()) {
            return groupedObjects;
        }

        return new IterableTransformer<ResponseObject>(groupedObjects) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                } else {
                    result.add(FILTER_EMAIL_FUNCTION.apply((RpslObject) input));
                }

            }
        }.setHeader(new MessageObject(QueryMessages.outputFilterNotice()));
    }

    private Iterable<? extends ResponseObject> applyOutputFilters(final Query query, final Iterable<? extends ResponseObject> objects) {
        if (query.isShortHand()) {
            return Iterables.transform(objects, toShorthandFunction::apply);
        }

        if (query.isBriefAbuseContact()) {
            return Iterables.filter(Iterables.transform(objects, briefAbuseCFunction::apply), Objects::nonNull);
        }

        if (query.isKeysOnly()) {
            return Iterables.concat(
                    Collections.singletonList(new MessageObject(QueryMessages.primaryKeysOnlyNotice())),
                    Iterables.transform(objects, toKeysFunction::apply));
        }

        return objects;
    }
}
