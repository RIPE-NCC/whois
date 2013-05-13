package net.ripe.db.whois.common.domain.serials;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OperationTest {
    @Test
    public void getByCode() {
        for (Operation operation : Operation.values()) {
            assertThat(Operation.getByCode(operation.getCode()), is(operation));
        }
    }
}
