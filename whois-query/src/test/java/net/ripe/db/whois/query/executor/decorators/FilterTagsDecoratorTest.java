package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterTagsDecoratorTest {

    @Mock TagsDao tagsDao;
    @Mock RpslObjectDao objectDao;

    @InjectMocks
    FilterTagsDecorator subject;


    @Test
    public void unrefInfo_for_unreferenced_role() {
        when(tagsDao.getTags(1)).thenReturn(Lists.newArrayList(new Tag(CIString.ciString("unref"), 1, "34")));
        final RpslObject role = RpslObject.parse(1, "role: Test Role\nnic-hdl: TR1-TEST");
        final Query query = Query.parse("--show-tag-info TR1-TEST");

        final Iterable<? extends ResponseObject> result = subject.decorate(query, ImmutableList.of(role));

        final Iterator<? extends ResponseObject> iterator = result.iterator();
        assertThat(iterator.next() instanceof RpslObject, is(true));
        assertThat(iterator.next().toString(), is("% Tags relating to 'TR1-TEST'"));
        assertThat(new String(iterator.next().toByteArray()), is("% Unreferenced # 'TR1-TEST' will be deleted in 34 days\n"));
    }

    @Test
    public void no_unrefInfo_for_referenced_mntner() {
        when(tagsDao.getTags(1)).thenReturn(Lists.<Tag>newArrayList());
        final Query query = Query.parse("--show-tag-info TEST-MNT");

        final RpslObject mntner = RpslObject.parse(1, "mntner: TEST-MNT");
        final Iterable<? extends ResponseObject> result = subject.decorate(query, ImmutableList.of(mntner));

        final Iterator<? extends ResponseObject> iterator = result.iterator();
        final RpslObject object = (RpslObject) iterator.next();
        assertThat(mntner, is(object));
        assertFalse(iterator.hasNext());
    }


}
