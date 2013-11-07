package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.mapper.AbstractWhoisObjectMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AbstractWhoisObjectMapperTest {

    @Mock
    private AbstractWhoisObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new AbstractWhoisObjectMapper(null) {
            @Override
            Attribute buildAttribute(RpslAttribute attribute, CIString value, String comment, String source) {
                return null;
            }
        };
    }

    @Test
    public void map_versions() {
        final DeletedVersionResponseObject deleted = new DeletedVersionResponseObject(new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102");

        final List<VersionResponseObject> versionInfos = Lists.newArrayList(
                new VersionResponseObject(2, Operation.UPDATE, 3, new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102"),
                new VersionResponseObject(2, Operation.UPDATE, 4, new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102"));

        final List<WhoisVersion> whoisVersions = mapper.mapVersions(Lists.newArrayList(deleted), versionInfos);

        assertThat(whoisVersions, hasSize(3));
        final WhoisVersion deletedVersion = whoisVersions.get(0);
        assertThat(deletedVersion.getOperation(), nullValue());
        assertThat(deletedVersion.getRevision(), nullValue());
        assertThat(deletedVersion.getDeletedDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion1 = whoisVersions.get(1);
        assertThat(whoisVersion1.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion1.getRevision(), is(3));
        assertThat(whoisVersion1.getDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion2 = whoisVersions.get(2);
        assertThat(whoisVersion2.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion2.getRevision(), is(4));
        assertThat(whoisVersion2.getDate(), is(not(nullValue())));
    }

    @Test
    public void map_tags() {
        final List<WhoisTag> tags = mapper.map(RpslObject.parse("mntner: TEST-MNT\nsource: TEST"),
                Lists.newArrayList(
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("foo"), "foo data"),
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("bar"), "bar data"),
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("barf"), "barf data"))).getTags();

        assertThat(tags, hasSize(3));
        final WhoisTag tag1 = tags.get(0);
        assertThat(tag1.getId(), is("foo"));
        assertThat(tag1.getData(), is("foo data"));

        final WhoisTag tag2 = tags.get(1);
        assertThat(tag2.getId(), is("bar"));
        assertThat(tag2.getData(), is("bar data"));

        final WhoisTag tag3 = tags.get(2);
        assertThat(tag3.getId(), is("barf"));
        assertThat(tag3.getData(), is("barf data"));
    }
}
