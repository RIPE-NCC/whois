package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@Transactional
@RunWith(MockitoJUnitRunner.class)
public class UpdateObjectHandlerImplTest {

    @Mock UpdateContext updateContext;
    @Mock RpslObjectUpdateDao rpslObjectUpdateDao;
    @Mock DateTimeProvider dateTimeProvider;
    private UpdateObjectHandlerImpl subject;

    private final String RIPE_NCC_BA_MNT_MAINTAINER = "" +
            "mntner:            RIPE-NCC-BA-MNT\n" +
            "descr:             RIPE-NCC BA Maintainer\n" +
            "admin-c:           BADT-RIPE\n" +
            "upd-to:            bad@ripe.net\n" +
            "auth:              MD5-PW $1$LFXfnt7M$FhrMgErGJsTP9QIGbk8s9.\n" +
            "mnt-by:            RIPE-NCC-BA-MNT\n" +
            "referral-by:       RIPE-NCC-BA-MNT\n" +
            "changed:           bad@ripe.net 20071025\n" +
            "source:            TEST";

    @Before
    public void setUp() throws Exception {
        subject = new UpdateObjectHandlerImpl(rpslObjectUpdateDao, Lists.<BusinessRuleValidator>newArrayList(), dateTimeProvider);
    }

    @Test
    public void create_success() {
        final RpslObject mntner = RpslObject.parse("mntner: MNT");
        final PreparedUpdate update = update(null, mntner, Operation.UNSPECIFIED, Action.CREATE);

        when(updateContext.hasErrors(update)).thenReturn(false);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, times(1)).createObject(mntner);
    }

    @Test
    public void create_fail() {
        final RpslObject mntner = RpslObject.parse("mntner: MNT");
        final PreparedUpdate update = update(null, mntner, Operation.UNSPECIFIED, Action.CREATE);
        when(updateContext.hasErrors(update)).thenReturn(true);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, never()).createObject(mntner);
    }

    public void update_success() {
        final RpslObject mntner = RpslObject.parse("mntner: MNTY");
        final RpslObject mntner2 = RpslObject.parse("mntner: MNT2");
        final PreparedUpdate update = update(mntner, mntner2, Operation.UNSPECIFIED, Action.MODIFY);
        when(updateContext.hasErrors(update)).thenReturn(false);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, times(1)).updateObject(anyInt(), eq(mntner2));
    }

    @Test
    public void update_fail() {
        final RpslObject mntner = RpslObject.parse("mntner: MNTY");
        final RpslObject mntner2 = RpslObject.parse("mntner: MNT2");
        final PreparedUpdate update = update(mntner, mntner2, Operation.UNSPECIFIED, Action.MODIFY);
        when(updateContext.hasErrors(update)).thenReturn(true);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, never()).updateObject(anyInt(), eq(mntner2));
    }

    @Test
    public void updateLastChangedAttribute() {
        when(dateTimeProvider.getCurrentDate()).thenReturn(LocalDate.now());
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
        final RpslObject databaseObject = RpslObject.parse(RIPE_NCC_BA_MNT_MAINTAINER + "\n" +
                "changed: user@host.org 20120601\n" +
                "changed: user@host.org\n" +
                "remarks: changed");

        final RpslObject updatedObject = subject.updateLastChangedAttribute(databaseObject);

        final String currentDate = formatter.print(LocalDate.now());
        final List<RpslAttribute> attributes = updatedObject.findAttributes(AttributeType.CHANGED);

        assertThat(attributes.get(attributes.size() - 1).getCleanValue().toString(), endsWith(currentDate));
    }

    @Test
    public void delete_success() {
        RpslObject mntner = RpslObject.parse(1, "mntner: MNT\nmnt-by: MNT-BY");
        Update update = update(mntner, mntner, Operation.DELETE);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, mntner, mntner, Action.DELETE);
        when(updateContext.hasErrors(preparedUpdate)).thenReturn(false);

        subject.execute(preparedUpdate, updateContext);

        verify(rpslObjectUpdateDao, times(1)).deleteObject(mntner.getObjectId(), mntner.getKey().toString());
    }

    @Test
    public void delete_fail() {
        RpslObject mntner = RpslObject.parse(1, "mntner: MNT\nmnt-by: MNT-BY");
        Update update = update(mntner, mntner, Operation.DELETE);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, mntner, Action.DELETE);

        when(updateContext.hasErrors(preparedUpdate)).thenReturn(true);

        subject.execute(preparedUpdate, updateContext);

        verify(rpslObjectUpdateDao, never()).deleteObject(mntner.getObjectId(), mntner.getKey().toString());
    }

    private Update update(final RpslObject originalObject, final RpslObject updatedObject, final Operation operation) {
        Paragraph paragraph = new Paragraph((originalObject != null) ? originalObject.toString() : "", null);
        return new Update(paragraph, operation, null, updatedObject);
    }


    private PreparedUpdate update(final RpslObject originalObject, final RpslObject updatedObject, final Operation operation, final Action action) {
        final Update update = new Update(new Paragraph(updatedObject.toString()), operation, null, updatedObject);
        return new PreparedUpdate(update, originalObject, updatedObject, action);
    }
}
