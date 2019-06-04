package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
class NoticeFactory {

    public static final String NOICE_MULTIPLE_VALUE_DESC = "There are multiple %s %s in %s, but only the first %s %s was returned.";
    public static final String NOTICE_MULTIPLE_VALUE_TITLE = "Multiple %s found";

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

        // TODO: [ES] add selfLink to notices

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
        tnc.getLinks().add(new Link(selfLink, tncLinkrel, tncLinkhref, null, this.tncLinktype));
        return tnc;
    }


    public static void addNoticeForMultipleValues(RdapObject rdapObject, AttributeType type, List<RpslAttribute> values, String key) {
        if(values.isEmpty() || values.size() == 1) {
            return;
        }

        String commaSeperatedValues = values.stream().map( x -> x.getCleanValue()).collect(Collectors.joining(","));

        final Notice notice = new Notice();
        notice.setTitle( String.format(NOTICE_MULTIPLE_VALUE_TITLE, type.getName()));
        notice.getDescription().add(String.format(NOICE_MULTIPLE_VALUE_DESC, type.getName(), commaSeperatedValues, key, type.getName(), values.get(0).getCleanValue()));

        rdapObject.getNotices().add(notice);
    }
}
