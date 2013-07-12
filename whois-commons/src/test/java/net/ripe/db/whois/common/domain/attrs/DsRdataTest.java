package net.ripe.db.whois.common.domain.attrs;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DsRdataTest {

    @Test
    public void basicTest() {
        assertThat(DsRdata.parse("7096 5 2 4A369FE834DE194579B94C92CBAFE7C4B5EF7F73CD7399854C8FF598 45D019BA").toString(),
                is("7096 5 2 4A369FE834DE194579B94C92CBAFE7C4B5EF7F73CD7399854C8FF59845D019BA"));
        assertThat(DsRdata.parse("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF").toString(),
                is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));
        assertThat(DsRdata.parse("59725 8 2 dd175adbdb5af96c926a100fce4a3a3524ca143b20f52bf5c3a3f6e5eb756c51").toString(),
                is("59725 8 2 dd175adbdb5af96c926a100fce4a3a3524ca143b20f52bf5c3a3f6e5eb756c51"));
        assertThat(DsRdata.parse("9520 8 1 ( EA17B8C10043303DDE17B55AAB18FBDFF2066176 )").toString(),
                is("9520 8 1 EA17B8C10043303DDE17B55AAB18FBDFF2066176"));
        assertThat(DsRdata.parse("9520 8 2 ( 59EEB479C70A53DC1B14786F0360AD9DB6CF477C73B0 E4FCB12788DE2F2E528F )").toString(),
                is("9520 8 2 59EEB479C70A53DC1B14786F0360AD9DB6CF477C73B0E4FCB12788DE2F2E528F"));
    }
}
