package net.ripe.db.whois.update.dao;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class LegacyAutnumDaoTest extends AbstractUpdateDaoTest  {
    @Autowired LegacyAutnumDao subject;

    @Before
    public void setup() {
        databaseHelper.getInternalsTemplate().execute("DELETE FROM legacy_autnums");
    }

    @Test
    public void storeAndLoad() throws SQLException {
        subject.store(Lists.newArrayList("325", "675", "1058"));

        assertThat(subject.load(), contains(ciString("AS325"), ciString("AS675"), ciString("AS1058")));
    }
}
