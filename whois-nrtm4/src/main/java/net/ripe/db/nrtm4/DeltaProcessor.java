package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.RpslObjectModel;
import net.ripe.db.whois.common.dao.Serial;
import net.ripe.db.whois.common.dao.jdbc.SerialRpslObjectTuple;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class DeltaProcessor {

    private final DummifierNrtm dummifierNrtm;

    public DeltaProcessor(final DummifierNrtm dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    List<DeltaChange> process(final List<SerialRpslObjectTuple> changes) {
        return changes.stream()
            .filter(change -> dummifierNrtm.isAllowed(NRTM_VERSION, change.getRpslObjectModel().getObject()))
            .map(serialRpsl -> {
                final Serial serial = serialRpsl.getSerial();
                final RpslObjectModel rpslObjectModel = serialRpsl.getRpslObjectModel();
                if (serial.isInLast()) {
                    return new DeltaChange(
                        DeltaChange.Action.ADD_MODIFY,
                        rpslObjectModel.getObjectType(),
                        rpslObjectModel.getKey(),
                        dummifierNrtm.dummify(NRTM_VERSION, rpslObjectModel.getObject())
                    );
                } else {
                    return new DeltaChange(
                        DeltaChange.Action.DELETE,
                        rpslObjectModel.getObjectType(),
                        rpslObjectModel.getKey(),
                        null
                    );
                }
            }).collect(Collectors.toList());
    }

}
