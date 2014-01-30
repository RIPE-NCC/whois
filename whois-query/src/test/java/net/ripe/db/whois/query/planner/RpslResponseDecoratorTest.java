
package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.executor.decorators.DummifyDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterPersonalDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterPlaceholdersDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterTagsDecorator;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.Fixture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: [AH] this should have been an integration test

@RunWith(MockitoJUnitRunner.class)
public class RpslResponseDecoratorTest {

    @Mock SourceContext sourceContext;
    @Mock FilterPersonalDecorator filterPersonalDecorator;
    @Mock RpslObjectDao rpslObjectDaoMock;
    @Mock PrimaryObjectDecorator decorator;
    @Mock AbuseCFinder abuseCFinder;
    @Mock DummifyDecorator dummifyDecorator;
    @Mock FilterTagsDecorator filterTagsDecorator;
    @Mock FilterPlaceholdersDecorator filterPlaceholdersDecorator;
    @Mock SsoTokenTranslator ssoTokenTranslator;
    @Mock CrowdClient crowdClient;
    @InjectMocks AbuseCInfoDecorator abuseCInfoDecorator;

    RpslResponseDecorator subject;

    @Before
    public void setup() {
        subject = new RpslResponseDecorator(rpslObjectDaoMock,
                filterPersonalDecorator,
                dummifyDecorator,
                sourceContext,
                abuseCFinder,
                filterTagsDecorator,
                filterPlaceholdersDecorator,
                abuseCInfoDecorator,
                ssoTokenTranslator,
                crowdClient,
                decorator);
        when(sourceContext.getWhoisSlaveSource()).thenReturn(Source.slave("RIPE"));
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("RIPE"));
        when(sourceContext.isAcl()).thenReturn(true);
        when(sourceContext.isMain()).thenReturn(true);
        Fixture.mockRpslObjectDaoLoadingBehavior(rpslObjectDaoMock);

        decoratorPassthrough(filterPersonalDecorator, filterPlaceholdersDecorator, filterTagsDecorator, dummifyDecorator);
    }

    private static void decoratorPassthrough(ResponseDecorator... responseDecorator) {
        for (ResponseDecorator decorator : responseDecorator) {
            when(decorator.decorate(any(Query.class), any(Iterable.class))).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return invocationOnMock.getArguments()[1];
                }
            });
        }
    }

    @Test
    public void shouldReturnNoSearchKeySpecifiedWhenSearchKeyIsNotSpecified() {
        final String response = execute("-B -G -r -T inetnum -x asd");

        assertThat(response, is(""));
    }

    @Test
    public void shouldAddGroupingHeader() {
        when(sourceContext.isMain()).thenReturn(false);
        final String response = execute("-r -B -T mntner FOO-MNT", RpslObject.parse(1, "mntner: FOO-MNT\n"));
        assertThat(response, is("" +
                QueryMessages.relatedTo("FOO-MNT") + "\n" +
                "mntner:         FOO-MNT\n\n"));
    }

    @Test
    public void shouldSuppressGroupingHeader_shorthand() {
        when(sourceContext.isMain()).thenReturn(false);
        final String response = execute("-r -F -G -B -T mntner FOO-MNT", RpslObject.parse(1, "mntner: FOO-MNT\n"));
        assertEquals("*mt: FOO-MNT\n\n", response);
    }

    @Test
    public void brief_strips_all_but_inetnum_and_inet6num() {
        final RpslObject inet = RpslObject.parse(1, "inetnum: 10.0.0.0\nmntner: TEST\norg: ORG1-TEST");
        final RpslObject inet6 = RpslObject.parse(1, "inet6num: ::0/0\nmntner: TEST\norg: ORG1-TEST");

        when(abuseCFinder.getAbuseContacts(any(RpslObject.class))).thenReturn(Maps.<CIString, CIString>newHashMap());

        final String response = execute("-b 10.0.0.0", inet, inet6);
        assertThat(response, is(
                "inetnum:        10.0.0.0\n" +
                        "\n" +
                        "inet6num:       ::0/0\n" +
                        "\n"));
    }

    @Test
    public void keys() {
        when(sourceContext.isMain()).thenReturn(false);
        final String response = execute("-K 193.0.0.0/21", RpslObject.parse(1, "" +
                "route:          193.0.0.0/21\n" +
                "descr:          RIPE-NCC\n" +
                "origin:         AS3333\n" +
                "mnt-by:         RIPE-NCC-MNT\n" +
                "source:         RIPE # Filtered"));
        assertThat(response, is(QueryMessages.primaryKeysOnlyNotice() + "\nroute:          193.0.0.0/21\norigin:         AS3333\n\n"));
    }

    @Test
    public void keys_no_results() {
        final String response = execute("-K 193.0.0.0/21");
        assertThat(response, is(QueryMessages.primaryKeysOnlyNotice() + "\n"));
    }

    @Test
    public void shouldSuppressGroupingHeader() {
        when(sourceContext.isMain()).thenReturn(false);
        final String response = execute("-r -G -B -T mntner FOO-MNT", RpslObject.parse(1, "mntner: FOO-MNT\n"));
        assertThat(response, is("mntner:         FOO-MNT\n\n"));
    }

    @Test
    public void shouldAddFilterNoticeOnce() {
        when(sourceContext.isMain()).thenReturn(false);
        final String response = execute("-r -G -T organisation FOO-ORG",
                RpslObject.parse(1, "organisation: FOO-ORG\nsource: RIPE\n"),
                RpslObject.parse(1, "organisation: BAR-ORG\nsource: RIPE\n"));

        assertEquals("" +
                QueryMessages.outputFilterNotice() +
                "\n" +
                "organisation:   FOO-ORG\n" +
                "source:         RIPE\n" +
                "\n" +
                "organisation:   BAR-ORG\n" +
                "source:         RIPE\n" +
                "\n", response);
    }

    @Test
    public void shouldNotFilterWhenNoAclUsed() {
        when(sourceContext.isAcl()).thenReturn(false);
        when(sourceContext.isMain()).thenReturn(false);

        final String response = execute("-r -G -T organisation FOO-MNT",
                RpslObject.parse(1, "organisation: FOO-ORG\nsource: RIPE\n"),
                RpslObject.parse(1, "organisation: BAR-ORG\nsource: RIPE\n"));

        assertEquals("" +
                "organisation:   FOO-ORG\n" +
                "source:         RIPE\n" +
                "\n" +
                "organisation:   BAR-ORG\n" +
                "source:         RIPE\n" +
                "\n", response);
    }

    @Test
    public void shouldNotFilterForGrsSources() {
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("APNIC-GRS"));
        when(sourceContext.isAcl()).thenReturn(false);
        when(sourceContext.isMain()).thenReturn(false);

        final String response = execute("-r -G -T organisation FOO-MNT",
                RpslObject.parse(1, "organisation: FOO-ORG\nsource: APNIC-GRS\n"),
                RpslObject.parse(1, "organisation: BAR-ORG\nsource: APNIC-GRS\n"));

        assertEquals("" +
                "organisation:   FOO-ORG\n" +
                "source:         APNIC-GRS\n" +
                "\n" +
                "organisation:   BAR-ORG\n" +
                "source:         APNIC-GRS\n" +
                "\n", response);
    }

    @Test
    public void getResponseForPersonQueryNotFiltered() {
        final String response = execute("-r -B -T person DH3037-RIPE",
                RpslObject.parse(1, "person:david hilario\nnic-hdl:DH3037-RIPE\n"));

        assertTrue(response.contains("DH3037-RIPE"));
        assertFalse(response.contains("no entries found"));
    }

    @Test
    public void getResponseForPersonQueryFiltered() {

        final String response = execute("-r -T person DH3037-RIPE",
                RpslObject.parse(1, "person:david hilario\nnic-hdl:DH3037-RIPE\n"));

        assertTrue(response.contains("DH3037-RIPE"));
        assertFalse(response.contains("no entries found"));
    }

    @Test
    public void non_grouping_and_recursive_no_rpsl_objects() {
        String result = execute("-G -B -T inetnum 10.0.0.0", RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.0"));

        assertThat(result, is("% No abuse contact registered for 10.0.0.0 - 10.0.0.0\n\ninetnum:        10.0.0.0 - 10.0.0.0\n\n"));
    }

    @Test
    public void filter_mntner_pgpkey() {
        String result = execute(
                "-G -B -T inetnum 10.0.0.0",
                RpslObject.parse(1, "mntner: FOO-MNT\nauth: PGPKEY-ASD\nsource: RIPE\n"));

        assertThat(result, containsString("auth:           PGPKEY-ASD"));
    }

    public void filter_mntner_md5() {

        String result = execute(
                "-G -B -T inetnum 10.0.0.0",
                RpslObject.parse(1, "mntner: FOO-MNT\nauth: MD5-PW ABC\nsource: RIPE\n"));

        assertThat(result, containsString("auth:           MD5-PW # Filtered"));
    }

    @Test
    public void filter_irt() {
        String result = execute(
                "-G -B -T irt IRT-MNT",
                RpslObject.parse(1, "irt: IRT-MNT\nauth: PGPKEY-ASD\nsource: RIPE\n"));

        assertThat(result, containsString("auth"));
    }

    @Test
    public void non_grouping_and_recursive_no_recursive_objects() {
        final RpslObject inetnum = RpslObject.parse(1, "inetnum: 10.0.0.0\norg:ORG1-TEST\nstatus:OTHER");
        final HashMap<CIString, CIString> map = Maps.newHashMap();
        map.put(CIString.ciString("10.0.0.0"), CIString.ciString("abuse@ripe.net"));

        when(abuseCFinder.getAbuseContacts(inetnum)).thenReturn(map);

        String result = execute("-G -B -T inetnum 10.0.0.0", inetnum);

        assertThat(result, is("" +
                "% Abuse contact for '10.0.0.0' is 'abuse@ripe.net'\n" +
                "\n" +
                "inetnum:        10.0.0.0\n" +
                "org:            ORG1-TEST\n" +
                "status:         OTHER\n\n"));
    }

    @Test
    public void non_grouping_and_recursive_with_recursive_objects() {
        RpslObject rpslObject = RpslObject.parse("inetnum: 10.0.0.0\ntech-c:NICHDL\norg:ORG1-TEST\nstatus:OTHER");
        final HashMap<CIString, CIString> map = Maps.newHashMap();
        map.put(CIString.ciString("10.0.0.0"), CIString.ciString("abuse@ripe.net"));
        when(decorator.appliesToQuery(any(Query.class))).thenReturn(true);
        when(abuseCFinder.getAbuseContacts(rpslObject)).thenReturn(map);

        String result = execute("-G -B -T inetnum 10.0.0.0", rpslObject);

        verify(decorator, atLeastOnce()).decorate(any(Query.class), eq(rpslObject));
        assertThat(result, is("" +
                "% Abuse contact for '10.0.0.0' is 'abuse@ripe.net'\n\n" +
                "inetnum:        10.0.0.0\n" +
                "tech-c:         NICHDL\n" +
                "org:            ORG1-TEST\n" +
                "status:         OTHER\n" +
                "\n"));
    }

    @Test
    public void non_grouping_and_recursive_with_recursive_objects_sorts_and_is_unique() {
        final RpslObject object1 = RpslObject.parse(1, "inetnum: 10.0.0.1\ntech-c:NICHDL\nadmin-c:NICHDL\nstatus:OTHER");
        final RpslObject object2 = RpslObject.parse(1, "inetnum: 10.0.0.2\ntech-c:NICHDL\nadmin-c:NICHDL\nstatus:OTHER");

        when(decorator.appliesToQuery(any(Query.class))).thenReturn(true);
        when(decorator.decorate(any(Query.class), any(RpslObject.class))).thenReturn(Arrays.asList(
                new RpslObjectInfo(1, ObjectType.ROUTE, "z"),
                new RpslObjectInfo(2, ObjectType.ROUTE, "a")
        ));
        when(rpslObjectDaoMock.getById(anyInt())).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return RpslObject.parse((Integer) (invocation.getArguments()[0]), ("mntner: test" + invocation.getArguments()[0].toString()).getBytes());
            }
        });

        final HashMap<CIString, CIString> map1 = Maps.newHashMap();
        map1.put(CIString.ciString("10.0.0.1"), CIString.ciString("abuse@ripe.net"));
        when(abuseCFinder.getAbuseContacts(object1)).thenReturn(map1);

        final HashMap<CIString, CIString> map2 = Maps.newHashMap();
        map2.put(CIString.ciString("10.0.0.2"), CIString.ciString("abuse@ripe.net"));
        when(abuseCFinder.getAbuseContacts(object2)).thenReturn(map2);

        String result = execute("-G -B -T inetnum 10.0.0.0", object1, object2);

        verify(decorator, atLeastOnce()).decorate(any(Query.class), eq(object1));
        verify(decorator, atLeastOnce()).decorate(any(Query.class), eq(object2));
        assertThat(result, is("" +
                "% Abuse contact for '10.0.0.1' is 'abuse@ripe.net'\n\n" +
                "inetnum:        10.0.0.1\n" +
                "tech-c:         NICHDL\n" +
                "admin-c:        NICHDL\n" +
                "status:         OTHER\n" +
                "\n" +
                "% Abuse contact for '10.0.0.2' is 'abuse@ripe.net'\n\n" +
                "inetnum:        10.0.0.2\n" +
                "tech-c:         NICHDL\n" +
                "admin-c:        NICHDL\n" +
                "status:         OTHER\n" +
                "\n" +
                "mntner:         test2\n" +
                "\n" +
                "mntner:         test1\n" +
                "\n"));
    }

    @Test
    public void grouping_and_recursive_with_recursive_objects_sorts() {
        final RpslObject object1 = RpslObject.parse(1, "inetnum: 10.0.0.1\ntech-c:NICHDL\nadmin-c:NICHDL\norg: ORG1-TEST\nstatus:OTHER");
        final RpslObject object2 = RpslObject.parse(1, "inetnum: 10.0.0.2\ntech-c:NICHDL\nadmin-c:NICHDL\norg: ORG1-TEST\nstatus:OTHER");

        when(decorator.appliesToQuery(any(Query.class))).thenReturn(true);
        when(decorator.decorate(any(Query.class), any(RpslObject.class))).thenReturn(Arrays.asList(
                new RpslObjectInfo(1, ObjectType.ROUTE, "z"),
                new RpslObjectInfo(2, ObjectType.ROUTE, "a")
        ));

        when(rpslObjectDaoMock.getById(anyInt())).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return RpslObject.parse((Integer) (invocation.getArguments()[0]), ("mntner: test" + invocation.getArguments()[0].toString()).getBytes());
            }
        });

        final HashMap<CIString, CIString> map1 = Maps.newHashMap();
        map1.put(CIString.ciString("10.0.0.1"), CIString.ciString("abuse@ripe.net"));

        final HashMap<CIString, CIString> map2 = Maps.newHashMap();
        map2.put(CIString.ciString("10.0.0.2"), CIString.ciString("abuse@ripe.net"));

        when(abuseCFinder.getAbuseContacts(object1)).thenReturn(map1);
        when(abuseCFinder.getAbuseContacts(object2)).thenReturn(map2);

        String result = execute("-B -T inetnum 10.0.0.0", object1, object2);

        assertThat(result, is("" +
                QueryMessages.relatedTo("10.0.0.1") + "\n" +
                "% Abuse contact for '10.0.0.1' is 'abuse@ripe.net'\n\n" +
                "inetnum:        10.0.0.1\n" +
                "tech-c:         NICHDL\n" +
                "admin-c:        NICHDL\n" +
                "org:            ORG1-TEST\n" +
                "status:         OTHER\n" +
                "\n" +
                "mntner:         test2\n" +
                "\n" +
                "mntner:         test1\n" +
                "\n" +
                QueryMessages.relatedTo("10.0.0.2") + "\n" +
                "% Abuse contact for '10.0.0.2' is 'abuse@ripe.net'\n\n" +
                "inetnum:        10.0.0.2\n" +
                "tech-c:         NICHDL\n" +
                "admin-c:        NICHDL\n" +
                "org:            ORG1-TEST\n" +
                "status:         OTHER\n" +
                "\n" +
                "mntner:         test2\n" +
                "\n" +
                "mntner:         test1\n" +
                "\n")
        );
    }

    private String execute(final String query, final ResponseObject... responseObjects) {
        return getResponseTextAsString(subject.getResponse(Query.parse(query), Lists.newArrayList(responseObjects)));
    }

    private String getResponseTextAsString(final Iterable<? extends ResponseObject> responseObjects) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (ResponseObject responseObject : responseObjects) {
                responseObject.writeTo(baos);
                baos.write('\n');
            }
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void dummify_response() {
        when(sourceContext.getGrsSourceNames()).thenReturn(ciSet("GRS1", "GRS2"));
        when(sourceContext.isDummificationRequired()).thenReturn(true);
        when(sourceContext.isMain()).thenReturn(false);
        when(dummifyDecorator.decorate(any(Query.class), any(Iterable.class))).thenReturn(Collections.EMPTY_LIST);

        final String response = execute("-s TEST-GRS -T person test", RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST"));
        assertThat(response, is("" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n\n"));

        verify(dummifyDecorator, atLeastOnce()).decorate(any(Query.class), any(Iterable.class));
    }

    @Test
    public void dummify_filter() {
        when(sourceContext.getGrsSourceNames()).thenReturn(ciSet("GRS1", "GRS2"));
        when(sourceContext.isDummificationRequired()).thenReturn(true);
        when(dummifyDecorator.decorate(any(Query.class), any(Iterable.class))).thenReturn(Collections.EMPTY_LIST);

        final RpslObject inetnum = RpslObject.parse(1, "inetnum: 10.0.0.0\norg:ORG1-TEST");

        final String response = execute("-G -B -T inetnum 10.0.0.0", inetnum);
        assertThat(response, is(""));

        verify(dummifyDecorator, atLeastOnce()).decorate(any(Query.class), any(Iterable.class));
    }
}
