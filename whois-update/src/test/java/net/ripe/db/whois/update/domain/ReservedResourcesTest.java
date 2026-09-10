package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReservedResourcesTest {

    @Test
    public void reserved_as_numbers() {
        final ReservedResources subject = new ReservedResources("1,2,3");

        assertThat(subject.isReservedAsNumber(1L), is(true));
        assertThat(subject.isReservedAsNumber(2L), is(true));
        assertThat(subject.isReservedAsNumber(3L), is(true));
        assertThat(subject.isReservedAsNumber(4L), is(false));
    }

    @Test
    public void reserved_as_numbers_range() {
        final ReservedResources subject = new ReservedResources("64496-131071");

        assertThat(subject.isReservedAsNumber(64495L), is(false));
        assertThat(subject.isReservedAsNumber(64496L), is(true));
        assertThat(subject.isReservedAsNumber(64497L), is(true));
        assertThat(subject.isReservedAsNumber(131071L), is(true));
        assertThat(subject.isReservedAsNumber(131072L), is(false));
    }

    @Test
    public void reserved_as_block() {
        final ReservedResources subject = new ReservedResources("64496-131071");

        assertThat(subject.isReservedAsBlock("as64496-AS131071"), is(true));
        assertThat(subject.isReservedAsBlock("As64495-as131071"), is(true));   // overlapping
        assertThat(subject.isReservedAsBlock("As131070-as131072"), is(true));   // overlapping
        assertThat(subject.isReservedAsBlock("AS1-AS2"), is(false));
        assertThat(subject.isReservedAsBlock("as131072-as131073"), is(false));
    }

    @Test
    public void bogon() {
        final ReservedResources subject = new ReservedResources("1", "127.0.0.1", "::1", "10.0.0.0-10.255.255.255");

        assertThat(subject.isBogon("127.0.0.1"), is(true));
        assertThat(subject.isBogon("::1"), is(true));
        assertThat(subject.isBogon("10.0.0.1"), is(true));
        assertThat(subject.isBogon("127.0.0.2"), is(false));
        assertThat(subject.isBogon("::2"), is(false));
    }

}
