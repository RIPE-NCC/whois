package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.rpsl.AttributeType;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.AttributeType.PREFIXLEN;

@Component
public class PrefixlenValidator extends AbstractRemarksValidator {

    protected AttributeType getAttributeType(){
        return PREFIXLEN;
    }
}
