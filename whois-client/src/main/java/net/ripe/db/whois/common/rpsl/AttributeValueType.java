package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;

import java.util.Collections;

interface AttributeValueType {
    final AttributeValueType SINGLE_VALUE = new SingleValueType();
    final AttributeValueType LIST_VALUE = new ListValueType();

    Iterable<String> getValues(String value);

    final class SingleValueType implements AttributeValueType {
        @Override
        public Iterable<String> getValues(final String value) {
            return Collections.singletonList(value);
        }
    }

    final class ListValueType implements AttributeValueType {
        private static final Splitter LIST_VALUE_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

        @Override
        public Iterable<String> getValues(final String value) {
            return LIST_VALUE_SPLITTER.split(value);
        }
    }
}
