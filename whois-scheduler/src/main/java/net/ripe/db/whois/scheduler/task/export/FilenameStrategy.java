package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;

import static net.ripe.db.whois.common.rpsl.ObjectType.AS_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

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
            return (objectType == AS_SET || objectType == AUT_NUM || objectType == ROUTE || objectType == ROUTE6) ?
                "ripe-nonauth.db." + objectType.getName() :
                null;
        }
    }

    String getFilename(ObjectType objectType);
}
