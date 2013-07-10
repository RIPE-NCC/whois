package net.ripe.db.whois.common.domain.attrs;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DsRdataTest {

    @Test
    public void basicTest() {
        for (String testString: ImmutableList.of(
                "7096 5 2 4A369FE834DE194579B94C92CBAFE7C4B5EF7F73CD7399854C8FF598 45D019BA",
                "52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF",
                "59725 8 2 dd175adbdb5af96c926a100fce4a3a3524ca143b20f52bf5c3a3f6e5eb756c51"
                )) {
            DsRdata subject = DsRdata.parse(testString);
            assertThat(subject.toString(), is(testString));
        }
    }
}
