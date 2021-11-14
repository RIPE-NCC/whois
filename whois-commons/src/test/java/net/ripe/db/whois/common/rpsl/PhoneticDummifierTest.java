package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PhoneticDummifierTest {

    @Test
    public void dummify_org_name() {
        assertThat(dummify("LTU Systeme GmbH & Co. KG"), is("Lima Sierra Golf Charlie"));
        assertThat(dummify("Cedgetec UG (haftungsbeschraenkt)"), is("Charlie Uniform"));
        assertThat(dummify("Eretebat Farda E-Commerce Co. (PJS)"), is("Echo Foxtrot Echo Charlie"));
        assertThat(dummify("Saudi financial leases contract registry Company (Saudi Joint Stock)"), is("Sierra Foxtrot Lima Charlie"));
        assertThat(dummify("SIA \"Singularity Telecom\""), is("Sierra Tango"));
        assertThat(dummify("Hilkaltex Sh. Y. (1988) Ltd."), is("Hotel Sierra Yankee Lima"));
        assertThat(dummify("\"Fregat TV\" Ltd."), is("Tango Lima"));
        assertThat(dummify("SHAHRNET"), is("Sierra Alpha"));
        assertThat(dummify("3D Fashion Operations B.V."), is("Foxtrot Oscar Bravo"));
    }

    private String dummify(final String input) {
        return new PhoneticDummifier(input).toString();
    }
}
