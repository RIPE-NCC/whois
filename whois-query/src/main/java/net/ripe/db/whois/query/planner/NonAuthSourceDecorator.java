package net.ripe.db.whois.query.planner;

import jersey.repackaged.com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
class NonAuthSourceDecorator implements ResponseDecorator {

    private final String source;
    private final String nonAuthSource;
    private final List<String> ripeAuthAndNonauthSources;

    @Autowired
    public NonAuthSourceDecorator(@Value("${whois.nonauth.source}") final String nonAuthSource,
                                  @Value("${whois.source}") final String source) {
        this.source = source;
        this.nonAuthSource = nonAuthSource;
        this.ripeAuthAndNonauthSources = Lists.newArrayList(this.source, nonAuthSource);
    }

    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {
            // if --resources flag then filter out any NONAUTH objects
        if (query.hasOption(QueryFlag.RESOURCE)) {
            return filteroutResources(input, CIString.ciString(nonAuthSource));
        } else if (!query.getSources().containsAll(ripeAuthAndNonauthSources)) {
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
        Collections.reverse(filteredResponse);
        Iterator iterator = filteredResponse.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            if (iterator instanceof RpslObject && ((RpslObject) iterator).getValueForAttribute(AttributeType.SOURCE).equals(source)) {
                // remove Rpsl object
                iterator.remove();
                // remove message about object
                if (iterator.next() instanceof MessageObject) {
                    iterator.remove();
                }
            }
        }
        Collections.reverse(filteredResponse);
        return filteredResponse;
    }
}
