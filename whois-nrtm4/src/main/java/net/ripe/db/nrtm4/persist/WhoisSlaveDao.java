package net.ripe.db.nrtm4.persist;

import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class WhoisSlaveDao {

    private final DeltaFileRepository deltaFileRepository;
    private final WhoisSlaveRepository whoisSlaveRepository;

    public WhoisSlaveDao(
            final DeltaFileRepository deltaFileRepository,
            final WhoisSlaveRepository whoisSlaveRepository
    ) {
        this.deltaFileRepository = deltaFileRepository;
        this.whoisSlaveRepository = whoisSlaveRepository;
    }

    public List<Pair<SerialModel, RpslObjectModel>> findSerialsAndObjectsSinceSerial(final int serialId) {
        final DeltaFile deltaFile = deltaFileRepository.findLastChange();

        final int lastSerialId = deltaFile.getLastSerialId();
        final List<Pair<SerialModel, RpslObjectModel>> lastObjects = whoisSlaveRepository.findSerialsInLastSince(lastSerialId);
        final List<Pair<SerialModel, RpslObjectModel>> historyObjects = whoisSlaveRepository.findSerialsInHistorySince(lastSerialId);

        final List<Pair<SerialModel, RpslObjectModel>> allObjects = new ArrayList<>();

        allObjects.addAll(lastObjects);
        allObjects.addAll(historyObjects);

        allObjects.sort(Comparator.comparingInt(pair -> pair.getValue0().getSerialId()));
        return allObjects;
    }

}
