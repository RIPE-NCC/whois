package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component
public class NoticeFactory {

    @Value("${rdap.tnc.title}")
    private String rdap_tnc_title;

    @Value("${rdap.tnc.description}")
    private String rdap_tnc_description;

    @Value("${rdap.tnc.linkrel}")
    private String rdap_tnc_linkrel;

    @Value("${rdap.tnc.linkhref}")
    private String rdap_tnc_linkhref;

    @Value("${rdap.tnc.linktype}")
    private String rdap_tnc_linktype;

    private static NoticeFactory noticeFactory;
    private static Notice noticeTemplate;

    public NoticeFactory () {
    }

    @PostConstruct
    public void init() {
        if (noticeFactory == null) {
            noticeFactory = this;
        }

        // setup the tnc once
        noticeTemplate = new Notice();
        noticeTemplate.setTitle(noticeFactory.rdap_tnc_title);
        noticeTemplate.getDescription().add(noticeFactory.rdap_tnc_description);

        Link link = new Link();
        link.setRel(noticeFactory.rdap_tnc_linkrel);
        link.setHref(noticeFactory.rdap_tnc_linkhref);
        link.setType(noticeFactory.rdap_tnc_linktype);
        noticeTemplate.setLinks(link);
    }

    /*public static NoticeFactory (RpslObject rpslObject) {
        // add the tnc
    }*/

    public static List<Notice> generateNotices (String selfLink) {
        List<Notice> notices = new ArrayList<Notice>();
        if (noticeTemplate != null) {
            Notice notice = (Notice)SerializationUtils.clone(noticeTemplate);
            notice.getLinks().setValue(selfLink);
            notices.add(notice);

            // add more here
        }
        return notices;
    }
}
