package net.ripe.db.whois.rdap;

import com.google.common.collect.Lists;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
class NoticeFactory {

    private final NoticePropertyValues noticePropertyValues;
    @Autowired
    public NoticeFactory(final NoticePropertyValues noticePropertyValues) {
        this.noticePropertyValues=noticePropertyValues;
    }

    public List<Notice> generateNotices(final String selfLink, final RpslObject rpslObject) {
        final List<Notice> notices = Lists.newArrayList();

        // TODO: [ES] add selfLink to notices

        if (this.noticePropertyValues.getIsFiltered().equals("true")) {
            final Notice filtered = new Notice();
            filtered.setTitle(this.noticePropertyValues.getFilterTitle());
            filtered.getDescription().add(this.noticePropertyValues.getFilterDescription());
            notices.add(filtered);
        }

        final Notice inaccuracyNotice = new Notice();
        inaccuracyNotice.setTitle(this.noticePropertyValues.getInaccuracyNoticeTitle());
        inaccuracyNotice.getDescription().add(this.noticePropertyValues.getInaccuracyNoticeDescription());
        inaccuracyNotice.getLinks().add(new Link(
                selfLink,
                this.noticePropertyValues.getInaccuracyNoticeLinkRel(),
                this.noticePropertyValues.getInaccuracyNoticeLinkHref(),
                null,
                null,
                MediaType.TEXT_HTML
        ));
        notices.add(inaccuracyNotice);

        final Notice source = new Notice();
        source.setTitle(this.noticePropertyValues.getSourceTitle());
        source.getDescription().add(this.noticePropertyValues.getSourceDescription());
        source.getDescription().add(rpslObject.getValueForAttribute(AttributeType.SOURCE).toString());
        notices.add(source);

        return notices;
    }

    public Notice generateTnC(final String selfLink) {
        final Notice tnc = new Notice();
        tnc.setTitle(this.noticePropertyValues.getTncTitle());
        tnc.getDescription().add(this.noticePropertyValues.getTncDescription());
        tnc.getLinks().add(new Link(
                selfLink,
                this.noticePropertyValues.getTncLinkrel(),
                this.noticePropertyValues.getLinkHref(),
                null,
                null,
                this.noticePropertyValues.getLinkType()));
        return tnc;
    }
}
