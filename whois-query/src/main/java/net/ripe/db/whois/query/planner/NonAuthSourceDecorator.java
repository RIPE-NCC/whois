package net.ripe.db.whois.query.planner;

import jersey.repackaged.com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class NonAuthSourceDecorator implements ResponseDecorator {

    private String source;
    private String nonAuthSource;

    @Autowired
    public NonAuthSourceDecorator(@Value("${whois.nonauth.source}") final String nonAuthSource,
                                  @Value("${whois.source}") final String source) {
        this.source = source;
        this.nonAuthSource = nonAuthSource;
    }

    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {

        if (!query.getSources().isEmpty() && !query.getSources().containsAll(Lists.newArrayList(this.source, nonAuthSource))) {
            if (query.getSources().contains(nonAuthSource)) {
                return filteroutResources(input, CIString.ciString(source));
            } else if (query.getSources().contains(source)) {
                return filteroutResources(input, CIString.ciString(nonAuthSource));
            }
        }
        return input;
    }

    private Iterable<? extends ResponseObject> filteroutResources(Iterable<? extends ResponseObject> input, CIString source) {
        List<? extends ResponseObject> filteredResponse = Lists.newArrayList(input);
        filteredResponse.removeIf(obj -> obj instanceof RpslObject && ((RpslObject) obj).getValueForAttribute(AttributeType.SOURCE).equals(source));
        return filteredResponse;
    }
}
