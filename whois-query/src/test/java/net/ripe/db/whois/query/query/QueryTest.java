package net.ripe.db.whois.query.query;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class QueryTest {
    private Query subject;

    private Query parseWithNewline(final String input) {
        // [EB]: userinput will always be newline terminated
        return Query.parse(input + "\n");
    }

    private void parse(final String input) {
        subject = parseWithNewline(input);
    }

    @Test
    public void empty() {
        try {
            parse("");
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessages(), contains(QueryMessages.noSearchKeySpecified()));
        }
    }

    @Test
    public void query_with_searchValue() {
        parse("foo");

        assertThat(subject.isFiltered(), is(true));
        assertThat(subject.isGrouping(), is(true));
        assertThat(subject.isProxyValid(), is(true));
        assertThat(subject.isReturningReferencedObjects(), is(true));
        assertThat(subject.getSearchValue(), is("foo"));
    }

    @Test
    public void query_with_space() {
        parse("foo ");

        assertThat(subject.getSearchValue(), is("foo"));
    }

    @Test
    public void query_ignores_C() {
        parse("-C test");

        assertThat(subject.hasOptions(), is(true));
        assertThat(subject.getSearchValue(), is("test"));
    }

    @Test
    public void deprecated_R() {
        try {
            parse("-R test");
        } catch (QueryException e) {
            assertThat(e.getMessages(), hasSize(1));
            assertThat(e.getMessages().iterator().next(), is(QueryMessages.malformedQuery()));
        }
    }

    @Test
    public void non_filtered() {
        parse("-B foo");

        assertThat(subject.isFiltered(), is(false));
    }

    @Test
    public void non_grouping() {
        parse("-G foo");

        assertThat(subject.isGrouping(), is(false));
    }

    @Test
    public void non_recursive() {
        parse("-r foo");

        assertThat(subject.isReturningReferencedObjects(), is(false));
    }

    @Test
    public void is_short_hand() {
        parse("-F foo");

        assertThat(subject.isShortHand(), is(true));
    }

    @Test
    public void short_hand_is_non_recursive() {
        parse("-F foo");

        assertThat(subject.isShortHand(), is(true));
        assertThat(subject.isReturningReferencedObjects(), is(false));
    }

    @Test
    public void has_ip_flags() {
        final Set<QueryFlag> flags = Sets.newHashSet(QueryFlag.REVERSE_DOMAIN);
        for (final Query.MatchOperation matchOperation : Query.MatchOperation.values()) {
            if (matchOperation.getQueryFlag() != null) {
                flags.add(matchOperation.getQueryFlag());
            }
        }

        for (final QueryFlag queryFlag : flags) {
            for (final String flag : queryFlag.getFlags()) {
                parse((flag.length() == 1 ? "-" : "--") + flag + " 10.0.0.0");
                assertThat("flag: " + flag, subject.hasIpFlags(), is(true));
            }
        }
    }

    @Test
    public void match_operations_default_inetnum() {
        parse("-T inetnum 10.0.0.0");

        assertThat(subject.matchOperation(), is(nullValue()));
    }

    @Test
    public void match_operations_default_inet6num() {
        parse("-T inet6num ::0/0");

        assertThat(subject.matchOperation(), is(nullValue()));
    }

    @Test
    public void match_operations_no_default_for_maintainer() {
        parse("-T mntner foo");

        assertThat(subject.matchOperation(), is(nullValue()));
    }

    @Test
    public void match_operations_empty() {
        parse("foo");

        assertThat(subject.matchOperation(), is(nullValue()));
    }

    @Test
    public void recognize_flags_with_arguments() {
        final String[] flagsWithArguments = {"t", "v", "q", "V"};

        final StringBuilder queryBuilder = new StringBuilder();
        for (String flag : flagsWithArguments) {
            queryBuilder.append("-").append(flag).append(" ").append(flag).append("-flag").append(" ");
        }
        queryBuilder.append("is wrong");

        parse(queryBuilder.toString());

        assertThat(subject.getSearchValue(), is("is wrong"));
    }

    @Test
    public void no_sources() {
        parse("test");

        assertThat(subject.getSources(), hasSize(0));
    }

    @Test
    public void source() {
        parse("-s RIPE foo");

        assertThat(subject.getSources(), contains("RIPE"));
    }

    @Test
    public void sources() {
        parse("-s RIPE,TEST foo");

        assertThat(subject.getSources(), contains("RIPE", "TEST"));
    }

    @Test
    public void missing_arguments_for_flags() {
        final String[] flagsWithArguments = {"i", "s", "t", "v", "q", "V"};

        for (String flag : flagsWithArguments) {
            try {
                parse("-" + flag);
                fail("Missing argument for " + flag + " should throw an exception");
            } catch (QueryException e) {
                assertThat(e.getMessages(), contains(QueryMessages.malformedQuery()));
            }
        }
    }

    @Test
    public void single_type_invalid_searchkey() {
        try {
            parse("-T aut-num foo");
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
            assertThat(e.getMessages(), contains(QueryMessages.invalidSearchKey()));
        }
    }

    @Test
    public void single_type() {
        parse("-T aut-num AS1");

        assertThat(subject.hasObjectTypeFilter(ObjectType.AUT_NUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), contains(ObjectType.AUT_NUM));
    }

    @Test
    public void empty_types() {
        parse("TEST-DBM-MNT");

        assertThat(subject.getSuppliedObjectTypes(), hasSize(0));
        assertThat(subject.getObjectTypes(), not(hasSize(0)));
    }

    @Test
    public void multiple_types_casing() {
        parse("-T aut-num,iNet6nUM,iNETnUm foo");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.hasObjectTypeFilter(ObjectType.INET6NUM), is(true));
        assertThat(subject.hasObjectTypeFilter(ObjectType.AUT_NUM), is(false));
        assertThat(subject.getSuppliedObjectTypes(), containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void multiple_types_with_empty_element() {
        parse("-T aut-num,,iNETnUm foo");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.hasObjectTypeFilter(ObjectType.AUT_NUM), is(false));
        assertThat(subject.getSuppliedObjectTypes(), containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM));

        parse("-T aut-num,,iNETnUm as112");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.hasObjectTypeFilter(ObjectType.AUT_NUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM));
    }

    @Test
    public void short_types_casing() {
        parse("-T in,rT,An foo");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.hasObjectTypeFilter(ObjectType.ROUTE), is(false));
        assertThat(subject.hasObjectTypeFilter(ObjectType.AUT_NUM), is(false));
        assertThat(subject.getSuppliedObjectTypes(), containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.ROUTE));
    }

    @Test
    public void non_existing_types() {
        try {
            parse("-T aUT-Num,IAmInvalid,iNETnUm");
            fail("Non existing type should throw an exception");
        } catch (QueryException e) {
            assertThat(e.getMessage(), containsString("ERROR:103"));
        }
    }

    @Test
    public void type_option_without_space() {
        parse("-Tinetnum dont_care");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void type_option_with_extra_space() {
        parse("-T  inetnum dont_care");

        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void type_with_clustered_options() {
        parse("-rT inetnum dont_care");

        assertThat(subject.isReturningReferencedObjects(), is(false));
        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void type_with_clustered_options_and_no_space() {
        parse("-rTinetnum dont_care");

        assertThat(subject.isReturningReferencedObjects(), is(false));
        assertThat(subject.hasObjectTypeFilter(ObjectType.INETNUM), is(true));
        assertThat(subject.getSuppliedObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void proxied_for() {
        parse("-VclientId,10.0.0.0 foo");

        assertThat(subject.getProxy(), equalTo("clientId,10.0.0.0"));
    }

    @Test
    public void proxied_for_invalid() {
        assertThrows(QueryException.class, () -> {
            parse("-VclientId,ipAddress");
        });
    }

    @Test
    public void one_element_proxy_has_no_proxy_ip() {
        parse("-Vone foo");

        assertThat(subject.isProxyValid(), is(true));
        assertThat(subject.hasProxyWithIp(), is(false));
        assertThat(subject.getProxyIp(), nullValue());
    }

    @Test
    public void proxy_with_ip() {
        parse("-Vone,10.0.0.1 foo");

        assertThat(subject.isProxyValid(), is(true));
        assertThat(subject.hasProxyWithIp(), is(true));
    }

    @Test
    public void proxy_with_invalid_ip() {
        assertThrows(QueryException.class, () -> {
            parse("-Vone,two");
        });
    }

    @Test
    public void proxy_with_more_than_two_elements() {
        assertThrows(QueryException.class, () -> {
            parse("-Vone,two,10.1.1.1");
        });
    }

    @Test
    public void to_string_returns_input() {
        parse("-r -GBTinetnum dont_care");

        assertThat(subject.toString(), equalTo("-r -GBTinetnum dont_care"));
    }

    @Test
    public void only_keep_alive_flag_specified() {
        parse("-k\r");

        assertThat(subject.hasKeepAlive(), is(true));
        assertThat(subject.getSearchValue(), is(""));
    }

    @Test
    public void only_keep_alive_flag_specified_longoption() {
        parse("--persistent-connection\r");

        assertThat(subject.hasKeepAlive(), is(true));
        assertThat(subject.getSearchValue(), is(""));
    }

    @Test
    public void keep_alive_recognised() {
        parse("-rBG -T inetnum --persistent-connection 192.168.200.0 - 192.168.200.255\r");

        assertThat(subject.hasKeepAlive(), is(true));
        assertThat(subject.getSearchValue(), is("192.168.200.0 - 192.168.200.255"));
    }

    @Test
    public void invalidProxyShouldThrowException() {
        assertThrows(QueryException.class, () -> {
            Query.parse("-Vone,two,three -Tperson DW-RIPE");
        });
    }

    @Test
    public void testNoInverse() {
        final Query query = Query.parse("foo");

        assertThat(query.isInverse(), is(false));
    }

    @Test
    public void testInverseSingleAttribute() {
        final Query query = Query.parse("-i mnt-by aardvark-mnt");

        assertThat(query.isInverse(), is(true));
        assertThat(query.getAttributeTypes(), containsInAnyOrder(AttributeType.MNT_BY));
        assertThat(query.getSearchValue(), is("aardvark-mnt"));
        assertThat(query.matchOperation(), is(nullValue()));
    }

    @Test
    public void testInverseSingleAttributeWithTypeFilter() {
        final Query query = Query.parse("-i mnt-by aardvark-mnt -T inetnum");

        assertThat(query.isInverse(), is(true));
        assertThat(query.getAttributeTypes(), containsInAnyOrder(AttributeType.MNT_BY));
        assertThat(query.getSearchValue(), is("aardvark-mnt"));
        assertThat(query.matchOperation(), is(nullValue()));
    }

    @Test
    public void testInverseSingleAttributeShort() {
        final Query query = Query.parse("-i mb aardvark-mnt");

        assertThat(query.isInverse(), is(true));
        assertThat(query.getAttributeTypes(), containsInAnyOrder(AttributeType.MNT_BY));
        assertThat(query.getSearchValue(), is("aardvark-mnt"));
    }

    @Test
    public void testInverseInvalidAttribute() {
        try {
            Query.parse("-i some-invalid aardvark-mnt");
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getMessage(), is(QueryMessages.invalidAttributeType("some-invalid").toString()));
        }
    }

    @Test
    public void testInverseMultipleAttributes() {
        final Query query = Query.parse("-i mb,mz aardvark-mnt");

        assertThat(query.isInverse(), is(true));
        assertThat(query.getAttributeTypes(), containsInAnyOrder(AttributeType.MNT_REF, AttributeType.MNT_BY));
        assertThat(query.getSearchValue(), is("aardvark-mnt"));
    }

    @Test
    public void testMultipleObjectTypes() {
        final Query query = Query.parse("-T inet6num,domain,inetnum searchkey");

        assertThat(query.getSearchValue(), is("searchkey"));
        assertThat(query.getObjectTypes(), contains(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.DOMAIN));
    }

    @Test
    public void testMultipleSeparatedObjectTypes() {
        final Query query = Query.parse("-T inetnum -T inet6num,domain searchkey");

        assertThat(query.getObjectTypes(), contains(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.DOMAIN));
    }

    @Test
    public void testMultipleSeparatedAttributeTypes() {
        final Query query = Query.parse("-i mnt-by -i mnt-ref,mnt-lower foo");

        assertThat(query.getAttributeTypes(), containsInAnyOrder(AttributeType.MNT_BY, AttributeType.MNT_REF, AttributeType.MNT_LOWER));
    }

    @Test
    public void illegal_range_ipv4_more_all() {
        illegalRange("-M 0/0");
    }

    @Test
    public void illegal_range_ipv4_more() {
        illegalRange("-m 0/0");
    }

    @Test
    public void illegal_range_ipv6_more_all() {
        illegalRange("-M ::0/0");
    }

    @Test
    public void illegal_range_ipv6_more() {
        illegalRange("-m ::0/0");
    }

    private void illegalRange(final String queryString) {
        try {
            Query.parse(queryString);
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessage(), is(QueryMessages.illegalRange().toString()));
        }
    }

    @Test
    public void multiple_proxies() {
        assertThrows(QueryException.class, () -> {
            Query.parse("-V ripews,188.111.4.162   -V 85.25.132.61");
        });
    }

    @Test
    public void attributes_person() {
        final Query query = Query.parse("-i person name");
        assertThat(query.getAttributeTypes(), contains(AttributeType.ABUSE_C, AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.AUTHOR, AttributeType.PING_HDL));
        final Query queryPn = Query.parse("-i pn name");
        assertThat(queryPn.getAttributeTypes(), contains(AttributeType.ABUSE_C, AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.AUTHOR, AttributeType.PING_HDL));
    }

    @Test
    public void attributes_person_and_others() {
        final Query query = Query.parse("-i mb,person,ml name");
        assertThat(query.getAttributeTypes(), contains(AttributeType.MNT_BY, AttributeType.ABUSE_C, AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.AUTHOR, AttributeType.PING_HDL, AttributeType.MNT_LOWER));
        final Query queryPn = Query.parse("-i mb,pn,ml name");
        assertThat(queryPn.getAttributeTypes(), contains(AttributeType.MNT_BY, AttributeType.ABUSE_C, AttributeType.ADMIN_C, AttributeType.TECH_C, AttributeType.ZONE_C, AttributeType.AUTHOR, AttributeType.PING_HDL, AttributeType.MNT_LOWER));
    }

    @Test
    public void getAsBlockRange() {
        final Query query = Query.parse("-r -T as-block AS1-AS2");

        assertThat(query.getAsBlockRangeOrNull().getBegin(), is(1L));
        assertThat(query.getAsBlockRangeOrNull().getEnd(), is(2L));
    }

    @Test
    public void not_lookupBothDirections() {
        final Query query = Query.parse("10.0.0.0");

        assertThat(query.isLookupInBothDirections(), is(false));
    }

    @Test
    public void getIpKeyOrNull_both_directions_forward() {
        final Query query = Query.parse("-d 10.0.0.0");

        assertThat(query.isLookupInBothDirections(), is(true));
        assertThat(query.getIpKeyOrNull().toString(), is("10.0.0.0/32"));
    }

    @Test
    public void getIpKeyOrNull_both_directions_reverse_domain() {
        final Query query = Query.parse("-d 10.in-addr.arpa");

        assertThat(query.isLookupInBothDirections(), is(true));
        assertThat(query.getIpKeyOrNull().toString(), is("10.0.0.0/8"));
    }

    @Test
    public void not_isReturningIrt() {
        final Query query = Query.parse("10.0.0.0");

        assertThat(query.isReturningIrt(), is(false));
    }

    @Test
    public void isReturningIrt() {
        final Query query = Query.parse("-c 10.0.0.0");

        assertThat(query.isReturningIrt(), is(true));
    }

    @Test
    public void hasSources() {
        final Query query = Query.parse("-s RIPE,TEST 10.0.0.0");

        assertThat(query.hasSources(), is(true));
        assertThat(query.getSources(), contains("RIPE", "TEST"));
    }

    @Test
    public void not_hasSources() {
        final Query query = Query.parse("10.0.0.0");

        assertThat(query.hasSources(), is(false));
    }

    @Test
    public void isPrimaryObjectsOnly() {
        final Query query = Query.parse("-rG 10.0.0.0");

        assertThat(query.isPrimaryObjectsOnly(), is(true));
    }

    @Test
    public void isPrimaryObjectsOnly_grouping() {
        final Query query = Query.parse("-r 10.0.0.0");

        assertThat(query.isPrimaryObjectsOnly(), is(false));
    }

    @Test
    public void isPrimaryObjectsOnly_relatedTo() {
        final Query query = Query.parse("-G 10.0.0.0");

        assertThat(query.isPrimaryObjectsOnly(), is(false));
    }

    @Test
    public void isPrimaryObjectsOnly_irt() {
        final Query query = Query.parse("-rGc 10.0.0.0");

        assertThat(query.isPrimaryObjectsOnly(), is(false));
    }

    @Test
    public void system_info_invalid_option() {
        assertThrows(QueryException.class, () -> {
            Query query = Query.parse("-q invalid");
            query.getSystemInfoOption();
        });
    }

    @Test
    public void system_info_with_multiple_arguments() {
        assertThrows(QueryException.class, () -> {
            Query query = Query.parse("-q version -q invalid");
            query.getSystemInfoOption();
        });
    }

    @Test
    public void system_info_multiple_flags_no_arguments() {
        assertThrows(QueryException.class, () -> {
            Query query = Query.parse("-q -q");
            query.getSystemInfoOption();
        });
    }

    @Test
    public void system_info_version_flag() {
        final Query query = Query.parse("-q version");

        assertThat(query.isSystemInfo(), is(true));
        assertThat(query.getSystemInfoOption(), is(Query.SystemInfoOption.VERSION));
    }

    @Test
    public void system_info_types_flag() {
        final Query query = Query.parse("-q types");

        assertThat(query.isSystemInfo(), is(true));
        assertThat(query.getSystemInfoOption(), is(Query.SystemInfoOption.TYPES));
    }

    @Test
    public void system_info_types_flag_longoption() {
        final Query query = Query.parse("--types");

        assertThat(query.isSystemInfo(), is(true));
        assertThat(query.getSystemInfoOption(), is(Query.SystemInfoOption.TYPES));
    }

    @Test
    public void isAllSources() {
        final Query query = Query.parse("-a foo");

        assertThat(query.isAllSources(), is(true));
    }

    @Test
    public void not_isAllSources() {
        final Query query = Query.parse("foo");

        assertThat(query.isAllSources(), is(false));
    }

    @Test
    public void isBrief() {
        final Query query = Query.parse("-b 10.0.0.0");

        assertThat(query.isBriefAbuseContact(), is(true));
    }

    @Test
    public void isBrief_forces_grouping_and_irt() {
        final Query query = Query.parse("-b -C 10.0.0.0");

        assertThat(query.isBriefAbuseContact(), is(true));
        assertThat(query.isGrouping(), is(false));
        assertThat(query.isReturningIrt(), is(true));
    }

    @Test
    public void not_isBrief() {
        final Query query = Query.parse("foo");

        assertThat(query.isBriefAbuseContact(), is(false));
    }

    @Test
    public void isKeysOnly() {
        final Query query = Query.parse("-K 10.0.0.0");

        assertThat(query.isKeysOnly(), is(true));
    }

    @Test
    public void isKeysOnly_forces_non_grouping() {
        final Query query = Query.parse("-K 10.0.0.0");

        assertThat(query.isKeysOnly(), is(true));
        assertThat(query.isGrouping(), is(false));
    }

    @Test
    public void not_isKeysOnly() {
        final Query query = Query.parse("foo");

        assertThat(query.isKeysOnly(), is(false));
    }

    @Test
    public void not_related_when_isKeysOnly() {
        final Query query = Query.parse("-K 10.0.0.0");

        assertThat(query.isReturningReferencedObjects(), is(false));
    }

    @Test
    public void not_irt_when_isKeysOnline() {
        final Query query = Query.parse("-c -K 10.0.0.0");

        assertThat(query.isReturningIrt(), is(false));
    }

    @Test
    public void hasOption() {
        final Query query = Query.parse("-s RIPE -T inetnum 10.0.0.0");

        assertThat(query.hasOption(QueryFlag.SOURCES), is(true));
        assertThat(query.hasOption(QueryFlag.SELECT_TYPES), is(true));
        assertThat(query.hasOption(QueryFlag.NO_REFERENCED), is(false));
    }

    @Test
    public void validTemplateQuery() {
        final Query query = Query.parse("-t person");

        assertThat(query.hasOption(QueryFlag.TEMPLATE), is(true));
        assertThat(query.isTemplate(), is(true));
        assertThat(query.getTemplateOption(), is("person"));
    }

    @Test
    public void templateQueryMultipleArguments() {
        assertThrows(QueryException.class, () -> {
            Query query = Query.parse("-t person -t role");
            query.getTemplateOption();
        });

    }

    @Test
    public void validVerboseTemplateQuery() {
        final Query query = Query.parse("-v person");

        assertThat(query.hasOption(QueryFlag.VERBOSE), is(true));
        assertThat(query.isVerbose(), is(true));
        assertThat(query.getVerboseOption(), is("person"));
    }

    @Test
    public void verboseTemplateQueryMultipleArguments() {
        assertThrows(QueryException.class, () -> {
            Query query = Query.parse("-v person -v role");
            query.getVerboseOption();
        });
    }

    @Test
    public void systemInfoWithExtraFlagAndArgument() {
        final Query query = Query.parse("-q version -t person");

        assertThat(query.getSystemInfoOption(), is(Query.SystemInfoOption.VERSION));
        assertThat(query.getTemplateOption(), is("person"));
    }

    @Test
    public void multiple_match_operations() {
        try {
            Query.parse("-m -x 10.0.0.0");
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessages(), contains(QueryMessages.duplicateIpFlagsPassed()));
        }
    }

    @Test
    public void match_operations_without_ipkey() {
        final Query query = Query.parse("-m test");

        assertThat(query.getWarnings(), hasItem(QueryMessages.uselessIpFlagPassed()));
    }

    @Test
    public void invalid_combination() {
        try {
            Query.parse("-b -F 10.0.0.0");
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessages(), contains(QueryMessages.invalidCombinationOfFlags("-b, --abuse-contact", "-F, --brief")));
        }
    }

    @Test
    public void brief_without_ipkey() {
        try {
            Query.parse("-b test");
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessages(), contains(QueryMessages.malformedQuery()));
        }
    }

    @Test
    public void brief_not_showing_referenced_objects() {
        final Query query = Query.parse("-b 10.0.0.0");

        assertThat(query.isReturningReferencedObjects(), is(false));
    }

    @Test
    public void brief_not_grouping() {
        final Query query = Query.parse("-b 10.0.0.0");

        assertThat(query.isGrouping(), is(false));
    }

    @Test
    public void unsupportedQuery() {
        try {
            Query.parse("...");
            fail("Expected exception");
        } catch (QueryException e) {
            assertThat(e.getMessages(), contains(QueryMessages.invalidSearchKey()));
        }
    }

    @Test
    public void nonVersionQuery() {
        final Query query = Query.parse("10.0.0.0");

        assertThat(query.hasOption(QueryFlag.LIST_VERSIONS), is(false));
        assertThat(query.isVersionList(), is(false));
        assertThat(query.hasOption(QueryFlag.SHOW_VERSION), is(false));
        assertThat(query.isObjectVersion(), is(false));
    }

    @Test
    public void versionlistQuery() {
        final Query query = Query.parse("--list-versions 10.0.0.0");

        assertThat(query.hasOption(QueryFlag.LIST_VERSIONS), is(true));
        assertThat(query.isVersionList(), is(true));
    }

    @Test
    public void versionQuery() {
        final Query query = Query.parse("--show-version 1 10.0.0.0");

        assertThat(query.hasOption(QueryFlag.SHOW_VERSION), is(true));
        assertThat(query.isObjectVersion(), is(true));
    }

    @Test
    public void allow_only_k_T_and_V_options_for_version_queries() {
        final String[] validQueries = {
                "--show-version 1 AS12 -k",
                "--show-version 1 AS12 -V fred",
                "--show-version 1 AS12 -k -V fred",
                "--list-versions AS12 -k -V fred",
                "--list-versions AS12 -V fred",
                "--list-versions AS12 -k",
                "--diff-versions 1:2 AS12",
                "--diff-versions 1:2 AS12 -k",
                "--diff-versions 1:2 AS12 -V fred",
                "--show-version 1 AS12 -T aut-num",
        };

        for (String query : validQueries) {
            Query.parse(query);
        }

        final String[] invalidQueries = {
                "--show-version 1 AS12 -B",
                "--list-versions AS12 -G",
                "--list-versions AS12 -k --show-version 1 AS12",
                "--diff-versions 1:2 AS12 -k --show-version 1",
                "--diff-versions 1:2 AS12 -B"
        };

        for (String query : invalidQueries) {
            try {
                Query.parse(query);
                fail(String.format("%s should not succeed", query));
            } catch (final QueryException e) {
                assertThat(e.getMessage(), containsString("cannot be used together"));
            }
        }
    }

    @Test
    public void versionDiffQuery() {
        final Query query = Query.parse("--diff-versions 1:2 10.0.0.0");

        assertThat(query.hasOption(QueryFlag.DIFF_VERSIONS), is(true));
        assertThat(query.isVersionDiff(), is(true));
    }


    @Test
    public void grs_search_types_specified_none() {
        final Query query = Query.parse("--resource 10.0.0.0");

        assertThat(query.getObjectTypes(), contains(
                ObjectType.INETNUM,
                ObjectType.ROUTE,
                ObjectType.DOMAIN));
    }

    @Test
    public void grs_search_types_specified_single() {
        final Query query = Query.parse("--resource -Tinetnum 10.0.0.0");

        assertThat(query.getObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void inverse_query_should_not_filter_object_types() {
        final Query query = Query.parse("-i nic-hdl 10.0.0.1");

        assertThat(query.getObjectTypes(), hasSize(21));
    }

    @Test
    public void grs_search_types_specified_non_resource() {
        final Query query = Query.parse("--resource -Tinetnum,mntner 10.0.0.0");

        assertThat(query.getObjectTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void grs_enables_all_sources() {
        final Query query = Query.parse("--resource 10.0.0.0");

        assertThat(query.isAllSources(), is(false));
        assertThat(query.isResource(), is(true));
    }
}
