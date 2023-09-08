package net.ripe.db.nrtm4;

import com.google.common.collect.Maps;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.ripe.db.nrtm4.RpslObjectEnqueuer.POISON_PILL;

public class SnapshotRecordConsumer implements Supplier<Map<CIString, byte[]>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotRecordConsumer.class);

    private final BlockingQueue<RpslObjectData> sharedQueue;
    private  final List<NrtmVersionInfo>  sourceToVersionInfo;

    public SnapshotRecordConsumer(final BlockingQueue<RpslObjectData> sharedQueue, final List<NrtmVersionInfo> sourceToVersionInfo) {
        this.sharedQueue = sharedQueue;
        this.sourceToVersionInfo = sourceToVersionInfo;
    }

    @Override
    public Map<CIString, byte[]> get() {

        final Map<CIString, GzipOutStreamWriter> resources = initializeResources(sourceToVersionInfo);
        try {
            while (true) {
                final RpslObjectData rpslObjectData = sharedQueue.take();
                if (rpslObjectData.objectId() == POISON_PILL.objectId()) {
                    LOGGER.info("closing the resources");
                    resources.values().forEach(GzipOutStreamWriter::close);
                    break;
                }

                LOGGER.info("Processing rpslObject {}", rpslObjectData.rpslObject().getKey());

                final CIString source = rpslObjectData.rpslObject().getValueForAttribute(AttributeType.SOURCE);
                if (resources.containsKey(source)) {
                    resources.get(source).write(new SnapshotFileRecord(rpslObjectData.rpslObject()));
                }
            }

            return resources.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().getOutputstream().toByteArray()));
        } catch (final InterruptedException e) {
            LOGGER.warn("Iterator interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (final IOException e) {
            LOGGER.error("Exception writing snapshot {}", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Map<CIString, GzipOutStreamWriter> initializeResources(final List<NrtmVersionInfo> sourceToVersionInfo)  {

        final Map<CIString, GzipOutStreamWriter> resources = Maps.newHashMap();

        sourceToVersionInfo.forEach(nrtmVersionInfo -> {
            try {
                final GzipOutStreamWriter resource = new GzipOutStreamWriter();
                resource.write(new NrtmVersionRecord(nrtmVersionInfo, NrtmDocumentType.SNAPSHOT));

                resources.put(nrtmVersionInfo.source().getName(), resource);

            } catch (IOException e) {
                LOGGER.error("Exception while creating a outputstream for {}-  {}", nrtmVersionInfo.source().getName(), e);
            }
        });

        return resources;
    }
}
