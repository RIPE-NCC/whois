package net.ripe.db.nrtm4.dao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
@Service
public class WhoisDao {

    private final WhoisSerialRepository whoisSerialRepository;

    WhoisDao(
        final WhoisSerialRepository whoisSerialRepository
    ) {
        this.whoisSerialRepository = whoisSerialRepository;
    }

    public InitialSnapshotState getInitialSnapshotState() {
        final int lastSerialId = whoisSerialRepository.findLastSerialId();
        final List<ObjectData> objects = whoisSerialRepository.findLastObjects();
        return new InitialSnapshotState(lastSerialId, objects);
    }

    public String findRpsl(final int objectId, final int sequenceId) {
        return whoisSerialRepository.findRpslInLast(objectId, sequenceId);
    }
}
