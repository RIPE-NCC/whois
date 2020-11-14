package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.dao.jdbc.IndexDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMaintenanceJmxTest {
    @Mock RpslObjectUpdateDao updateDao;
    @Mock IndexDao indexDao;
    @InjectMocks DatabaseMaintenanceJmx subject;

    @Test
    public void undeleteObject_success() {
        when(updateDao.undeleteObject(1)).thenReturn(new RpslObjectUpdateInfo(1, 1, ObjectType.MNTNER, "DEV-MNT"));
        final String response = subject.undeleteObject(1, "comment");
        assertThat(response, is(
                "Recovered object: RpslObjectUpdateInfo{objectId=1, objectType=MNTNER, key=DEV-MNT, sequenceId=1}\n" +
                " *** Remember to update IP trees if needed"));
    }

    @Test
    public void undeleteObject_error() {
        when(updateDao.undeleteObject(1)).thenThrow(EmptyResultDataAccessException.class);

        final String response = subject.undeleteObject(1, "comment");
        assertThat(response, is("Unable to recover: null"));
    }
}
