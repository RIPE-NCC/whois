package net.ripe.db.whois.common.dao.jdbc;


import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("IntegrationTest")
public class JdbcRpslObjectUpdateDaoCtdIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired RpslObjectUpdateDao subject;

    @BeforeEach
    public void setup() {
        sourceContext.setCurrentSourceToWhoisMaster();
    }

    @AfterEach
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void self_referencing_role_test() {
        RpslObject selfrefRole = RpslObject.parse("role: some role\nadmin-c:ROLE-NIC\nnic-hdl:ROLE-NIC");
        subject.createObject(selfrefRole);
    }
}
