package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class FilterPersonalDecoratorTest {
    @InjectMocks private FilterPersonalDecorator subject;

    private final ResponseObject filterMessage = new MessageObject(QueryMessages.noPersonal());

    private final ResponseObject relatedMessage = new MessageObject("% Related");

    private final ResponseObject person = RpslObject.parse("" +
            "person:        Test Person\n" +
            "nic-hdl:       TP1-TEST");

    private final ResponseObject role = RpslObject.parse("" +
            "role:          Test Role\n" +
            "nic-hdl:       TR1-TEST");

    private final ResponseObject abuseRole = RpslObject.parse("" +
            "role:          Abuse Role\n" +
            "nic-hdl:       AR1-TEST\n" +
            "abuse-mailbox: abuse@me.not");

    private final ResponseObject maintainer = RpslObject.parse("" +
            "mntner:        TEST-MNT");

    @Test
    public void no_personal_filters_personal_objects() {
        final List<ResponseObject> input = Lists.newArrayList(relatedMessage, maintainer, role, person, abuseRole);

        final Query query = Query.parse(String.format("%s test", QueryFlag.NO_PERSONAL));
        final Set<ResponseObject> response = Sets.newLinkedHashSet(subject.decorate(query, input));
        assertThat(response, hasSize(3));
        assertThat(response, contains(filterMessage, relatedMessage, maintainer));
    }

    @Test
    public void no_personal_filters_explicit_type() {
        assertThrows(QueryException.class, () -> {
            Query.parse(String.format("%s %s role Test", QueryFlag.NO_PERSONAL.getLongFlag(), QueryFlag.SELECT_TYPES.getLongFlag()));
        });
    }

    @Test
    public void no_personal_message_only() {
        final List<ResponseObject> input = Lists.newArrayList(filterMessage);

        final Query query = Query.parse(String.format("%s unknown", QueryFlag.NO_PERSONAL));
        final Set<ResponseObject> response = Sets.newLinkedHashSet(subject.decorate(query, input));
        assertThat(response, hasSize(1));
        assertThat(response, contains(filterMessage));
    }

    @Test
    public void show_personal_filters_nothing() {
        final List<ResponseObject> input = Lists.newArrayList(relatedMessage, maintainer, role, person, abuseRole);

        final Query query = Query.parse(String.format("%s test", QueryFlag.SHOW_PERSONAL));
        final Set<ResponseObject> response = Sets.newLinkedHashSet(subject.decorate(query, input));
        assertThat(response, hasSize(5));
        assertThat(response, contains(relatedMessage, maintainer, role, person, abuseRole));
    }

    @Test
    public void show_personal_and_no_personal() {
        final List<ResponseObject> input = Lists.newArrayList(relatedMessage, maintainer, role, person, abuseRole);

        final Query query = Query.parse(String.format("%s %s test", QueryFlag.NO_PERSONAL, QueryFlag.SHOW_PERSONAL));
        final Set<ResponseObject> response = Sets.newLinkedHashSet(subject.decorate(query, input));
        assertThat(response, hasSize(5));
        assertThat(response, contains(relatedMessage, maintainer, role, person, abuseRole));
    }
}
