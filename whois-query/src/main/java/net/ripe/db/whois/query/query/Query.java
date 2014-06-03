package net.ripe.db.whois.query.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import joptsimple.OptionException;
import net.ripe.db.whois.common.IllegalArgumentExceptionMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryParser;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// TODO: [AH] further separate concerns of query parsing and business logic
// TODO: [AH] merge QueryBuilder and Query to cooperate better
// TODO: [ES] class is not immutable
public class Query {
    public static final EnumSet<ObjectType> ABUSE_CONTACT_OBJECT_TYPES = EnumSet.of(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);
    private static final EnumSet<ObjectType> GRS_LIMIT_TYPES = EnumSet.of(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.ROUTE, ObjectType.ROUTE6, ObjectType.DOMAIN);
    private static final EnumSet<ObjectType> DEFAULT_TYPES_LOOKUP_IN_BOTH_DIRECTIONS = EnumSet.of(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.ROUTE, ObjectType.ROUTE6, ObjectType.DOMAIN);
    private static final EnumSet<ObjectType> DEFAULT_TYPES_ALL = EnumSet.allOf(ObjectType.class);

    private static final List<QueryValidator> QUERY_VALIDATORS = Lists.newArrayList(
            new MatchOperationValidator(),
            new ProxyValidator(),
            new AbuseContactValidator(),
            new CombinationValidator(),
            new SearchKeyValidator(),
            new TagValidator(),
            new VersionValidator(),
            new InverseValidator());

    private final QueryParser queryParser;
    private final Messages messages = new Messages();

    private final Set<String> sources;
    private final Set<ObjectType> objectTypeFilter;
    private final Set<ObjectType> suppliedObjectTypes;
    private final Set<AttributeType> attributeTypeFilter;
    private final MatchOperation matchOperation;
    private final SearchKey searchKey;

    // TODO: [AH] these fields should be part of QueryContext, not Query
    private List<String> passwords;
    private String ssoToken;
    private Origin origin;
    private boolean trusted;
    // TODO: [AH] we should use -x flag for direct match for all object types instead of this hack
    private boolean matchPrimaryKeyOnly;

    private Query(final String query, final Origin origin, final boolean trusted) {
        try {
            queryParser = new QueryParser(query);
        } catch (IllegalArgumentExceptionMessage e) {
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, e.getExceptionMessage());
        }
        searchKey = new SearchKey(queryParser.getSearchKey());

        sources = parseSources();
        suppliedObjectTypes = parseSuppliedObjectTypes();
        objectTypeFilter = generateAndFilterObjectTypes();
        attributeTypeFilter = parseAttributeTypes();
        matchOperation = parseMatchOperations();
        this.origin = origin;
        this.trusted = trusted;
    }

    public static Query parse(final String args) {
        return parse(args, Origin.LEGACY, false);
    }

    public static Query parse(final String args, final Origin origin, final boolean trusted) {
        try {
            final Query query = new Query(args.trim(), origin, trusted);

            for (final QueryValidator queryValidator : QUERY_VALIDATORS) {
                queryValidator.validate(query, query.messages);
            }

            final Collection<Message> errors = query.messages.getMessages(Messages.Type.ERROR);
            if (!errors.isEmpty()) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, errors);
            }

            return query;
        } catch (OptionException e) {
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery());
        }
    }

    public static Query parse(final String args, final String ssoToken, final List<String> passwords, final boolean trusted) {
        Query query = parse(args, Origin.REST, trusted);
        query.ssoToken = ssoToken;
        query.passwords = passwords;
        return query;
    }

    public List<String> getPasswords() {
        return passwords;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public boolean via(Origin origin) {
        return this.origin == origin;
    }

    public Collection<Message> getWarnings() {
        return messages.getMessages(Messages.Type.WARNING);
    }

    public boolean isAllSources() {
        return queryParser.hasOption(QueryFlag.ALL_SOURCES);
    }

    public boolean isResource() {
        return queryParser.hasOption(QueryFlag.RESOURCE);
    }

    public boolean isLookupInBothDirections() {
        return queryParser.hasOption(QueryFlag.REVERSE_DOMAIN);
    }

    public boolean isReturningIrt() {
        return isBriefAbuseContact() || (!isKeysOnly() && queryParser.hasOption(QueryFlag.IRT));
    }

    public boolean isGrouping() {
        return (!isKeysOnly() && !queryParser.hasOption(QueryFlag.NO_GROUPING)) && !isBriefAbuseContact();
    }

    public boolean isBriefAbuseContact() {
        return queryParser.hasOption(QueryFlag.ABUSE_CONTACT);
    }

    public boolean isKeysOnly() {
        return queryParser.hasOption(QueryFlag.PRIMARY_KEYS);
    }

    public boolean isPrimaryObjectsOnly() {
        return !(isReturningReferencedObjects() || isReturningIrt() || isGrouping());
    }

    public boolean isFiltered() {
        return !(queryParser.hasOption(QueryFlag.NO_FILTERING) || isKeysOnly() || isHelp() || isTemplate() || isVerbose());
    }

    public boolean isHelp() {
        return getSearchValue().equalsIgnoreCase("help");
    }

    public boolean isSystemInfo() {
        return queryParser.hasOption(QueryFlag.LIST_SOURCES_OR_VERSION) ||
                queryParser.hasOption(QueryFlag.LIST_SOURCES) ||
                queryParser.hasOption(QueryFlag.VERSION) ||
                queryParser.hasOption(QueryFlag.TYPES);
    }

    public boolean isReturningReferencedObjects() {
        return !(queryParser.hasOption(QueryFlag.NO_REFERENCED) || isShortHand() || isKeysOnly() || isResource() || isBriefAbuseContact());
    }

    public boolean isInverse() {
        return queryParser.hasOption(QueryFlag.INVERSE);
    }

    public boolean isTemplate() {
        return queryParser.hasOption(QueryFlag.TEMPLATE);
    }

    public boolean isVersionList() {
        return queryParser.hasOption(QueryFlag.LIST_VERSIONS);
    }

    public boolean isVersionDiff() {
        return queryParser.hasOption(QueryFlag.DIFF_VERSIONS);
    }

    public boolean isObjectVersion() {
        return queryParser.hasOption(QueryFlag.SHOW_VERSION);
    }

    public int getObjectVersion() {
        if (queryParser.hasOption(QueryFlag.SHOW_VERSION)) {
            final int version = Integer.parseInt(getOnlyValue(QueryFlag.SHOW_VERSION));
            if (version < 1) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("version flag number must be greater than 0"));
            }
            return version;
        }
        return -1;
    }

    public int[] getObjectVersions() {
        if (queryParser.hasOption(QueryFlag.DIFF_VERSIONS)) {
            final String[] values = StringUtils.split(getOnlyValue(QueryFlag.DIFF_VERSIONS), ':');
            if (values.length != 2) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("diff versions must be in the format a:b"));
            }
            final int firstValue = Integer.parseInt(values[0]);
            final int secondValue = Integer.parseInt(values[1]);
            if (firstValue < 1 || secondValue < 1) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("diff version number must be greater than 0"));
            }
            if (secondValue == firstValue) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("diff versions are the same"));
            }
            return new int[]{firstValue, secondValue};
        }
        return new int[]{-1, -1};
    }

    public String getTemplateOption() {
        return getOnlyValue(QueryFlag.TEMPLATE);
    }

    public boolean isVerbose() {
        return queryParser.hasOption(QueryFlag.VERBOSE);
    }

    public boolean isValidSyntax() {
        return queryParser.hasOption(QueryFlag.VALID_SYNTAX);
    }

    public boolean isNoValidSyntax() {
        return queryParser.hasOption(QueryFlag.NO_VALID_SYNTAX);
    }

    public SystemInfoOption getSystemInfoOption() {
        if (queryParser.hasOption(QueryFlag.LIST_SOURCES_OR_VERSION)) {
            final String optionValue = getOnlyValue(QueryFlag.LIST_SOURCES_OR_VERSION).trim();
            try {
                return SystemInfoOption.valueOf(optionValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("Invalid option: " + optionValue));
            }
        }

        if (queryParser.hasOption(QueryFlag.LIST_SOURCES)) {
            return SystemInfoOption.SOURCES;
        }

        if (queryParser.hasOption(QueryFlag.VERSION)) {
            return SystemInfoOption.VERSION;
        }

        if (queryParser.hasOption(QueryFlag.TYPES)) {
            return SystemInfoOption.TYPES;
        }

        throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery());
    }

    public String getVerboseOption() {
        return getOnlyValue(QueryFlag.VERBOSE);
    }

    public boolean hasObjectTypesSpecified() {
        return queryParser.hasOption(QueryFlag.SELECT_TYPES);
    }

    public Set<ObjectType> getObjectTypes() {
        return objectTypeFilter;
    }

    public Set<AttributeType> getAttributeTypes() {
        return attributeTypeFilter;
    }

    public MatchOperation matchOperation() {
        return matchOperation;
    }

    public boolean hasIpFlags() {
        return isLookupInBothDirections() || matchOperation != null;
    }

    public boolean hasObjectTypeFilter(ObjectType objectType) {
        return objectTypeFilter.contains(objectType);
    }

    public String getSearchValue() {
        return searchKey.getValue();
    }

    public Set<ObjectType> getSuppliedObjectTypes() {
        return suppliedObjectTypes;
    }

    public IpInterval<?> getIpKeyOrNull() {
        final IpInterval<?> ipKey = searchKey.getIpKeyOrNull();
        if (ipKey != null) {
            return ipKey;
        }

        if (isLookupInBothDirections()) {
            return searchKey.getIpKeyOrNullReverse();
        }

        return null;
    }

    public IpInterval<?> getIpKeyOrNullReverse() {
        final IpInterval<?> ipKey = searchKey.getIpKeyOrNullReverse();
        if (ipKey != null) {
            return ipKey;
        }

        if (isLookupInBothDirections()) {
            return searchKey.getIpKeyOrNull();
        }

        return null;
    }

    public String getRouteOrigin() {
        return searchKey.getOrigin();
    }

    public AsBlockRange getAsBlockRangeOrNull() {
        return searchKey.getAsBlockRangeOrNull();
    }

    public String getProxy() {
        return getOnlyValue(QueryFlag.CLIENT);
    }

    public boolean hasProxy() {
        return queryParser.hasOption(QueryFlag.CLIENT);
    }

    public boolean hasProxyWithIp() {
        return getProxyIp() != null;
    }

    public boolean hasKeepAlive() {
        return queryParser.hasOption(QueryFlag.PERSISTENT_CONNECTION);
    }

    public boolean isShortHand() {
        return queryParser.hasOption(QueryFlag.BRIEF);
    }

    public boolean hasOnlyKeepAlive() {
        return queryParser.hasOnlyQueryFlag(QueryFlag.PERSISTENT_CONNECTION);
    }

    public boolean isProxyValid() {
        if (!hasProxy()) {
            return true;
        }

        String[] proxyArray = StringUtils.split(getProxy(), ',');

        if (proxyArray.length > 2) {
            return false;
        }

        if (proxyArray.length == 2) {
            return InetAddresses.isInetAddress(proxyArray[1]);
        }

        return true;
    }

    public String getProxyIp() {
        if (!hasProxy()) {
            return null;
        }

        String[] proxyArray = StringUtils.split(getProxy(), ',');

        if (proxyArray.length == 2) {
            return proxyArray[1];
        }

        return null;
    }

    public boolean hasOptions() {
        return queryParser.hasOptions();
    }

    public boolean hasOption(QueryFlag queryFlag) {
        return queryParser.hasOption(queryFlag);
    }

    private String getOnlyValue(QueryFlag queryFlag) {
        try {
            return queryParser.getOptionValue(queryFlag);
        } catch (IllegalArgumentExceptionMessage e) {
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, e.getExceptionMessage());
        }
    }

    // TODO: [AH] drop access to queryParser.getOptionValues*(); make specific accessors instead
    @Deprecated
    public Set<String> getOptionValues(QueryFlag queryFlag) {
        return queryParser.getOptionValues(queryFlag);
    }

    // TODO: [AH] drop access to queryParser.getOptionValues*(); make specific accessors instead
    @Deprecated
    public Set<CIString> getOptionValuesCI(QueryFlag queryFlag) {
        return queryParser.getOptionValuesCI(queryFlag);
    }

    public boolean hasSources() {
        return !sources.isEmpty();
    }

    public Set<String> getSources() {
        return sources;
    }

    private Set<ObjectType> parseSuppliedObjectTypes() {
        final Set<String> objectTypesOptions = queryParser.getOptionValues(QueryFlag.SELECT_TYPES);
        final Set<ObjectType> objectTypes = Sets.newHashSet();

        if (!objectTypesOptions.isEmpty()) {
            for (final String objectType : objectTypesOptions) {
                try {
                    objectTypes.add(ObjectType.getByName(objectType));
                } catch (IllegalArgumentException e) {
                    throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.invalidObjectType(objectType));
                }
            }
        }
        return Collections.unmodifiableSet(objectTypes);
    }

    private Set<ObjectType> generateAndFilterObjectTypes() {
        final Set<ObjectType> response = Sets.newTreeSet(ObjectType.COMPARATOR);    // whois query results returned in correct order depends on this comparator

        if (suppliedObjectTypes.isEmpty()) {
            if (isLookupInBothDirections()) {
                response.addAll(DEFAULT_TYPES_LOOKUP_IN_BOTH_DIRECTIONS);
            } else {
                response.addAll(DEFAULT_TYPES_ALL);
            }
        } else {
            response.addAll(suppliedObjectTypes);
        }

        if (queryParser.hasOption(QueryFlag.NO_PERSONAL)) {
            response.remove(ObjectType.PERSON);
            response.remove(ObjectType.ROLE);
        }

        if (queryParser.hasOption(QueryFlag.RESOURCE)) {
            response.retainAll(GRS_LIMIT_TYPES);
        }

        if (queryParser.hasOption(QueryFlag.ABUSE_CONTACT)) {
            response.retainAll(ABUSE_CONTACT_OBJECT_TYPES);
        }

        if (!isInverse()) {
            nextObjectType:
            for (Iterator<ObjectType> it = response.iterator(); it.hasNext(); ) {
                ObjectType objectType = it.next();
                for (final AttributeType attribute : ObjectTemplate.getTemplate(objectType).getLookupAttributes()) {
                    if (AttributeMatcher.fetchableBy(attribute, this)) {
                        continue nextObjectType;
                    }
                }
                it.remove();
            }
        }

        return Collections.unmodifiableSet(response);
    }

    private Set<String> parseSources() {
        final Set<String> optionValues = queryParser.getOptionValues(QueryFlag.SOURCES);
        if (optionValues.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> result = Sets.newLinkedHashSet();
        for (final String source : optionValues) {
            result.add(source.toUpperCase());
        }

        return Collections.unmodifiableSet(result);
    }

    private Set<AttributeType> parseAttributeTypes() {
        if (!isInverse()) {
            return Collections.emptySet();
        }

        final Set<String> attributeTypes = queryParser.getOptionValues(QueryFlag.INVERSE);
        final Set<AttributeType> ret = Sets.newLinkedHashSet();
        for (final String attributeType : attributeTypes) {
            try {
                final AttributeType type = AttributeType.getByName(attributeType);
                if (AttributeType.PERSON.equals(type)) {
                    ret.addAll(Arrays.asList(AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.AUTHOR, AttributeType.PING_HDL));
                } else {
                    ret.add(type);
                }
            } catch (IllegalArgumentException e) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.invalidAttributeType(attributeType));
            }
        }

        return Collections.unmodifiableSet(ret);
    }

    private MatchOperation parseMatchOperations() {
        MatchOperation result = null;

        for (final MatchOperation matchOperation : MatchOperation.values()) {
            if (matchOperation.hasFlag() && queryParser.hasOption(matchOperation.getQueryFlag())) {
                if (result == null) {
                    result = matchOperation;
                } else {
                    throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.duplicateIpFlagsPassed());
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        return queryParser.equals(query.queryParser);
    }

    @Override
    public int hashCode() {
        return queryParser.hashCode();
    }

    @Override
    public String toString() {
        return queryParser.toString();
    }

    public boolean matchesObjectTypeAndAttribute(final ObjectType objectType, final AttributeType attributeType) {
        return ObjectTemplate.getTemplate(objectType).getLookupAttributes().contains(attributeType) && AttributeMatcher.fetchableBy(attributeType, this);
    }

    public boolean isMatchPrimaryKeyOnly() {
        return matchPrimaryKeyOnly;
    }

    public Query setMatchPrimaryKeyOnly(boolean matchPrimaryKeyOnly) {
        this.matchPrimaryKeyOnly = matchPrimaryKeyOnly;
        return this;
    }

    public static enum MatchOperation {
        MATCH_EXACT_OR_FIRST_LEVEL_LESS_SPECIFIC(),
        MATCH_EXACT(QueryFlag.EXACT),
        MATCH_FIRST_LEVEL_LESS_SPECIFIC(QueryFlag.ONE_LESS),
        MATCH_EXACT_AND_ALL_LEVELS_LESS_SPECIFIC(QueryFlag.ALL_LESS),
        MATCH_FIRST_LEVEL_MORE_SPECIFIC(QueryFlag.ONE_MORE),
        MATCH_ALL_LEVELS_MORE_SPECIFIC(QueryFlag.ALL_MORE);

        private final QueryFlag queryFlag;

        private MatchOperation() {
            this(null);
        }

        private MatchOperation(final QueryFlag queryFlag) {
            this.queryFlag = queryFlag;
        }

        boolean hasFlag() {
            return queryFlag != null;
        }

        public QueryFlag getQueryFlag() {
            return queryFlag;
        }
    }

    public static enum SystemInfoOption {
        VERSION, TYPES, SOURCES
    }

    public static enum Origin {
        LEGACY, REST, INTERNAL
    }
}
