package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;

import java.util.Set;

public abstract class FilenameStrategy {

    final String source;

    protected FilenameStrategy(final String source) {
        this.source = source.toLowerCase();
    }

    protected String getSource() {
        return this.source;
    }

    static class SplitFile extends FilenameStrategy {

        public SplitFile(final String source) {
            super(source);
        }

        @Override
        public String getFilename(final ObjectType objectType) {
            return String.format("%s.db.%s", getSource(), objectType.getName());
        }
    }

    static class SingleFile extends FilenameStrategy {

        public SingleFile(final String source) {
            super(source);
        }

        @Override
        public String getFilename(final ObjectType objectType) {
            return String.format("%s.db", getSource());
        }
    }

    static class NonAuthSingleFile extends FilenameStrategy {

        public NonAuthSingleFile(final String source) {
            super(source);
        }

        @Override
        public String getFilename(final ObjectType objectType) {
            return String.format("%s.db", getSource());
        }
    }

    static class NonAuthSplitFile extends FilenameStrategy {

        final Set<ObjectType> nonAuthObjectTypes;

        public NonAuthSplitFile(final String source, final Set<ObjectType> nonAuthObjectTypes) {
            super(source);
            this.nonAuthObjectTypes = nonAuthObjectTypes;
        }

        @Override
        public String getFilename(final ObjectType objectType) {
            return (nonAuthObjectTypes.contains(objectType)) ?
                String.format("%s.db.%s", getSource(), objectType.getName()) :
                null;
        }
    }

    abstract String getFilename(ObjectType objectType);
}
