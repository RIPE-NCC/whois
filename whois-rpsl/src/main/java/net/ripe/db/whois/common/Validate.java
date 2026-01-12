package net.ripe.db.whois.common;

import java.util.Collection;

// TOOD: [ES] copied methods from commons-lang org.apache.commons.lang.Validate class, so we can remove the outdated dependency.
//            We cannot directly replace these methods with commons-lang3 because the behaviour changed to throw NPE instead of IAE.
//            In future we need to either refactor to use commons-lang3 or switch to an alternative such as org.springframework.util.
public class Validate {

    public static void notNull(final Object object) {
        notNull(object, "The validated object is null");
    }

    public static void notNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(final boolean expression, final String message) {
        if (expression == false) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(final boolean expression, final String message, final Object value) {
        if (expression == false) {
            throw new IllegalArgumentException(message + value);
        }
    }

    public static void isTrue(final boolean expression) {
        if (expression == false) {
            throw new IllegalArgumentException("The validated expression is false");
        }
    }

    public static void notEmpty(final String string) {
        notEmpty(string, "The validated string is empty");
    }

    public static void notEmpty(final Collection collection) {
        notEmpty(collection, "The validated collection is empty");
    }

    public static void notEmpty(final Object[] array, final String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(final Collection<?> collection, final String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(final String string, final String message) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
