package net.ripe.db.whois.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceModeJmxTest {
    @Mock MaintenanceMode maintenanceMode;
    @InjectMocks MaintenanceModeJmx subject;

    @Test
    public void maintenance_mode_set() {
        subject.setMaintenanceMode("FULL, FULL");
        verify(maintenanceMode, times(1)).set(anyString());
    }
}
