package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
class NoticeFactory {

    private final String tncTitle;
    private final String tncDescription;
    private final String tncLinkrel;
    private final String tncLinkhref;
    private final String tncLinktype;
    private final String filterIsFiltered;
    private final String filterDescription;
    private final String filterTitle;
    private final String sourceDescription;
    private final String sourceTitle;

    @Autowired
    public NoticeFactory(@Value("${rdap.tnc.title:}") final String tncTitle,
                         @Value("${rdap.tnc.description:}") final String tncDescription,
                         @Value("${rdap.tnc.linkrel:}") final String tncLinkrel,
                         @Value("${rdap.tnc.linkhref:}") final String linkHref,
                         @Value("${rdap.tnc.linktype:}") final String linkType,
                         @Value("${rdap.filter.isfiltered:}") final String filtered,
                         @Value("${rdap.filter.description:}") final String filterDescription,
                         @Value("${rdap.filter.title:}") final String filterTitle,
                         @Value("${rdap.source.description:}") final String sourceDescription,
                         @Value("${rdap.source.title:}") final String sourceTitle) {
        this.tncTitle = tncTitle;
        this.tncDescription = tncDescription;
        this.tncLinkrel = tncLinkrel;
        this.tncLinkhref = linkHref;
        this.tncLinktype = linkType;
        this.filterIsFiltered = filtered;
        this.filterDescription = filterDescription;
        this.filterTitle = filterTitle;
        this.sourceDescription = sourceDescription;
        this.sourceTitle = sourceTitle;
    }

    public List<Notice> generateNotices(final String selfLink, final RpslObject rpslObject) {
        final List<Notice> notices = Lists.newArrayList();

        if (this.filterIsFiltered.equals("true")) {
            final Notice filtered = new Notice();
            filtered.setTitle(this.filterTitle);
            filtered.getDescription().add(this.filterDescription);
            notices.add(filtered);
        }

        final Notice source = new Notice();
        source.setTitle(this.sourceTitle);
        source.getDescription().add(this.sourceDescription);
        source.getDescription().add(rpslObject.getValueForAttribute(AttributeType.SOURCE).toString());
        notices.add(source);

        return notices;
    }

    public Notice generateTnC(final String selfLink) {
        final Notice tnc = new Notice();
        tnc.setTitle(this.tncTitle);
        tnc.getDescription().add(this.tncDescription);
        final Link link = new Link();
        link.setRel(this.tncLinkrel);
        link.setHref(this.tncLinkhref);
        link.setType(this.tncLinktype);
        link.setValue(selfLink);
        tnc.getLinks().add(link);
        return tnc;
    }
}
