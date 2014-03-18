package net.ripe.db.whois.common.support;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class ReinitContextTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
    }
}
