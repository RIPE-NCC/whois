package net.ripe.db.whois.update.dao;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

@Tag("IntegrationTest")
public class LegacyAutnumDaoIntegrationTest extends AbstractUpdateDaoIntegrationTest {
    @Autowired LegacyAutnumDao subject;

    @BeforeEach
    public void setup() {
        databaseHelper.getInternalsTemplate().execute("DELETE FROM legacy_autnums");
    }

    @Test
    public void storeAndLoad() throws SQLException {
        subject.store(Lists.newArrayList("325", "675", "1058"));

        assertThat(subject.load(), contains(ciString("AS325"), ciString("AS675"), ciString("AS1058")));
    }
}
