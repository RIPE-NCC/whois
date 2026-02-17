package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordsInIrtValidator extends AbstractPasswordsValidator {

    PasswordsInIrtValidator(@Value("${irt.password.supported:true}") final boolean isIrtPasswordSupported){
        super(isIrtPasswordSupported, ImmutableList.of(ObjectType.IRT));
    }
}
