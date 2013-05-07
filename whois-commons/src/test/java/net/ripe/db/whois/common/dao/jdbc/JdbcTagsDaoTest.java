package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JdbcTagsDaoTest extends AbstractDaoTest {
    @Autowired TagsDao subject;

    @Before
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @After
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void findTags() {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(2, \"unref\", \"25\")");

        final List<Tag> tags = subject.getTags(2);

        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getObjectId(), is(2));
        assertThat(tags.get(0).getValue(), is("25"));
    }

    @Test
    public void tagNotFound() {
        final List<Tag> tags = subject.getTags(3);

        assertTrue(tags.isEmpty());
    }

    @Test
    public void createTag() {
        subject.createTag(new Tag(CIString.ciString("unref"), 2, "15"));

        final List<Tag> tags = subject.getTags(2);
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getValue(), is("15"));
        assertThat(tags.get(0).getObjectId(), is(2));
    }

    @Test
    public void createTags() {
        subject.createTags(Lists.newArrayList(new Tag(CIString.ciString("unref"), 2, "15")));

        final List<Tag> tags = subject.getTags(2);
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getValue(), is("15"));
        assertThat(tags.get(0).getObjectId(), is(2));
    }

    @Test
    public void regenerateUnref() {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(4, \"unref\", \"27\")");
        assertThat(subject.getTags(4).size(), is(1));

        final Tag tag1 = new Tag(CIString.ciString("unref"), 1, "24");
        final Tag tag2 = new Tag(CIString.ciString("unref"), 2, "15");
        final Tag tag3 = new Tag(CIString.ciString("unref"), 3, "12");
        subject.rebuild(CIString.ciString("unref"), Lists.newArrayList(tag1, tag2, tag3));

        assertThat(subject.getTags(4).size(), is(0));

        final Tag foundTag1 = subject.getTags(1).get(0);
        assertThat(foundTag1.getType(), is(CIString.ciString("unref")));
        assertThat(foundTag1.getValue(), is("24"));

        final Tag foundTag2 = subject.getTags(2).get(0);
        assertThat(foundTag2.getType(), is(CIString.ciString("unref")));
        assertThat(foundTag2.getValue(), is("15"));

        final Tag foundTag3 = subject.getTags(3).get(0);
        assertThat(foundTag3.getType(), is(CIString.ciString("unref")));
        assertThat(foundTag3.getValue(), is("12"));
    }

    @Test
    public void deleteTag() {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(1, \"unref\", \"87\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(2, \"unref\", \"54\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(3, \"unref\", \"77\")");

        subject.deleteTag(CIString.ciString("unref"), 2);

        assertThat(subject.getTags(1).size(), is(1));
        assertThat(subject.getTags(2).size(), is(0));
        assertThat(subject.getTags(3).size(), is(1));
    }

    @Test
    public void deleteTags() {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(1, \"unref\", \"87\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(2, \"unref\", \"54\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(3, \"unref\", \"77\")");

        subject.deleteTags(CIString.ciString("unref"), Lists.newArrayList(2));

        assertThat(subject.getTags(1).size(), is(1));
        assertThat(subject.getTags(2).size(), is(0));
        assertThat(subject.getTags(3).size(), is(1));
    }

    @Test
    public void deleteTagsOfType() {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(1, \"unref\", \"87\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(2, \"unref\", \"54\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(3, \"unref\", \"77\")");
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) VALUES(4, \"other\", \"Other Data\")");

        subject.deleteTagsOfType(CIString.ciString("unref"));

        assertThat(subject.getTags(1).size(), is(0));
        assertThat(subject.getTags(2).size(), is(0));
        assertThat(subject.getTags(3).size(), is(0));
        assertThat(databaseHelper.getWhoisTemplate().queryForInt("SELECT count(*) FROM tags WHERE tag_id != \"unref\""), is(1));
    }
}
