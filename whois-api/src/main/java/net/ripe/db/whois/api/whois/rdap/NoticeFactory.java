package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

    @Value("${rdap.tnc.title:}")
    private String rdap_tnc_title;

    @Value("${rdap.tnc.description:}")
    private String rdap_tnc_description;

    @Value("${rdap.tnc.linkrel:}")
    private String rdap_tnc_linkrel;

    @Value("${rdap.tnc.linkhref:}")
    private String rdap_tnc_linkhref;

    @Value("${rdap.tnc.linktype:}")
    private String rdap_tnc_linktype;

    @Value("${rdap.filter.isfiltered:}")
    private String rdap_filter_is_filtered;

    @Value("${rdap.filter.description:}")
    private String rdap_filter_description;

    @Value("${rdap.filter.title:}")
    private String rdap_filter_title;

    @Value("${rdap.source.description:}")
    private String rdap_source_description;

    @Value("${rdap.source.title:}")
    private String rdap_source_title;

    private static NoticeFactory noticeFactory;

    public NoticeFactory () {
    }

    @PostConstruct
    public void init() {
        if (noticeFactory == null) {
            noticeFactory = this;
        }
    }

    public static List<Notice> generateNotices (String selfLink, RpslObject rpslObject) {
        List<Notice> notices = new ArrayList<Notice>();

        if (noticeFactory != null) {
            // setup the tnc once
            Notice tnc = new Notice();
            tnc.setTitle(noticeFactory.rdap_tnc_title);
            tnc.getDescription().add(noticeFactory.rdap_tnc_description);

            Link link = new Link();
            link.setRel(noticeFactory.rdap_tnc_linkrel);
            link.setHref(noticeFactory.rdap_tnc_linkhref);
            link.setType(noticeFactory.rdap_tnc_linktype);
            link.setValue(selfLink);
            tnc.setLinks(link);

            notices.add(tnc);

            if (noticeFactory.rdap_filter_is_filtered.equals("true")) {
                Notice filtered = new Notice();
                filtered.setTitle(noticeFactory.rdap_filter_title);
                filtered.getDescription().add(noticeFactory.rdap_filter_description);

                notices.add(filtered);
            }

            List<RpslAttribute> rpslAttributeList = rpslObject.findAttributes(AttributeType.SOURCE);
            CIString sourceName = rpslAttributeList.get(0).getCleanValue();
            Notice source = new Notice();
            source.setTitle(noticeFactory.rdap_source_title);
            source.getDescription().add(noticeFactory.rdap_source_description);
            source.getDescription().add(sourceName.toString());

            notices.add(source);
        }
        return notices;
    }
}
