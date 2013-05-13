package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;

interface FilenameStrategy {
    static class SplitFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe.db." + objectType.getName();
        }
    }

    static class SingleFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe.db";
        }
    }

    String getFilename(ObjectType objectType);
}
