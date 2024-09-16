package net.ripe.db.whois.common.rpsl.attrs;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class DsRdataTest {

    @Test
    public void validData() {
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

        assertThat(DsRdata.parse("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF").toString(),
                is("52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF"));

        assertThat(DsRdata.parse("17729 8 4 6FA0EF598C52  8C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6").toString(),
                is("17729 8 4 6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6"));

        assertThat(DsRdata.parse("33841 5 3 B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259").toString(),
                is("33841 5 3 B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259"));

        // unknown type
        assertThat(DsRdata.parse("33841 10 5 B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259").toString(),
                is("33841 10 5 B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259B2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259"));
    }

    @Test
    public void invalidData() {
        //total junk input
        verifyFailure("12534534 234 243 23409sdlfkjh skdjhf34uhfsd lshjdf92483hf jshdfkjshdf 02 ljksdhflkjhg0 jkgh93h749gh",
                "Invalid syntax (12534534 234 243 23409sdlfkjh skdjhf34uhfsd lshjdf92483hf jshdfkjshdf 02 ljksdhflkjhg0 jkgh93h749gh)");

        verifyFailure("asasa asd jhdfg kjdlhfglkjshlgkfdghj",
                "Invalid syntax (asasa asd jhdfg kjdlhfglkjshlgkfdghj)");

        // wrong digest length for type
        verifyFailure("52314 5 1 93B5837D4E5C063A37",
                "Digest format is invalid for digest type 1:  (93B5837D4E5C063A37)");

        verifyFailure("52314 5 1 93B5837D4E5C063A3793B5837D4E5C063A3793B5837D4E5C063A37",
                "Digest format is invalid for digest type 1:  (93B5837D4E5C063A3793B5837D4E5C063A3793B5837D4E5C063A37)");

        verifyFailure("31122 5 2 1233e0314f7c268574c0ab082d77779c4dc3d2bcf6620fe934efb912ba4fcd7f360",
                "Digest format is invalid for digest type 2:  (1233e0314f7c268574c0ab082d77779c4dc3d2bcf6620fe934efb912ba4fcd7f360)");

        verifyFailure("33841 5 3 AB2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259",
                "Digest format is invalid for digest type 3:  (AB2B9053FFDE225CDB87012E914164E68B389EDC2821103546926859E76EAA259)");

        verifyFailure("17729 8 4 A6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6",
                "Digest format is invalid for digest type 4:  (A6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6)");

        // keytag too large
        verifyFailure("31122222 5 2 3e0314f7c268574c0ab082d77779c4dc3d2bcf6620fe934efb912ba4f",
                "Invalid keytag: 31122222 (31122222 5 2 3e0314f7c268574c0ab082d77779c4dc3d2bcf6620fe934efb912ba4f)");

        //wildly inaccurate algorithm type
        verifyFailure("17729 555 4 6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6",
                "Invalid algorithm: 555 (17729 555 4 6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6)");

        //wildly inaccurate digest type
        verifyFailure("17729 5 444 6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6",
                "Invalid digest type: 444 (17729 5 444 6FA0EF598C528C860630AE82676B9CCF66161284960E81622E68C2F69ADBAA6215970F92214CCED9357B20714A36BDF6)");

        verifyFailure("523145 1 93B5837D4E5C063A37",
                "Invalid syntax (523145 1 93B5837D4E5C063A37)");

    }

    private void verifyFailure(String input, String expectedError) {
        try {
            DsRdata.parse(input);
            fail("Expected exception not thrown\nInput: '" + input + "'\n Expected Error: '"+ expectedError + "'");
        } catch (AttributeParseException e) {
            assertThat(e.getMessage(), is(expectedError));
        }

    }
}
