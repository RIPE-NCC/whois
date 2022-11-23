package net.ripe.db.nrtm4.persist;

import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class WhoisSlaveDao {

    private final WhoisSlaveRepository whoisSlaveRepository;

    public WhoisSlaveDao(
        final WhoisSlaveRepository whoisSlaveRepository
    ) {
        this.whoisSlaveRepository = whoisSlaveRepository;
    }

    public List<Pair<SerialModel, RpslObjectModel>> findSerialsAndObjectsSinceSerial(final int serialId) {
        final List<Pair<SerialModel, RpslObjectModel>> lastObjects = whoisSlaveRepository.findSerialsInLastSince(serialId);
        final List<Pair<SerialModel, RpslObjectModel>> historyObjects = whoisSlaveRepository.findSerialsInHistorySince(serialId);
        final List<Pair<SerialModel, RpslObjectModel>> allObjects = new ArrayList<>(lastObjects.size() + historyObjects.size());
        allObjects.addAll(lastObjects);
        allObjects.addAll(historyObjects);
        allObjects.sort(Comparator.comparingInt(pair -> pair.getValue0().getSerialId()));
        return allObjects;
    }

}
