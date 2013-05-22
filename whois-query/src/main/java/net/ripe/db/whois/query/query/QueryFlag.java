package net.ripe.db.whois.query.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum QueryFlag {
    EXACT(new Builder("x", "exact")
            .withSearchKey("<ip-lookup>")
            .describedAs("Requests that only an exact match on a prefix be performed. If no exact match is found no objects are returned.")),

    ONE_LESS(new Builder("l", "one-less")
            .withSearchKey("<ip-lookup>")
            .describedAs("Returns first level less specific inetnum, inet6num or route(6) objects, excluding exact matches.")),


    ALL_LESS(new Builder("L", "all-less")
            .withSearchKey("<ip-lookup>")
            .describedAs("Returns all level less specific inetnum, inet6num or route(6) objects, including exact matches.")),

    ONE_MORE(new Builder("m", "one-more")
            .withSearchKey("<ip-lookup>")
            .describedAs("Returns first level more specific inetnum, inet6num or route(6) objects, excluding exact matches.")),

    ALL_MORE(new Builder("M", "all-more")
            .withSearchKey("<ip-lookup>")
            .describedAs("Returns all level more specific inetnum, inet6num or route(6) objects, excluding exact matches.")),

    NO_IRT(new Builder("C", "no-irt")
            .withSearchKey("<ip-lookup>")
            .describedAs("Turns off default option '-c' or '--irt'.")),

    IRT(new Builder("c", "irt")
            .withSearchKey("<ip-lookup>")
            .describedAs("Requests first level less specific inetnum or inet6num objects with the \"mnt-irt:\" attribute (enabled by default).")),

    ABUSE_CONTACT(new Builder("b", "abuse-contact")
            .withSearchKey("<ip-lookup>")
            .describedAs("Requests the \"abuse-mailbox:\" address related to the specified inetnum or inet6num object. Only specified object key and \"abuse-mailbox:\" attributes are shown.")),

    REVERSE_DOMAIN(new Builder("d", "reverse-domain")
            .withSearchKey("<ip-lookup>")
            .describedAs("When used with hierarchical flags (like --one-less), both address and route object types and domain object types are returned.")),

    /* -------------------------------------------------------------------------------------------------------------- */

    INVERSE(new Builder("i", "inverse")
            .withSearchKey("<attribute-name> <inverse-key>")
            .describedAs("Perform an inverse query.")
            .requiresArgument(String.class)),

    /* -------------------------------------------------------------------------------------------------------------- */

    BRIEF(new Builder("F", "brief")
            .describedAs("Produce output using short hand notation for attribute names.")),

    PRIMARY_KEYS(new Builder("K", "primary-keys")
            .describedAs("" +
                    "Requests that only the primary keys of an object to be returned. " +
                    "The exceptions are set objects, where the members attributes will also be returned. " +
                    "This flag does not apply to person and role objects.")),

    PERSISTENT_CONNECTION(new Builder("k", "persistent-connection")
            .withSearchKey("(optional normal query)")
            .describedAs("" +
                    "Requests a persistent connection. After returning the result the connection will not " +
                    "be closed by the server and a client may issue multiple queries on the same connection.\n" +
                    "Note, that server implements 'stop-and-wait' protocol, when no next query can be sent " +
                    "before receiving a reply for the previous one.\n" +
                    "Except the first -k query, -k without an argument closes the persistent connection.")),

    NO_GROUPING(new Builder("G", "no-grouping")
            .describedAs("Disables the grouping of objects by relevance.")),

    NO_FILTERING(new Builder("B", "no-filtering")
            .describedAs("Disables the filtering of \"notify:\", \"changed:\" and \"e-mail:\" attributes.")),

    /* -------------------------------------------------------------------------------------------------------------- */

    NO_TAGINFO(new Builder("no-taginfo")
            .describedAs("Switches off tagging information.")),

    SHOW_TAGINFO(new Builder("show-taginfo")
            .describedAs("Switches on tagging information.")),

    FILTER_TAG_INCLUDE(new Builder("filter-tag-include")
            .describedAs("Show only objects with given tag(s)")
            .requiresArgument(String.class)),

    FILTER_TAG_EXCLUDE(new Builder("filter-tag-exclude")
            .describedAs("Do not show objects with given tag(s)")
            .requiresArgument(String.class)),

    /* -------------------------------------------------------------------------------------------------------------- */

    NO_REFERENCED(new Builder("r", "no-referenced")
            .describedAs("Switches off referenced lookup for related information after retrieving the objects that match the query string.")),

    NO_PERSONAL(new Builder("no-personal")
            .describedAs("Filter PERSON and ROLE objects from results")),

    SHOW_PERSONAL(new Builder("show-personal")
            .describedAs("Include PERSON and ROLE objects in results")),

    SELECT_TYPES(new Builder("T", "select-types")
            .withSearchKey("(comma separated list of object types with no white space)")
            .describedAs("" +
                    "Select the types of objects to lookup in the query.")
            .requiresArgument(String.class)),

    ALL_SOURCES(new Builder("a", "all-sources")
            .describedAs("Specifies that the server should perform lookups in all available sources. See also \"-q sources\" or \"--all-sources\" query.")),

    SOURCES(new Builder("s", "sources")
            .withSearchKey("(comma separated list of sources, no white space is allowed)")
            .describedAs("" +
                    "Specifies which sources and in which order are to be looked up when performing a query.")
            .requiresArgument(String.class)),

    GRS(new Builder("grs")
            .describedAs("Search all sources for resources and returns the authoritative one. Placeholders are omitted.")),

    /* -------------------------------------------------------------------------------------------------------------- */

    LIST_SOURCES_OR_VERSION(new Builder("q")
            .withSearchKey("(sources|version|types)")
            .describedAs("" +
                    "\"sources\"  see list-sources.\n" +
                    "\"version\"  see version.\n" +
                    "\"types\"    show all object types.")
            .requiresArgument(String.class)),

    LIST_SOURCES(new Builder("list-sources")
            .describedAs("" +
                    "Returns the current set of sources along with the information required for mirroring. " +
                    "See [REF], section 2.9 \"Other server features\" for more information.")),

    VERSION(new Builder("version")
            .describedAs("Displays the current version of the server.")),

    TYPES(new Builder("types")
            .describedAs("List of available RPSL object types.")),

    TEMPLATE(new Builder("t", "template")
            .withSearchKey("<object-type>")
            .describedAs("Requests a template for the specified object type.")
            .requiresArgument(String.class)),

    VERBOSE(new Builder("v", "verbose")
            .withSearchKey("<object-type>")
            .describedAs("Requests a verbose template for the specified object type.")
            .requiresArgument(String.class)),

    CLIENT(new Builder("V", "client")
            .withSearchKey("<client-tag>")
            .describedAs("Sends information about the client to the server.")
            .requiresArgument(String.class)),

    /* -------------------------------------------------------------------------------------------------------------- */

    LIST_VERSIONS(new Builder("list-versions")
            .describedAs("Returns a list of historical versions of the object")),

    SHOW_VERSION(new Builder("show-version")
            .withSearchKey("<version-number>")
            .describedAs("Returns historical version of the object")
            .requiresArgument(Integer.class));

    private static class Builder {
        private List<String> flags = Collections.emptyList();
        private String searchKey;
        private String description;
        private Class<?> requiredArgument;

        private Builder(final String... flags) {
            this.flags = Lists.newArrayList(flags);
        }

        Builder withSearchKey(final String searchKey) {
            this.searchKey = searchKey;
            return this;
        }

        Builder describedAs(final String description) {
            this.description = description;
            return this;
        }

        Builder requiresArgument(Class<?> requiredArgument) {
            this.requiredArgument = requiredArgument;
            return this;
        }
    }

    private final List<String> flags;
    private final String longFlag;
    private final String searchKey;
    private final String description;
    private final Class<?> requiredArgument;
    private final String toString;

    private QueryFlag(final Builder builder) {
        this.flags = Collections.unmodifiableList(builder.flags);
        this.searchKey = builder.searchKey;
        this.description = builder.description;
        this.requiredArgument = builder.requiredArgument;

        String longestFlag = "";
        final StringBuilder toStringBuilder = new StringBuilder();
        for (final String flag : flags) {
            if (toStringBuilder.length() > 0) {
                toStringBuilder.append(", ");
            }

            final String flagString = flag.length() == 1 ? "-" + flag : "--" + flag;
            toStringBuilder.append(flagString);
            if (longestFlag.length() < flagString.length()) {
                longestFlag = flagString;
            }
        }

        this.toString = toStringBuilder.toString();
        this.longFlag = longestFlag;
    }

    public List<String> getFlags() {
        return flags;
    }

    public String getLongFlag() {
        return longFlag;
    }

    @CheckForNull
    public String getSearchKey() {
        return searchKey;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    public Class<?> getRequiredArgument() {
        return requiredArgument;
    }

    @Override
    public String toString() {
        return toString;
    }


    private static Set<String> VALID_LONG_FLAGS;

    static {
        final List<String> validLongFlags = Lists.newArrayListWithCapacity(QueryFlag.values().length);
        for (final QueryFlag queryFlag : QueryFlag.values()) {
            for (final String flag : queryFlag.getFlags()) {
                if (flag.length() > 1) {
                    validLongFlags.add(flag);
                }
            }
        }

        VALID_LONG_FLAGS = Collections.unmodifiableSet(Sets.newLinkedHashSet(validLongFlags));
    }

    public static Set<String> getValidLongFlags() {
        return VALID_LONG_FLAGS;
    }
}
