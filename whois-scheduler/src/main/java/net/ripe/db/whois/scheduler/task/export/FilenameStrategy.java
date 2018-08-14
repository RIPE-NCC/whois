package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;

public interface FilenameStrategy {

    class SplitFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe.db." + objectType.getName();
        }
    }

    class SingleFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe.db";
        }
    }

    class NonAuthSingleFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe-nonauth.db";
        }
    }

    class NonAuthSplitFile implements FilenameStrategy {
        @Override
        public String getFilename(final ObjectType objectType) {
            return "ripe-nonauth.db." + objectType.getName();
        }
    }

    String getFilename(ObjectType objectType);
}
