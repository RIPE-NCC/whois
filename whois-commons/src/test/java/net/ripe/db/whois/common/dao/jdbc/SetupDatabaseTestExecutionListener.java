package net.ripe.db.whois.common.dao.jdbc;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class SetupDatabaseTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        DatabaseHelper.setupDatabase();
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        testContext.getApplicationContext().getBean(DatabaseHelper.class).setup();
    }
}
