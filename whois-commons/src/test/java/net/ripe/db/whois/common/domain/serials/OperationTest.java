package net.ripe.db.whois.common.domain.serials;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OperationTest {

    @Test
    public void getByCode() {
        for (Operation operation : Operation.values()) {
            assertThat(Operation.getByCode(operation.getCode()), is(operation));
        }
    }

    @Test
    public void getByName() {
        assertThat(Operation.getByName("ADD"), is(Operation.UPDATE));
        assertThat(Operation.getByName("DEL"), is(Operation.DELETE));
    }
}
