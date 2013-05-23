package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceTaggerJdbcTest extends AbstractSchedulerIntegrationTest {
    @Autowired ResourceTagger subject;
    @Autowired SourceContext sourceContext;
    @Autowired DateTimeProvider dateTimeProvider;
    @Autowired TagsDao tagsDao;
    @Autowired RpslObjectDao objectDao;

    GrsSource grsSource;
    AuthoritativeResource authoritativeResource;
    AuthoritativeResourceData authoritativeResourceData;

    @Before
    public void setUp() throws Exception {
        authoritativeResource = mock(AuthoritativeResource.class);
        when(authoritativeResource.getResourceTypes()).thenReturn(Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM));

        authoritativeResourceData = mock(AuthoritativeResourceData.class);
        when(authoritativeResourceData.getAuthoritativeResource()).thenReturn(authoritativeResource);

        grsSource = new GrsSource("TEST-GRS", sourceContext, dateTimeProvider, authoritativeResourceData) {
            @Override
            void acquireDump(final File file) throws IOException {
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

        subject.tagObjects(grsSource, authoritativeResource);

        final List<Tag> tags = tagsDao.getTagsOfType(ciString("TEST_RESOURCE"));
        assertThat(tags, hasSize(3));

        for (final Tag tag : tags) {
            assertThat(tag.getType(), is(ciString("TEST_RESOURCE")));

            final RpslObject rpslObject = objectDao.getById(tag.getObjectId());
            switch (rpslObject.getType()) {
                case AUT_NUM:
                    assertThat(rpslObject.getKey(), is(ciString("AS7")));
                    assertThat(tag.getValue(), is("Registry maintained"));
                    break;
                case INETNUM:
                    assertThat(rpslObject.getKey(), is(ciString("193.0.0.0 - 193.0.7.255")));
                    assertThat(tag.getValue(), is("Registry maintained"));
                    break;
                case INET6NUM:
                    assertThat(rpslObject.getKey(), is(ciString("2a01:4f8:191:34f1::/64")));
                    assertThat(tag.getValue(), is("User maintained"));
                    break;
                default:
                    fail("Unexpected type: " + rpslObject.getType());
                    break;
            }
        }
    }
}
