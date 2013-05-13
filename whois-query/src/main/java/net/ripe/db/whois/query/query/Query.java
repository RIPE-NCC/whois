package net.ripe.db.whois.query.query;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public final class Query {
    public static final Pattern FLAG_PATTERN = Pattern.compile("(--?)([^-].*)");
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static final int MAX_QUERY_ELEMENTS = 60;

    private static final Set<ObjectType> DEFAULT_TYPES_LOOKUP_IN_BOTH_DIRECTIONS = Sets.newTreeSet(ObjectType.COMPARATOR);

    static {
        Collections.addAll(DEFAULT_TYPES_LOOKUP_IN_BOTH_DIRECTIONS, ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.ROUTE, ObjectType.ROUTE6, ObjectType.DOMAIN);
    }

    private static final Set<ObjectType> DEFAULT_TYPES_ALL = Sets.newTreeSet(ObjectType.COMPARATOR);

    static {
        Collections.addAll(DEFAULT_TYPES_ALL, ObjectType.values());
    }

    private static final List<QueryValidator> QUERY_VALIDATORS = Lists.newArrayList(
            new MatchOperationValidator(),
            new ProxyValidator(),
            new BriefValidator(),
            new CombinationValidator(),
            new SearchKeyValidator(),
            new TagValidator(),
            new VersionValidator());

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

        QueryFlag getQueryFlag() {
            return queryFlag;
        }
    }

    public static enum SystemInfoOption {
        VERSION, TYPES, SOURCES
    }

    private static final OptionParser PARSER = new OptionParser() {
        {
            for (final QueryFlag queryFlag : QueryFlag.values()) {
                for (final String flag : queryFlag.getFlags()) {
                    final OptionSpecBuilder optionSpecBuilder = accepts(flag);
                    if (queryFlag.getRequiredArgument() != null) {
                        optionSpecBuilder.withRequiredArg().ofType(queryFlag.getRequiredArgument());
                    }
                }
            }
        }

        @Override
        public OptionSet parse(final String... arguments) {
            for (final String argument : arguments) {
                final Matcher matcher = FLAG_PATTERN.matcher(argument);
                if (matcher.matches() && !isValidOption(matcher)) {
                    throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("Invalid option: " + argument));
                }
            }

            return super.parse(arguments);
        }

        private boolean isValidOption(final Matcher matcher) {
            final boolean shortOptionSupplied = matcher.group(1).length() == 1;
            final String suppliedFlag = matcher.group(2);

            for (final String flag : QueryFlag.getValidLongFlags()) {
                if (flag.equalsIgnoreCase(suppliedFlag)) {
                    return !shortOptionSupplied;
                }
            }

            return shortOptionSupplied;
        }
    };

    private static final Joiner SPACE_JOINER = Joiner.on(' ');
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings();
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings();

    private final Messages messages = new Messages();
    private final OptionSet options;

    private final String originalStringQuery;

    private final Set<String> sources;
    private final Set<ObjectType> objectTypeFilter;
    private final Set<AttributeType> attributeTypeFilter;
    private final Set<Query.MatchOperation> matchOperations;
    private final SearchKey searchKey;

    private Query(final String query) {
        String[] args = Iterables.toArray(SPACE_SPLITTER.split(query), String.class);
        originalStringQuery = SPACE_JOINER.join(args);
        if (args.length > MAX_QUERY_ELEMENTS) {
            messages.add(QueryMessages.malformedQuery());
        }

        options = PARSER.parse(args);
        searchKey = new SearchKey(SPACE_JOINER.join(options.nonOptionArguments()).trim());

        sources = parseSources();
        objectTypeFilter = parseObjectTypes();
        attributeTypeFilter = parseAttributeTypes();
        matchOperations = parseMatchOperations();
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public static Query parse(final String args) {
        try {
            final Query query = new Query(args.trim());

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

    public Collection<Message> getWarnings() {
        return messages.getMessages(Messages.Type.WARNING);
    }

    public boolean hasOptions() {
        return options.hasOptions();
    }

    public boolean hasOption(final QueryFlag queryFlag) {
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAllSources() {
        return hasOption(QueryFlag.ALL_SOURCES);
    }

    public boolean isLookupInBothDirections() {
        return hasOption(QueryFlag.REVERSE_DOMAIN);
    }

    public boolean isReturningIrt() {
        return isBrief() || (!isKeysOnly() && hasOption(QueryFlag.IRT));
    }

    public boolean isGrouping() {
        return (!isKeysOnly() && !hasOption(QueryFlag.NO_GROUPING)) && !(isBrief() || isObjectVersion());
    }

    public boolean isBrief() {
        return hasOption(QueryFlag.ABUSE_CONTACT);
    }

    public boolean isKeysOnly() {
        return hasOption(QueryFlag.PRIMARY_KEYS);
    }

    public boolean isPrimaryObjectsOnly() {
        return !(isReturningReferencedObjects() || isReturningIrt() || isGrouping());
    }

    public boolean isFiltered() {
        return !(hasOption(QueryFlag.NO_FILTERING) || isKeysOnly() || isHelp() || isTemplate() || isVerbose());
    }

    public boolean isHelp() {
        return getSearchValue().equalsIgnoreCase("help");
    }

    public boolean isSystemInfo() {
        return hasOption(QueryFlag.LIST_SOURCES_OR_VERSION) || hasOption(QueryFlag.LIST_SOURCES) || hasOption(QueryFlag.VERSION) || hasOption(QueryFlag.TYPES);
    }

    public boolean isReturningReferencedObjects() {
        return !(hasOption(QueryFlag.NO_REFERENCED) || isShortHand() || isKeysOnly() || isObjectVersion() || isBrief());
    }

    public boolean isInverse() {
        return hasOption(QueryFlag.INVERSE);
    }

    public boolean isTemplate() {
        return hasOption(QueryFlag.TEMPLATE);
    }

    public boolean isVersionList() {
        return hasOption(QueryFlag.LIST_VERSIONS);
    }

    public boolean isObjectVersion() {
        return hasOption(QueryFlag.SHOW_VERSION);
    }

    public int getObjectVersion() {
        try {
            if (hasOption(QueryFlag.SHOW_VERSION)) {
                int version = Integer.parseInt(getOptionValue(QueryFlag.SHOW_VERSION));
                if (version < 1) {
                    throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("version flag number must be greater than 0"));
                }
                return version;
            }
            return -1;
        } catch (OptionException e) {
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery());
        }
    }

    public String getTemplateOption() {
        return getOptionValue(QueryFlag.TEMPLATE);
    }

    public boolean isVerbose() {
        return hasOption(QueryFlag.VERBOSE);
    }

    public SystemInfoOption getSystemInfoOption() {
        if (hasOption(QueryFlag.LIST_SOURCES_OR_VERSION)) {
            final String optionValue = getOptionValue(QueryFlag.LIST_SOURCES_OR_VERSION).trim();
            try {
                return SystemInfoOption.valueOf(optionValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery("Invalid option: " + optionValue));
            }
        }

        if (hasOption(QueryFlag.LIST_SOURCES)) {
            return SystemInfoOption.SOURCES;
        }

        if (hasOption(QueryFlag.VERSION)) {
            return SystemInfoOption.VERSION;
        }

        if (hasOption(QueryFlag.TYPES)) {
            return SystemInfoOption.TYPES;
        }

        throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.malformedQuery());
    }

    public String getVerboseOption() {
        return getOptionValue(QueryFlag.VERBOSE);
    }

    public boolean hasObjectTypesSpecified() {
        return hasOption(QueryFlag.SELECT_TYPES);
    }

    public Set<ObjectType> getObjectTypes() {
        return objectTypeFilter;
    }

    public Set<AttributeType> getAttributeTypes() {
        return attributeTypeFilter;
    }

    public Set<Query.MatchOperation> matchOperations() {
        return matchOperations;
    }

    public boolean hasIpFlags() {
        return isLookupInBothDirections() || isBrief() || !matchOperations().isEmpty();
    }

    public boolean hasObjectTypeFilter(ObjectType objectType) {
        return objectTypeFilter.contains(objectType);
    }

    public String getSearchValue() {
        return searchKey.getValue();
    }

    public String getCleanSearchValue() {
        final IpInterval<?> ipKeyOrNull = getIpKeyOrNull();
        if (ipKeyOrNull != null) {
            return ipKeyOrNull instanceof Ipv4Resource ? ((Ipv4Resource)ipKeyOrNull).toRangeString() : ipKeyOrNull.toString();
        }

        return WHITESPACE_PATTERN.matcher(searchKey.getValue().trim()).replaceAll(" ");
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

    public AsBlockRange getAsBlockRangeOrNull() {
        return searchKey.getAsBlockRangeOrNull();
    }

    public String getProxy() {
        return getOptionValue(QueryFlag.CLIENT);
    }

    public boolean hasProxy() {
        return hasOption(QueryFlag.CLIENT);
    }

    public boolean hasProxyWithIp() {
        return getProxyIp() != null;
    }

    public boolean hasKeepAlive() {
        return hasOption(QueryFlag.PERSISTENT_CONNECTION);
    }

    public boolean isShortHand() {
        return hasOption(QueryFlag.BRIEF);
    }

    public boolean hasOnlyKeepAlive() {
        return hasKeepAlive() && (queryLength() == 2 || originalStringQuery.equals("--persistent-connection"));
    }

    public int queryLength() {
        return originalStringQuery.length();
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

    public boolean hasSources() {
        return !sources.isEmpty();
    }

    public Set<String> getSources() {
        return sources;
    }

    public Query addProxyFlag(InetAddress inetAddress) {
        if (!hasProxyWithIp()) {
            return addProxiedFor(inetAddress);
        }

        return this;
    }

    private Query addProxiedFor(InetAddress inetAddress) {
        return new Query(String.format("-VWhoisRDP,%s %s", inetAddress.getHostAddress(), originalStringQuery));
    }

    private Set<ObjectType> parseObjectTypes() {
        final Set<String> objectTypes = getOptionValues(QueryFlag.SELECT_TYPES);
        final TreeSet<ObjectType> ret;

        if (objectTypes.isEmpty()) {
            if (isLookupInBothDirections()) {
                ret = new TreeSet<ObjectType>(DEFAULT_TYPES_LOOKUP_IN_BOTH_DIRECTIONS);
            } else {
                ret = new TreeSet<ObjectType>(DEFAULT_TYPES_ALL);
            }
        } else {
            ret = new TreeSet<ObjectType>(ObjectType.COMPARATOR);
            for (final String objectType : objectTypes) {
                try {
                    ret.add(ObjectType.getByName(objectType));
                } catch (IllegalArgumentException e) {
                    throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.invalidObjectType(objectType));
                }
            }
        }

        if (hasOption(QueryFlag.NO_PERSONAL)) {
            ret.remove(ObjectType.PERSON);
            ret.remove(ObjectType.ROLE);
        }

        return Collections.unmodifiableSet(ret);
    }

    private Set<String> parseSources() {
        final Set<String> optionValues = getOptionValues(QueryFlag.SOURCES);
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

        final Set<String> attributeTypes = getOptionValues(QueryFlag.INVERSE);
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

    public Set<String> getOptionValues(final QueryFlag queryFlag) {
        final Set<String> optionValues = Sets.newLinkedHashSet();
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : options.valuesOf(flag)) {
                    for (final String splittedArgument : COMMA_SPLITTER.split(optionArgument.toString())) {
                        optionValues.add(splittedArgument);
                    }
                }
            }
        }

        return optionValues;
    }

    // TODO: [AH] only this CIString version should be used
    public Set<CIString> getOptionValuesCI(final QueryFlag queryFlag) {
        final Set<CIString> optionValues = Sets.newLinkedHashSet();
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : options.valuesOf(flag)) {
                    for (final String splittedArgument : COMMA_SPLITTER.split(optionArgument.toString())) {
                        optionValues.add(ciString(splittedArgument));
                    }
                }
            }
        }

        return optionValues;
    }

    String getOptionValue(final QueryFlag queryFlag) {
        String optionValue = null;
        for (final String flag : queryFlag.getFlags()) {
            if (options.has(flag)) {
                for (final Object optionArgument : options.valuesOf(flag)) {
                    if (optionValue == null) {
                        optionValue = optionArgument.toString();
                    } else {
                        throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.invalidMultipleFlags((flag.length() == 1 ? "-" : "--") + flag));
                    }
                }
            }
        }
        return optionValue;
    }

    private Set<MatchOperation> parseMatchOperations() {
        final Set<MatchOperation> result = Sets.newHashSet();
        for (final Query.MatchOperation matchOperation : Query.MatchOperation.values()) {
            if (matchOperation.hasFlag() && hasOption(matchOperation.getQueryFlag())) {
                result.add(matchOperation);
            }
        }
        return Collections.unmodifiableSet(result);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + options.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Query other = (Query) obj;
        return options.equals(other.options);
    }

    @Override
    public String toString() {
        return originalStringQuery;
    }

    public boolean matchesObjectType(final ObjectType objectType) { // TODO [AK] Merge this with getObjectTypes, we're never interested in stuff we don't query anyway
        for (final AttributeType attribute : ObjectTemplate.getTemplate(objectType).getLookupAttributes()) {
            if (AttributeMatcher.fetchableBy(attribute, this)) {
                return true;
            }
        }

        return false;
    }

    public boolean MatchesObjectTypeAndAttribute(final ObjectType objectType, final AttributeType attributeType) {
        return ObjectTemplate.getTemplate(objectType).getLookupAttributes().contains(attributeType) && AttributeMatcher.fetchableBy(attributeType, this);
    }
}
