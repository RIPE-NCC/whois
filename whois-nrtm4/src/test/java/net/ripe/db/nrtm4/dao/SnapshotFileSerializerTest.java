package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class SnapshotFileSerializerTest {

    @Mock
    DummifierNrtm dummifierNrtm;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

}
