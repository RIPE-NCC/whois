package net.ripe.db.whois.common.etree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChildNodeTreeMapEmptyTest {

    @SuppressWarnings("unchecked")
    @Test
    public void removeChildIsUnsupported() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ChildNodeTreeMap.EMPTY.removeChild(null);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addChildIsUnsupported() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ChildNodeTreeMap.EMPTY.addChild(null);
        });
    }
}
