package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceTaggerJdbcTest extends AbstractSchedulerIntegrationTest {
    @Autowired ResourceTagger subject;
    @Autowired SourceContext sourceContext;
    @Autowired TagsDao tagsDao;
    @Autowired RpslObjectDao objectDao;
    @Mock Downloader downloader;

    GrsSource testGrsSource;
    AuthoritativeResource authoritativeResource;
    AuthoritativeResourceData authoritativeResourceData;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("grs.import.sources.tagRoutes", "TEST-GRS");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("grs.import.sources.tagRoutes");
    }

    @Before
    public void setUp() throws Exception {
        authoritativeResource = mock(AuthoritativeResource.class);
        when(authoritativeResource.getResourceTypes()).thenReturn(Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM));

        authoritativeResourceData = mock(AuthoritativeResourceData.class);
        when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);

        testGrsSource = new GrsSource("TEST-GRS", sourceContext, testDateTimeProvider, authoritativeResourceData, downloader) {
            @Override
            void acquireDump(final Path path) throws IOException {
            }

            @Override
            void handleObjects(final File file, final ObjectHandler handler) throws IOException {
            }
        };
    }

    @Test
    public void tagObjects() {
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inetnum:        193.0.0.0 - 193.0.7.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "inet6num:       2a01:4f8:191:34f1::/64\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "aut-num:        AS7\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "aut-num:        AS8\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "person:         Test person\n" +
                        "nic-hdl:        TP1-TEST\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS7"))).thenReturn(true);
        when(authoritativeResource.isMaintainedByRir(ObjectType.INETNUM, ciString("193.0.0.0 - 193.0.7.255"))).thenReturn(true);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2a01:4f8:191:34f1::/64"))).thenReturn(true);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.PERSON, ciString("TP1-TEST"))).thenReturn(true);

        subject.tagObjects(testGrsSource);

        final List<Tag> registryResources = tagsDao.getTagsOfType(ciString("TEST-REGISTRY-RESOURCE"));
        assertThat(registryResources, hasSize(2));
        for (final Tag tag : registryResources) {
            assertThat(tag.getType(), is(ciString("TEST-REGISTRY-RESOURCE")));

            final RpslObject rpslObject = objectDao.getById(tag.getObjectId());
            switch (rpslObject.getType()) {
                case AUT_NUM:
                    assertThat(rpslObject.getKey(), is(ciString("AS7")));
                    break;
                case INETNUM:
                    assertThat(rpslObject.getKey(), is(ciString("193.0.0.0 - 193.0.7.255")));
                    break;
                default:
                    fail("Unexpected type: " + rpslObject.getType());
                    break;
            }
        }

        final List<Tag> userResources = tagsDao.getTagsOfType(ciString("TEST-USER-RESOURCE"));
        assertThat(userResources, hasSize(1));
        for (final Tag tag : userResources) {
            assertThat(tag.getType(), is(ciString("TEST-USER-RESOURCE")));

            final RpslObject rpslObject = objectDao.getById(tag.getObjectId());
            switch (rpslObject.getType()) {
                case INET6NUM:
                    assertThat(rpslObject.getKey(), is(ciString("2a01:4f8:191:34f1::/64")));
                    break;
                default:
                    fail("Unexpected type: " + rpslObject.getType());
                    break;
            }
        }
    }

    @Test
    public void tagRouteNone() {
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS1"))).thenReturn(false);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("193.1.0.0/24"))).thenReturn(false);
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "aut-num:        AS1\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "route:          193.1.0.0/24\n" +
                        "origin:         AS1\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        subject.tagObjects(testGrsSource);

        final List<Tag> tags = tagsDao.getTags(objectDao.findByKey(ObjectType.ROUTE, "193.1.0.0/24AS1").getObjectId());
        assertThat(tags, hasSize(0));
    }


    @Test
    public void tagRoutePrefixOnly() {
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS1"))).thenReturn(false);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("193.1.0.0/24"))).thenReturn(true);
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "aut-num:        AS1\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "route:          193.1.0.0/24\n" +
                        "origin:         AS1\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        subject.tagObjects(testGrsSource);

        final List<Tag> tagsByType = tagsDao.getTagsOfType(ciString("TEST-PREFIX-ONLY-RESOURCE"));
        assertThat(tagsByType, hasSize(1));
        assertThat(objectDao.getById(tagsByType.get(0).getObjectId()).getKey().toString(), is("193.1.0.0/24AS1"));
    }

    @Test
    public void tagRouteASNOnly() {
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS1"))).thenReturn(true);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("193.1.0.0/24"))).thenReturn(false);
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "aut-num:        AS1\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "route:          193.1.0.0/24\n" +
                        "origin:         AS1\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        subject.tagObjects(testGrsSource);

        final List<Tag> tagsByType = tagsDao.getTagsOfType(ciString("TEST-ASN-ONLY-RESOURCE"));
        assertThat(tagsByType, hasSize(1));
        assertThat(objectDao.getById(tagsByType.get(0).getObjectId()).getKey().toString(), is("193.1.0.0/24AS1"));
    }

    @Test
    public void tagRouteBothPrefixAndASN() {
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS1"))).thenReturn(true);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("193.1.0.0/24"))).thenReturn(true);
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "aut-num:        AS1\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "route:          193.1.0.0/24\n" +
                        "origin:         AS1\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        subject.tagObjects(testGrsSource);

        final List<Tag> tagsByType = tagsDao.getTagsOfType(ciString("TEST-ASN-AND-PREFIX-RESOURCE"));
        assertThat(tagsByType, hasSize(1));
        assertThat(objectDao.getById(tagsByType.get(0).getObjectId()).getKey().toString(), is("193.1.0.0/24AS1"));
    }

    @Test
    public void tagRoute6BothPrefixAndASN() {
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS1"))).thenReturn(true);
        when(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:2002::/64"))).thenReturn(true);
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse("" +
                        "inet6num:       2001:2002::/64\n" +
                        "netname:        RIPE-NCC\n" +
                        "source:         TEST-GRS\n"),
                RpslObject.parse("" +
                        "aut-num:        AS1\n" +
                        "source:         TEST-GRS\n"
                ),
                RpslObject.parse("" +
                        "route6:         2001:2002::/64\n" +
                        "origin:         AS1\n" +
                        "source:         TEST-GRS\n"
                )
        ));

        subject.tagObjects(testGrsSource);

        final List<Tag> tagsByType = tagsDao.getTagsOfType(ciString("TEST-ASN-AND-PREFIX-RESOURCE"));
        assertThat(tagsByType, hasSize(1));
        assertThat(objectDao.getById(tagsByType.get(0).getObjectId()).getKey().toString(), is("2001:2002::/64AS1"));
    }
}
