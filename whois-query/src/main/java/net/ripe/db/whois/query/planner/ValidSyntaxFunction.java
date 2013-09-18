package net.ripe.db.whois.query.planner;


import com.google.common.base.Function;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

class ValidSyntaxFunction implements Function<ResponseObject, Iterable<? extends ResponseObject>> {
    @Override
    public Iterable<? extends ResponseObject> apply(@Nullable final ResponseObject input) {
        if (input instanceof RpslObject) {
            final RpslObject object = (RpslObject) input;

            if (!validSyntax(object)) {
                return Arrays.asList(new MessageObject(QueryMessages.invalidSyntax(object.getKey())));
            }
        }
        return Collections.singletonList(input);
    }

    private boolean validSyntax(final RpslObject object) {
        final ObjectMessages objectMessages = ObjectTemplate.getTemplate(object.getType()).validate(object);
        return objectMessages.getErrorCount() == 0;
    }
}
