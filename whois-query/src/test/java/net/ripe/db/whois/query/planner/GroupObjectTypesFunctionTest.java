package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.Fixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupObjectTypesFunctionTest {
    private Query query;

    @Mock private PrimaryObjectDecorator decorator;
    @Mock private RpslObjectDao rpslObjectDao;

    private GroupObjectTypesFunction subject;

    @BeforeEach
    public void setUp() {
        query = Query.parse("foo");
        subject = new GroupObjectTypesFunction(rpslObjectDao, query, Sets.newHashSet(decorator));
    }

    @Test
    public void apply_messageObject() {
        final ResponseObject input = new MessageObject("");
        final Iterable<ResponseObject> responseObjects = subject.apply(input);
        final Iterable<ResponseObject> relatedObjects = subject.getGroupedAfter();

        verify(decorator, times(0)).appliesToQuery(query);

        assertThat(responseObjects, contains(input));
        assertThat(Lists.newArrayList(relatedObjects), hasSize(0));
    }

    @Test
    public void apply_rpslObject_no_match() {
        final ResponseObject input = RpslObject.parse("inetnum:10.0.0.0");

        when(decorator.appliesToQuery(query)).thenReturn(false);

        final Iterable<ResponseObject> responseObjects = subject.apply(input);
        final Iterable<ResponseObject> relatedObjects = subject.getGroupedAfter();

        verify(decorator, times(0)).decorate(query, (RpslObject) input);

        assertThat(responseObjects, contains(input));
        assertThat(Lists.newArrayList(relatedObjects), hasSize(0));
    }

    @Test
    public void apply_rpslObject() {
        Fixture.mockRpslObjectDaoLoadingBehavior(rpslObjectDao);

        final ResponseObject input = RpslObject.parse("inetnum:10.0.0.0");

        final RpslObjectInfo info1 = new RpslObjectInfo(1, ObjectType.IRT, "IRT");
        final ResponseObject result1 = RpslObject.parse("irt:irt");

        final RpslObjectInfo info2 = new RpslObjectInfo(2, ObjectType.MNTNER, "MNTNER");
        final ResponseObject result2 = RpslObject.parse("mntner:mntner");

        when(decorator.appliesToQuery(query)).thenReturn(true);
        when(decorator.decorate(query, (RpslObject) input)).thenReturn(Arrays.asList(info1, info2));
        when(rpslObjectDao.getById(1)).thenReturn((RpslObject) result1);
        when(rpslObjectDao.getById(2)).thenReturn((RpslObject) result2);

        final Iterable<ResponseObject> responseObjects = subject.apply(input);
        final Iterable<ResponseObject> relatedObjects = subject.getGroupedAfter();

        assertThat(responseObjects, contains(input));
        assertThat(relatedObjects, contains(result2, result1));
    }
}
