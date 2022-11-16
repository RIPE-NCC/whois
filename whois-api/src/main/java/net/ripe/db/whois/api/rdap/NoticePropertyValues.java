package net.ripe.db.whois.api.rdap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class NoticePropertyValues {

    private final String tncTitle;
    private final String tncDescription;
    private final String tncLinkrel;
    private final String linkHref;
    private final String linkType;
    private final String isFiltered;
    private final String filterDescription;
    private final String filterTitle;
    private final String sourceDescription;
    private final String sourceTitle;
    private final String inaccuracyNoticeLinkRel;
    private final String inaccuracyNoticeTitle;
    private final String inaccuracyNoticeDescription;
    private final String inaccuracyNoticeLinkHref;

    public NoticePropertyValues(
            @Value("${rdap.tnc.title:}") final String tncTitle,
            @Value("${rdap.tnc.description:}") final String tncDescription,
            @Value("${rdap.tnc.linkrel:}") final String tncLinkrel,
            @Value("${rdap.tnc.linkhref:}") final String linkHref,
            @Value("${rdap.tnc.linktype:}") final String linkType,
            @Value("${rdap.filter.isfiltered:}") final String isFiltered,
            @Value("${rdap.filter.description:}") final String filterDescription,
            @Value("${rdap.filter.title:}") final String filterTitle,
            @Value("${rdap.source.description:}") final String sourceDescription,
            @Value("${rdap.source.title:}") final String sourceTitle,
            @Value("${rdap.inaccuracy_notice.linkrel:}") final String inaccuracyNoticeLinkRel,
            @Value("${rdap.inaccuracy_notice.title:}") final String inaccuracyNoticeTitle,
            @Value("${rdap.inaccuracy_notice.description:}") final String inaccuracyNoticeDescription,
            @Value("${rdap.inaccuracy_notice.linkhref:}") final String inaccuracyNoticeLinkHref
    ) {
        this.tncTitle = tncTitle;
        this.tncDescription = tncDescription;
        this.tncLinkrel = tncLinkrel;
        this.linkHref = linkHref;
        this.linkType = linkType;
        this.isFiltered = isFiltered;
        this.filterDescription = filterDescription;
        this.filterTitle = filterTitle;
        this.sourceDescription = sourceDescription;
        this.sourceTitle = sourceTitle;
        this.inaccuracyNoticeLinkRel = inaccuracyNoticeLinkRel;
        this.inaccuracyNoticeTitle = inaccuracyNoticeTitle;
        this.inaccuracyNoticeDescription = inaccuracyNoticeDescription;
        this.inaccuracyNoticeLinkHref = inaccuracyNoticeLinkHref;
    }

    public String getTncTitle() {
        return tncTitle;
    }

    public String getTncDescription() {
        return tncDescription;
    }

    public String getTncLinkrel() {
        return tncLinkrel;
    }

    public String getLinkHref() {
        return linkHref;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getIsFiltered() {
        return isFiltered;
    }

    public String getFilterDescription() {
        return filterDescription;
    }

    public String getFilterTitle() {
        return filterTitle;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getInaccuracyNoticeLinkRel() {
        return inaccuracyNoticeLinkRel;
    }

    public String getInaccuracyNoticeTitle() {
        return inaccuracyNoticeTitle;
    }

    public String getInaccuracyNoticeDescription() {
        return inaccuracyNoticeDescription;
    }

    public String getInaccuracyNoticeLinkHref() {
        return inaccuracyNoticeLinkHref;
    }

}
