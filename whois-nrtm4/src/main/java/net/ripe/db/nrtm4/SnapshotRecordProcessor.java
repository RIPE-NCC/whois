package net.ripe.db.nrtm4;

import com.google.common.collect.Maps;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SnapshotRecordProcessor implements Supplier<Map<CIString, byte[]>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotRecordProcessor.class);
    private final BlockingQueue<SnapshotFileRecord> sharedQueue;
    private  final List<NrtmVersionInfo>  sourceToVersionInfo;

    public SnapshotRecordProcessor(final BlockingQueue<SnapshotFileRecord> sharedQueue, final List<NrtmVersionInfo> sourceToVersionInfo) {
        this.sharedQueue = sharedQueue;
        this.sourceToVersionInfo = sourceToVersionInfo;
    }

    @Override
    public Map<CIString, byte[]> get() {

        final Map<CIString, GzipOutStreamWriter> resources = initializeResources(sourceToVersionInfo);
        try {
            while (true) {
                final SnapshotFileRecord record = sharedQueue.take();
                if (record.getObject() == null) {
                    resources.values().forEach(GzipOutStreamWriter::close);
                    break;
                }

                final CIString source = record.getObject().getValueForAttribute(AttributeType.SOURCE);
                if (resources.containsKey(source)) {
                    resources.get(source).write(record);
                }
            }

            return resources.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().getOutputstream().toByteArray()));
        } catch (final Exception  e) {
            LOGGER.error("Exception writing snapshot {}", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            resources.values().forEach(GzipOutStreamWriter::close);
        }
    }

    private Map<CIString, GzipOutStreamWriter> initializeResources(final List<NrtmVersionInfo> sourceToVersionInfo)  {

        final Map<CIString, GzipOutStreamWriter> resources = Maps.newHashMap();

        sourceToVersionInfo.forEach(nrtmVersionInfo -> {
            final GzipOutStreamWriter resource = new GzipOutStreamWriter();
            resource.write(new NrtmVersionRecord(nrtmVersionInfo, NrtmDocumentType.SNAPSHOT));
            resources.put(nrtmVersionInfo.source().getName(), resource);
        });

        return resources;
    }
}
