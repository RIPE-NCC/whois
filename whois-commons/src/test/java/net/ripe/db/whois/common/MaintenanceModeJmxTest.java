package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MaintenanceModeJmxTest {
    @Mock MaintenanceMode maintenanceMode;
    @InjectMocks MaintenanceModeJmx subject;

    @Test
    public void maintenance_mode_set() {
        subject.setMaintenanceMode("FULL, FULL");
        verify(maintenanceMode, times(1)).set(anyString());
    }
}
