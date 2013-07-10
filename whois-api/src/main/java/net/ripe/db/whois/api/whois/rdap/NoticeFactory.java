package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NoticeFactory {

    private String tncTitle;
    private String tncDescription;
    private String tncLinkRel;
    private String tncLinkHref;
    private String tncLinkType;

    private static NoticeFactory noticeFactory;

    List<Notice> notices = new ArrayList<Notice>();

    public void setTncTitle(@Value("${tncTitle}") final String tncTitle) {
        this.tncTitle = tncTitle;
    }

    public void setTncDescription(@Value("${tncDescription}") final String tncDescription) {
        this.tncDescription = tncDescription;
    }

    public void setTncLinkRel(@Value("${tncLinkRel}") final String tncLinkRel) {
        this.tncLinkRel = tncLinkRel;
    }

    public void setTncLinkHref(@Value("${tncLinkHref}") final String tncLinkHref) {
        this.tncLinkHref = tncLinkHref;
    }

    public void setTncLinkType(@Value("${tncLinkType}") final String tncLinkType) {
        this.tncLinkType = tncLinkType;
    }

    public NoticeFactory () {
        if (noticeFactory == null) {
            noticeFactory = this;
        }

        /*
        Notice notice = new Notice();
        notice.setTitle(tncTitle);
        notice.getDescription().add("This is the RIPE Database query service.");
        notice.getDescription().add("The objects are in RDAP format.");

        Link link = new Link();
        link.setValue(self);
        link.setRel("terms-of-service");
        link.setHref("http://www.ripe.net/db/support/db-terms-conditions.pdf");
        link.setType("application/pdf");
        notice.setLinks(link);
        */
    }

    /*public static NoticeFactory (RpslObject rpslObject) {
        // add the tnc

    }*/

    public static List<Notice> generateNotices (String selfLink) {
        List<Notice> notices = new ArrayList<Notice>();

        // setup the tnc
        Notice tnc = new Notice();
        tnc.setTitle(noticeFactory.tncTitle);
        tnc.getDescription().add(noticeFactory.tncDescription);

        Link link = new Link();
        link.setValue(selfLink);
        link.setRel(noticeFactory.tncLinkRel);
        link.setHref(noticeFactory.tncLinkHref);
        link.setType(noticeFactory.tncLinkType);
        tnc.setLinks(link);

        notices.add(tnc);

        // add more here

        return notices;
    }
}
