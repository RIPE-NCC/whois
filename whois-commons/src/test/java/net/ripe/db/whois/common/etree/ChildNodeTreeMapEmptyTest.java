package net.ripe.db.whois.common.etree;

import org.junit.Test;

public class ChildNodeTreeMapEmptyTest {

    @SuppressWarnings("unchecked")
    @Test(expected=UnsupportedOperationException.class)
    public void removeChildIsUnsupported() {
        ChildNodeTreeMap.EMPTY.removeChild(null);
    }

    @SuppressWarnings("unchecked")
    @Test(expected=UnsupportedOperationException.class)
    public void addChildIsUnsupported() {
        ChildNodeTreeMap.EMPTY.addChild(null);
    }
}
