package net.ripe.db.whois.query.endtoend.compare.query;

import com.google.common.base.Predicate;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;

import javax.annotation.Nullable;

public class KnownDifferencesPredicate implements Predicate<ResponseObject> {

    @Override
    public boolean apply(@Nullable final ResponseObject input) {
        return !(input instanceof MessageObject) || !input.toString().startsWith("% This query was served by");
    }
}
