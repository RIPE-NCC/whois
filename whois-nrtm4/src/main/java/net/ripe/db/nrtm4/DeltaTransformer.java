package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class DeltaTransformer {

    private final Dummifier dummifierNrtm;

    public DeltaTransformer(final Dummifier dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    List<DeltaChange> process(final List<SerialEntry> changes) {
        return changes.stream()
            .filter(change -> dummifierNrtm.isAllowed(NRTM_VERSION, change.getRpslObject()))
            .map(serialRpsl -> {
                if (serialRpsl.getOperation() == Operation.UPDATE) {
                    return DeltaChange.addModify(
                        serialRpsl.getSerialId(),
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
