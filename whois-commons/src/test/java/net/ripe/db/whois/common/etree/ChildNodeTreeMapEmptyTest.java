package net.ripe.db.whois.common.etree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChildNodeTreeMapEmptyTest {

    @SuppressWarnings("unchecked")
    @Test
    public void removeChildIsUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ChildNodeTreeMap.EMPTY.removeChild(null);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addChildIsUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ChildNodeTreeMap.EMPTY.addChild(null);
        });
    }
}
