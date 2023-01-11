package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.ObjectChangeData;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class DeltaTransformer {

    private final Dummifier dummifierNrtm;

    public DeltaTransformer(final Dummifier dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    Stream<DeltaChange> toDeltaChangeStream(final List<ObjectChangeData> objects) {
        return objects.stream().map(objectData -> {
            if (objectData.operation() == Operation.UPDATE) {
                final RpslObject dummy = dummifierNrtm.dummify(NRTM_VERSION, objectData.rpslObject());
                return DeltaChange.addModify(dummy);
            }
            return DeltaChange.delete(objectData.rpslObject().getType(), objectData.rpslObject().getFormattedKey());
        });
    }

    List<DeltaChange> toDeltaChange(final List<SerialEntry> changes) {
        return changes.stream()
            .filter(change -> dummifierNrtm.isAllowed(NRTM_VERSION, change.getRpslObject()))
            .map(serialRpsl -> {
                if (serialRpsl.getOperation() == Operation.UPDATE) {
                    return DeltaChange.addModify(
                        dummifierNrtm.dummify(NRTM_VERSION, serialRpsl.getRpslObject())
                    );
                } else {
                    return DeltaChange.delete(
                        serialRpsl.getRpslObject().getType(),
                        serialRpsl.getRpslObject().getKey().toString()
                    );
                }
            }).collect(Collectors.toList());
    }

}
