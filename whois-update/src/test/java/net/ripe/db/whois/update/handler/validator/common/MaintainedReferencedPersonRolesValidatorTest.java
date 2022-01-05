package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MaintainedReferencedPersonRolesValidatorTest {
    @Mock
    private RpslObjectDao rpslObjectDao;

    private MaintainedReferencedPersonRolesValidator subject;

    @BeforeEach
    public void setUp() {
        subject = new MaintainedReferencedPersonRolesValidator(rpslObjectDao);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void no_person_objects() {
        final ObjectMessages messages = validateUpdate(subject, null, RpslObject.parse("mntner: foo\nunknown: asd"));
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void referenced_person_not_found() {
        when(rpslObjectDao.getByKey(eq(ObjectType.PERSON), anyString())).thenThrow(new EmptyResultDataAccessException(1));
        when(rpslObjectDao.getByKey(eq(ObjectType.ROLE), anyString())).thenThrow(new EmptyResultDataAccessException(1));
        final ObjectMessages messages = validateUpdate(subject, null, RpslObject.parse("mntner: foo\ntech-c: anyString"));
        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void referenced_person_maintained() {
        final String personName = "TECH-RIPE";
        final RpslObject personObject = RpslObject.parse("person: foo\nmnt-by: MNT\nnic-hdl: " + personName);

        final String roleName = "ADMIN-RIPE";
        final RpslObject roleObject = RpslObject.parse("role: foo\nmnt-by: MNT\nnic-hdl: " + roleName);

        when(rpslObjectDao.getByKey(ObjectType.PERSON, roleName)).thenThrow(EmptyResultDataAccessException.class);
        when(rpslObjectDao.getByKey(ObjectType.ROLE, roleName)).thenReturn(roleObject);
        when(rpslObjectDao.getByKey(ObjectType.PERSON, personName)).thenReturn(personObject);

        final ObjectMessages messages = validateUpdate(subject, null, RpslObject.parse("mntner: foo\ntech-c: " + personName + "\nadmin-c: " + roleName));

        assertThat(messages.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void referenced_person_not_maintained() {
        final String personName = "TECH-RIPE";
        final RpslObject personObject = RpslObject.parse("person: foo\nnic-hdl: " + personName);

        when(rpslObjectDao.getByKey(ObjectType.PERSON, personName)).thenReturn(personObject);

        final String roleName = "ADMIN-RIPE";
        final RpslObject roleObject = RpslObject.parse("role: foo\nnic-hdl: " + roleName);

        when(rpslObjectDao.getByKey(ObjectType.PERSON, roleName)).thenThrow(EmptyResultDataAccessException.class);
        when(rpslObjectDao.getByKey(ObjectType.ROLE, roleName)).thenReturn(roleObject);

        final ObjectMessages messages = validateUpdate(subject, null, RpslObject.parse("mntner: foo\ntech-c: " + personName + "\nadmin-c: " + roleName));

        assertThat(messages.getMessages().getAllMessages(), hasItems(
                UpdateMessages.referencedObjectMissingAttribute(ObjectType.PERSON, personName, AttributeType.MNT_BY),
                UpdateMessages.referencedObjectMissingAttribute(ObjectType.ROLE, roleName, AttributeType.MNT_BY)
        ));
    }
}
