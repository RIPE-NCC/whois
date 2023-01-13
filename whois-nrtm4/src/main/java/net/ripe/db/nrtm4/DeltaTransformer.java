package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.ObjectChangeData;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


@Service
public class DeltaTransformer {

    private final Dummifier dummifierNrtm;

    public DeltaTransformer(final Dummifier dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    List<DeltaChange> toDeltaChangeList(final List<ObjectChangeData> objects) {
        return objects.stream()
            .filter(change -> dummifierNrtm.isAllowed(NRTM_VERSION, change.rpslObject()))
            .map(objectData -> {
            if (objectData.operation() == Operation.UPDATE) {
                final RpslObject dummy = dummifierNrtm.dummify(NRTM_VERSION, objectData.rpslObject());
                return DeltaChange.addModify(dummy);
            }
            return DeltaChange.delete(objectData.rpslObject().getType(), objectData.rpslObject().getKey().toString());
        }).toList();
    }

}
