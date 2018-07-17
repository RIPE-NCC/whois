package net.ripe.db.whois.query.planner;

import jersey.repackaged.com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class NonAuthSourceDecorator implements ResponseDecorator {

    private String nonAuthSource;
    private SourceContext sourceContext;

    @Autowired
    public NonAuthSourceDecorator(@Value("${whois.nonauth.source}") final String nonAuthSource,
                                  SourceContext sourceContext) {
        this.nonAuthSource = nonAuthSource;
        this.sourceContext = sourceContext;
    }

    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {

        if (!query.getSources().isEmpty() && !query.getSources().contains(nonAuthSource)
                && !query.getSources().stream().anyMatch(source -> sourceContext.getAllSourceNames().contains(CIString.ciString(source)))) {

            List<? extends ResponseObject> filteredResponse = Lists.newArrayList(input);
            filteredResponse.removeIf(obj -> obj instanceof RpslObject && ((RpslObject) obj).getValueForAttribute(AttributeType.SOURCE).equals(CIString.ciString(nonAuthSource)));
            return filteredResponse;
        }

        return input;
    }
}
