package net.ripe.db.whois.api.rest.checks;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceInputChecker {
    public void checkNserverCorrectPrefixes(List<Update> updates){
        List<RpslObject> rpslObjects = updates.stream().map(Update::getSubmittedObject).collect(Collectors.toList());

        for (RpslObject rpslObject: rpslObjects) {
            if (hasRipeNserver(rpslObject) && hasIncorrectPrefixes(rpslObject, isIpv4(rpslObject))){
                throw new IllegalArgumentException("Is not allowed to use that prefix with ns.ripe.net name server");
            }
        }
    }

    private boolean isIpv4(RpslObject rpslObject){
        return rpslObject.getAttributes().stream().anyMatch(attribute -> {
            assert attribute.getType() != null;
            return attribute.getType().equals(AttributeType.DOMAIN)
                    && !attribute.getValue().contains("ip6");
        });
    }
    private boolean hasIncorrectPrefixes(RpslObject rpslObject, boolean isIpv4) {
        return rpslObject.getAttributes().stream().anyMatch(attribute -> {
            assert attribute.getType() != null;
            return attribute.getType().equals(AttributeType.DESCR)
                    && ((!attribute.getValue().contains("/32") && !isIpv4) || (!attribute.getValue().contains("/16") && isIpv4));
        });
    }

    private boolean hasRipeNserver(RpslObject rpslObject) {
        return rpslObject.getAttributes().stream().anyMatch(attribute -> {
            assert attribute.getType() != null;
            return attribute.getType().equals(AttributeType.NSERVER)
                    && attribute.getValue().equals("ns.ripe.net");
        });
    }
}

