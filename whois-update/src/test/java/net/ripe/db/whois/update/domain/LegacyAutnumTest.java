package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class LegacyAutnumTest extends AbstractUpdateDaoTest {
    @Autowired LegacyAutnum subject;

    @Test
    public void importAndGet() throws IOException {
        subject.importLegacyAutnums(Paths.get("whois-update/target/test-classes/legacyAutnumImport").toString());
        assertThat(subject.getLegacyAutnums(), contains(CIString.ciString("AS436"), CIString.ciString("AS870"), CIString.ciString("AS6985")));
    }

}
