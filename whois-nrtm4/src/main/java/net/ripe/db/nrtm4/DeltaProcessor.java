package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.RpslObjectModel;
import net.ripe.db.nrtm4.persist.SerialModel;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DeltaProcessor {

    private final RpslObjectTransformer rpslObjectTransformer;

    public DeltaProcessor(final RpslObjectTransformer rpslObjectTransformer) {
        this.rpslObjectTransformer = rpslObjectTransformer;
    }

    List<DeltaChange> process(final List<Pair<SerialModel, RpslObjectModel>> changes) {
        return changes.stream()
            .filter(change -> rpslObjectTransformer.isAllowed(change.getValue1().getObject()))
            .map(serialRpsl -> {
                final SerialModel serial = serialRpsl.getValue0();
                final RpslObjectModel rpslObjectModel = serialRpsl.getValue1();
                if (serial.isAtlast()) {
                    return new DeltaChange(
                        DeltaChange.Action.add_modify,
                        rpslObjectModel.getObjectType(),
                        rpslObjectModel.getPkey(),
                        rpslObjectTransformer.filter(rpslObjectModel.getObject())
                    );
                } else {
                    return new DeltaChange(
                        DeltaChange.Action.delete,
                        rpslObjectModel.getObjectType(),
                        rpslObjectModel.getPkey(),
                        null
                    );
                }
            }).collect(Collectors.toList());
    }

}
