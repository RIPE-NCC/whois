package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;

public interface ExportFilter {

    boolean shouldExport(final RpslObject rpslObject);

    class SourceExportFilter implements ExportFilter {

        private final CIString source;
        private final Set<ObjectType> objectTypes;
        private final boolean lenientMissingSource;

        public SourceExportFilter(final String source,
                                  final Set<ObjectType> objectTypes,
                                  final boolean lenientMissingSource) {
            this.source = ciString(source);
            this.objectTypes = objectTypes;
            this.lenientMissingSource = lenientMissingSource;
        }

        public SourceExportFilter(final String source,
                                  final Set<ObjectType> objectTypes) {
            this.source = ciString(source);
            this.objectTypes = objectTypes;
            this.lenientMissingSource = true;
        }

        @Override
        public boolean shouldExport(RpslObject rpslObject) {
            return (lenientMissingSource && !rpslObject.containsAttribute(SOURCE)) ||
                    objectTypes.contains(rpslObject.getType()) && source.equals(rpslObject.getValueForAttribute(SOURCE));
        }
    }
}
