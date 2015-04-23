package nl.grol.whois.data.model;

/* automatically generated: do not edit manually */

import com.google.common.base.Preconditions;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inet6num {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue inet6num = null;
    private CommentedValue netname = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private List<CommentedValue> country = new ArrayList<CommentedValue>();
    private CommentedValue geoloc = null;
    private List<CommentedValue> language = new ArrayList<CommentedValue>();
    private CommentedValue orgRef = null;
    private CommentedValue sponsoringOrgRef = null;
    private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
    private CommentedValue status = null;
    private CommentedValue assignmentSize = null;
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntLowerRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntRoutesRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntDomainsRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntIrtRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public Inet6num(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.inet6num = builder.inet6num;
        this.netname = builder.netname;
        this.descr = builder.descr;
        this.country = builder.country;
        this.geoloc = builder.geoloc;
        this.language = builder.language;
        this.orgRef = builder.orgRef;
        this.sponsoringOrgRef = builder.sponsoringOrgRef;
        this.adminCRef = builder.adminCRef;
        this.techCRef = builder.techCRef;
        this.status = builder.status;
        this.assignmentSize = builder.assignmentSize;
        this.remarks = builder.remarks;
        this.notify = builder.notify;
        this.mntByRef = builder.mntByRef;
        this.mntLowerRef = builder.mntLowerRef;
        this.mntRoutesRef = builder.mntRoutesRef;
        this.mntDomainsRef = builder.mntDomainsRef;
        this.mntIrtRef = builder.mntIrtRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // inet6num
    public CommentedValue getInet6num() {
        return this.inet6num;
    }

    public String getInet6numValue() {
        String value = null;
        if (this.inet6num != null) {
            value = this.inet6num.getValue();
        }
        return value;
    }

    // netname
    public CommentedValue getNetname() {
        return this.netname;
    }

    public String getNetnameValue() {
        String value = null;
        if (this.netname != null) {
            value = this.netname.getValue();
        }
        return value;
    }

    // descr
    public List<CommentedValue> getDescr() {
        return this.descr;
    }

    public List<String> getDescrValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.descr) {
            values.add(value.getValue());
        }
        return values;
    }

    // country
    public List<CommentedValue> getCountry() {
        return this.country;
    }

    public List<String> getCountryValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.country) {
            values.add(value.getValue());
        }
        return values;
    }

    // geoloc
    public boolean hasGeoloc() {
        return this.geoloc != null;
    }

    public CommentedValue getGeoloc() {
        return this.geoloc;
    }

    public String getGeolocValue() {
        String value = null;
        if (this.geoloc != null) {
            value = this.geoloc.getValue();
        }
        return value;
    }

    // language
    public boolean hasLanguage() {
        return this.language.size() > 0;
    }

    public List<CommentedValue> getLanguage() {
        return this.language;
    }

    public List<String> getLanguageValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.language) {
            values.add(value.getValue());
        }
        return values;
    }

    // org
    public boolean hasOrgRef() {
        return this.orgRef != null;
    }

    public CommentedValue getOrgRef() {
        return this.orgRef;
    }

    public String getOrgRefValue() {
        String value = null;
        if (this.orgRef != null) {
            value = this.orgRef.getValue();
        }
        return value;
    }

    // sponsoring-org
    public boolean hasSponsoringOrgRef() {
        return this.sponsoringOrgRef != null;
    }

    public CommentedValue getSponsoringOrgRef() {
        return this.sponsoringOrgRef;
    }

    public String getSponsoringOrgRefValue() {
        String value = null;
        if (this.sponsoringOrgRef != null) {
            value = this.sponsoringOrgRef.getValue();
        }
        return value;
    }

    // admin-c
    public List<CommentedValue> getAdminCRef() {
        return this.adminCRef;
    }

    public List<String> getAdminCRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.adminCRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // tech-c
    public List<CommentedValue> getTechCRef() {
        return this.techCRef;
    }

    public List<String> getTechCRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.techCRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // status
    public CommentedValue getStatus() {
        return this.status;
    }

    public String getStatusValue() {
        String value = null;
        if (this.status != null) {
            value = this.status.getValue();
        }
        return value;
    }

    // assignment-size
    public boolean hasAssignmentSize() {
        return this.assignmentSize != null;
    }

    public CommentedValue getAssignmentSize() {
        return this.assignmentSize;
    }

    public String getAssignmentSizeValue() {
        String value = null;
        if (this.assignmentSize != null) {
            value = this.assignmentSize.getValue();
        }
        return value;
    }

    // remarks
    public boolean hasRemarks() {
        return this.remarks.size() > 0;
    }

    public List<CommentedValue> getRemarks() {
        return this.remarks;
    }

    public List<String> getRemarksValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.remarks) {
            values.add(value.getValue());
        }
        return values;
    }

    // notify
    public boolean hasNotify() {
        return this.notify.size() > 0;
    }

    public List<CommentedValue> getNotify() {
        return this.notify;
    }

    public List<String> getNotifyValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.notify) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-by
    public List<CommentedValue> getMntByRef() {
        return this.mntByRef;
    }

    public List<String> getMntByRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntByRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-lower
    public boolean hasMntLowerRef() {
        return this.mntLowerRef.size() > 0;
    }

    public List<CommentedValue> getMntLowerRef() {
        return this.mntLowerRef;
    }

    public List<String> getMntLowerRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntLowerRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-routes
    public boolean hasMntRoutesRef() {
        return this.mntRoutesRef.size() > 0;
    }

    public List<CommentedValue> getMntRoutesRef() {
        return this.mntRoutesRef;
    }

    public List<String> getMntRoutesRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntRoutesRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-domains
    public boolean hasMntDomainsRef() {
        return this.mntDomainsRef.size() > 0;
    }

    public List<CommentedValue> getMntDomainsRef() {
        return this.mntDomainsRef;
    }

    public List<String> getMntDomainsRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntDomainsRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-irt
    public boolean hasMntIrtRef() {
        return this.mntIrtRef.size() > 0;
    }

    public List<CommentedValue> getMntIrtRef() {
        return this.mntIrtRef;
    }

    public List<String> getMntIrtRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntIrtRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // changed
    public List<CommentedValue> getChanged() {
        return this.changed;
    }

    public List<String> getChangedValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.changed) {
            values.add(value.getValue());
        }
        return values;
    }

    // created
    public boolean hasCreated() {
        return this.created != null;
    }

    public CommentedValue getCreated() {
        return this.created;
    }

    public String getCreatedValue() {
        String value = null;
        if (this.created != null) {
            value = this.created.getValue();
        }
        return value;
    }

    // last-modified
    public boolean hasLastModified() {
        return this.lastModified != null;
    }

    public CommentedValue getLastModified() {
        return this.lastModified;
    }

    public String getLastModifiedValue() {
        String value = null;
        if (this.lastModified != null) {
            value = this.lastModified.getValue();
        }
        return value;
    }

    // source
    public CommentedValue getSource() {
        return this.source;
    }

    public String getSourceValue() {
        String value = null;
        if (this.source != null) {
            value = this.source.getValue();
        }
        return value;
    }


    public void validate() {
        Preconditions.checkState(inet6num != null && inet6num.getValue() != null, "Missing (single) mandatory attribute 'inet6num'");
        Preconditions.checkState(netname != null && netname.getValue() != null, "Missing (single) mandatory attribute 'netname'");
        Preconditions.checkState(descr != null && descr.size() > 0, "Missing (multiple) mandatory attribute 'descr'");
        Preconditions.checkState(country != null && country.size() > 0, "Missing (multiple) mandatory attribute 'country'");
        Preconditions.checkState(adminCRef != null && adminCRef.size() > 0, "Missing (multiple) mandatory attribute 'admin-c'");
        Preconditions.checkState(techCRef != null && techCRef.size() > 0, "Missing (multiple) mandatory attribute 'tech-c'");
        Preconditions.checkState(status != null && status.getValue() != null, "Missing (single) mandatory attribute 'status'");
        Preconditions.checkState(mntByRef != null && mntByRef.size() > 0, "Missing (multiple) mandatory attribute 'mnt-by'");
        Preconditions.checkState(changed != null && changed.size() > 0, "Missing (multiple) mandatory attribute 'changed'");
        Preconditions.checkState(source != null && source.getValue() != null, "Missing (single) mandatory attribute 'source'");

    }


    public WhoisResources toRequest() {
        WhoisResources whoisResources = new WhoisResources();

        WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(new Source(this.source.getValue()));
        whoisObject.setType("inet6num");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (inet6num != null) {
            if (!ATTR_NOT_IN_REQ.contains("inet6num")) {
                attributes.add(new Attribute("inet6num",
                        inet6num.getValue(),
                        inet6num.getComment(),
                        null, null));
            }
        }
        if (netname != null) {
            if (!ATTR_NOT_IN_REQ.contains("netname")) {
                attributes.add(new Attribute("netname",
                        netname.getValue(),
                        netname.getComment(),
                        null, null));
            }
        }
        if (descr != null) {
            if (!ATTR_NOT_IN_REQ.contains("descr")) {
                for (CommentedValue value : descr) {
                    attributes.add(new Attribute("descr",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (country != null) {
            if (!ATTR_NOT_IN_REQ.contains("country")) {
                for (CommentedValue value : country) {
                    attributes.add(new Attribute("country",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (geoloc != null) {
            if (!ATTR_NOT_IN_REQ.contains("geoloc")) {
                attributes.add(new Attribute("geoloc",
                        geoloc.getValue(),
                        geoloc.getComment(),
                        null, null));
            }
        }
        if (language != null) {
            if (!ATTR_NOT_IN_REQ.contains("language")) {
                for (CommentedValue value : language) {
                    attributes.add(new Attribute("language",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (orgRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("org")) {
                attributes.add(new Attribute("org",
                        orgRef.getValue(),
                        orgRef.getComment(),
                        null, null));
            }
        }
        if (sponsoringOrgRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("sponsoring-org")) {
                attributes.add(new Attribute("sponsoring-org",
                        sponsoringOrgRef.getValue(),
                        sponsoringOrgRef.getComment(),
                        null, null));
            }
        }
        if (adminCRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("admin-c")) {
                for (CommentedValue value : adminCRef) {
                    attributes.add(new Attribute("admin-c",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (techCRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("tech-c")) {
                for (CommentedValue value : techCRef) {
                    attributes.add(new Attribute("tech-c",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (status != null) {
            if (!ATTR_NOT_IN_REQ.contains("status")) {
                attributes.add(new Attribute("status",
                        status.getValue(),
                        status.getComment(),
                        null, null));
            }
        }
        if (assignmentSize != null) {
            if (!ATTR_NOT_IN_REQ.contains("assignment-size")) {
                attributes.add(new Attribute("assignment-size",
                        assignmentSize.getValue(),
                        assignmentSize.getComment(),
                        null, null));
            }
        }
        if (remarks != null) {
            if (!ATTR_NOT_IN_REQ.contains("remarks")) {
                for (CommentedValue value : remarks) {
                    attributes.add(new Attribute("remarks",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (notify != null) {
            if (!ATTR_NOT_IN_REQ.contains("notify")) {
                for (CommentedValue value : notify) {
                    attributes.add(new Attribute("notify",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntByRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-by")) {
                for (CommentedValue value : mntByRef) {
                    attributes.add(new Attribute("mnt-by",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntLowerRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-lower")) {
                for (CommentedValue value : mntLowerRef) {
                    attributes.add(new Attribute("mnt-lower",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntRoutesRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-routes")) {
                for (CommentedValue value : mntRoutesRef) {
                    attributes.add(new Attribute("mnt-routes",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntDomainsRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-domains")) {
                for (CommentedValue value : mntDomainsRef) {
                    attributes.add(new Attribute("mnt-domains",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntIrtRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-irt")) {
                for (CommentedValue value : mntIrtRef) {
                    attributes.add(new Attribute("mnt-irt",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (changed != null) {
            if (!ATTR_NOT_IN_REQ.contains("changed")) {
                for (CommentedValue value : changed) {
                    attributes.add(new Attribute("changed",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (created != null) {
            if (!ATTR_NOT_IN_REQ.contains("created")) {
                attributes.add(new Attribute("created",
                        created.getValue(),
                        created.getComment(),
                        null, null));
            }
        }
        if (lastModified != null) {
            if (!ATTR_NOT_IN_REQ.contains("last-modified")) {
                attributes.add(new Attribute("last-modified",
                        lastModified.getValue(),
                        lastModified.getComment(),
                        null, null));
            }
        }
        if (source != null) {
            if (!ATTR_NOT_IN_REQ.contains("source")) {
                attributes.add(new Attribute("source",
                        source.getValue(),
                        source.getComment(),
                        null, null));
            }
        }
        whoisObject.setAttributes(attributes);

        whoisResources.setWhoisObjects(Arrays.asList(whoisObject));
        return whoisResources;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        appendRpslAttribute(sb, "inet6num", this.inet6num);
        appendRpslAttribute(sb, "netname", this.netname);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "country", this.country);
        appendRpslAttribute(sb, "geoloc", this.geoloc);
        appendRpslAttribute(sb, "language", this.language);
        appendRpslAttribute(sb, "org", this.orgRef);
        appendRpslAttribute(sb, "sponsoring-org", this.sponsoringOrgRef);
        appendRpslAttribute(sb, "admin-c", this.adminCRef);
        appendRpslAttribute(sb, "tech-c", this.techCRef);
        appendRpslAttribute(sb, "status", this.status);
        appendRpslAttribute(sb, "assignment-size", this.assignmentSize);
        appendRpslAttribute(sb, "remarks", this.remarks);
        appendRpslAttribute(sb, "notify", this.notify);
        appendRpslAttribute(sb, "mnt-by", this.mntByRef);
        appendRpslAttribute(sb, "mnt-lower", this.mntLowerRef);
        appendRpslAttribute(sb, "mnt-routes", this.mntRoutesRef);
        appendRpslAttribute(sb, "mnt-domains", this.mntDomainsRef);
        appendRpslAttribute(sb, "mnt-irt", this.mntIrtRef);
        appendRpslAttribute(sb, "changed", this.changed);
        appendRpslAttribute(sb, "created", this.created);
        appendRpslAttribute(sb, "last-modified", this.lastModified);
        appendRpslAttribute(sb, "source", this.source);
        sb.append("\n");
        return sb.toString();
    }

    private void appendRpslAttribute(StringBuffer sb, String key, CommentedValue value) {
        if (value != null) {
            String comment = value.getComment() == null ? "" : String.format(" # %s", value.getComment());
            sb.append(String.format("\t%-20s%s%s\n", String.format("%s:", key), value.getValue(), comment));
        }
    }

    private void appendRpslAttribute(StringBuffer sb, String key, List<CommentedValue> values) {
        for (CommentedValue value : values) {
            String comment = value.getComment() == null ? "" : String.format(" # %s", value.getComment());
            sb.append(String.format("\t%-20s%s%s\n", String.format("%s:", key), value.getValue(), comment));
        }
    }

    public static class Builder {

        private CommentedValue inet6num = null;
        private CommentedValue netname = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private List<CommentedValue> country = new ArrayList<CommentedValue>();
        private CommentedValue geoloc = null;
        private List<CommentedValue> language = new ArrayList<CommentedValue>();
        private CommentedValue orgRef = null;
        private CommentedValue sponsoringOrgRef = null;
        private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
        private CommentedValue status = null;
        private CommentedValue assignmentSize = null;
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
        private List<CommentedValue> notify = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntLowerRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntRoutesRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntDomainsRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntIrtRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> changed = new ArrayList<CommentedValue>();
        private CommentedValue created = null;
        private CommentedValue lastModified = null;
        private CommentedValue source = null;


        public static Builder fromResponse(WhoisResources whoisResources) {
            Builder builder = new Builder();
            for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
                if (whoisObject.getType().equals("inet6num")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("inet6num")) {
                            builder.setInet6num(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("netname")) {
                            builder.setNetname(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("country")) {
                            builder.addCountry(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("geoloc")) {
                            builder.setGeoloc(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("language")) {
                            builder.addLanguage(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("org")) {
                            builder.setOrgRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("sponsoring-org")) {
                            builder.setSponsoringOrgRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("admin-c")) {
                            builder.addAdminCRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("tech-c")) {
                            builder.addTechCRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("status")) {
                            builder.setStatus(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("assignment-size")) {
                            builder.setAssignmentSize(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("notify")) {
                            builder.addNotify(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-by")) {
                            builder.addMntByRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-lower")) {
                            builder.addMntLowerRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-routes")) {
                            builder.addMntRoutesRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-domains")) {
                            builder.addMntDomainsRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-irt")) {
                            builder.addMntIrtRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("changed")) {
                            builder.addChanged(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("created")) {
                            builder.setCreated(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("last-modified")) {
                            builder.setLastModified(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("source")) {
                            builder.setSource(attr.getValue(), attr.getComment());
                        }

                    }
                    break;
                }
            }
            return builder;
        }


        // inet6num
        public CommentedValue getInet6num() {
            return this.inet6num;
        }

        public Builder setInet6num(final String value) {
            Preconditions.checkState(value != null);
            this.inet6num = new CommentedValue(value);
            return this;
        }

        public String getInet6numValue() {
            String value = null;
            if (this.inet6num != null) {
                value = this.inet6num.getValue();
            }
            return value;
        }

        public Builder setInet6num(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.inet6num = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeInet6num() {
            this.inet6num = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetInet6num(final String value) {
            Preconditions.checkState(value != null);
            this.setInet6num(value);
            return this;
        }

        public Builder mandatorySetInet6num(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setInet6num(value, comment);
            return this;
        }

        // netname
        public CommentedValue getNetname() {
            return this.netname;
        }

        public Builder setNetname(final String value) {
            Preconditions.checkState(value != null);
            this.netname = new CommentedValue(value);
            return this;
        }

        public String getNetnameValue() {
            String value = null;
            if (this.netname != null) {
                value = this.netname.getValue();
            }
            return value;
        }

        public Builder setNetname(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.netname = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeNetname() {
            this.netname = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetNetname(final String value) {
            Preconditions.checkState(value != null);
            this.setNetname(value);
            return this;
        }

        public Builder mandatorySetNetname(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setNetname(value, comment);
            return this;
        }

        // descr
        public List<CommentedValue> getDescr() {
            return this.descr;
        }

        public Builder setDescr(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.descr = values;
            return this;
        }

        public List<String> getDescrValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : descr) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addDescr(final String value) {
            Preconditions.checkState(value != null);
            this.descr.add(new CommentedValue(value));
            return this;
        }

        public Builder addDescr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.descr.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeDescrAll() {
            this.descr.clear();
            return this;
        }

        public Builder removeDescrAt(int index) {
            Preconditions.checkState(index >= 0 && index < descr.size(), "Invalid remove-at index for (multiple) attribute 'descr'");
            this.descr.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddDescr(final String value) {
            Preconditions.checkState(value != null);
            return addDescr(value);
        }

        public Builder mandatoryAddDescr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addDescr(value, comment);
        }

        // country
        public List<CommentedValue> getCountry() {
            return this.country;
        }

        public Builder setCountry(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.country = values;
            return this;
        }

        public List<String> getCountryValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : country) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addCountry(final String value) {
            Preconditions.checkState(value != null);
            this.country.add(new CommentedValue(value));
            return this;
        }

        public Builder addCountry(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.country.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeCountryAll() {
            this.country.clear();
            return this;
        }

        public Builder removeCountryAt(int index) {
            Preconditions.checkState(index >= 0 && index < country.size(), "Invalid remove-at index for (multiple) attribute 'country'");
            this.country.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddCountry(final String value) {
            Preconditions.checkState(value != null);
            return addCountry(value);
        }

        public Builder mandatoryAddCountry(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addCountry(value, comment);
        }

        // geoloc
        public boolean hasGeoloc() {
            return this.geoloc != null;
        }

        public CommentedValue getGeoloc() {
            return this.geoloc;
        }

        public Builder setGeoloc(final String value) {
            Preconditions.checkState(value != null);
            this.geoloc = new CommentedValue(value);
            return this;
        }

        public String getGeolocValue() {
            String value = null;
            if (this.geoloc != null) {
                value = this.geoloc.getValue();
            }
            return value;
        }

        public Builder setGeoloc(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.geoloc = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeGeoloc() {
            this.geoloc = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetGeoloc(final String value) {
            Preconditions.checkState(value != null);
            this.setGeoloc(value);
            return this;
        }

        public Builder optionalSetGeoloc(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setGeoloc(value, comment);
            return this;
        }

        // language
        public boolean hasLanguage() {
            return this.language.size() > 0;
        }

        public List<CommentedValue> getLanguage() {
            return this.language;
        }

        public Builder setLanguage(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.language = values;
            return this;
        }

        public List<String> getLanguageValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : language) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addLanguage(final String value) {
            Preconditions.checkState(value != null);
            this.language.add(new CommentedValue(value));
            return this;
        }

        public Builder addLanguage(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.language.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeLanguageAll() {
            this.language.clear();
            return this;
        }

        public Builder removeLanguageAt(int index) {
            Preconditions.checkState(index >= 0 && index < language.size(), "Invalid remove-at index for (multiple) attribute 'language'");
            this.language.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddLanguage(final String value) {
            Preconditions.checkState(value != null);
            return addLanguage(value);
        }

        public Builder optionalAddLanguage(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addLanguage(value, comment);
        }

        // org
        public boolean hasOrgRef() {
            return this.orgRef != null;
        }

        public CommentedValue getOrgRef() {
            return this.orgRef;
        }

        public Builder setOrgRef(final String value) {
            Preconditions.checkState(value != null);
            this.orgRef = new CommentedValue(value);
            return this;
        }

        public String getOrgRefValue() {
            String value = null;
            if (this.orgRef != null) {
                value = this.orgRef.getValue();
            }
            return value;
        }

        public Builder setOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.orgRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeOrgRef() {
            this.orgRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetOrgRef(final String value) {
            Preconditions.checkState(value != null);
            this.setOrgRef(value);
            return this;
        }

        public Builder optionalSetOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setOrgRef(value, comment);
            return this;
        }

        // sponsoring-org
        public boolean hasSponsoringOrgRef() {
            return this.sponsoringOrgRef != null;
        }

        public CommentedValue getSponsoringOrgRef() {
            return this.sponsoringOrgRef;
        }

        public Builder setSponsoringOrgRef(final String value) {
            Preconditions.checkState(value != null);
            this.sponsoringOrgRef = new CommentedValue(value);
            return this;
        }

        public String getSponsoringOrgRefValue() {
            String value = null;
            if (this.sponsoringOrgRef != null) {
                value = this.sponsoringOrgRef.getValue();
            }
            return value;
        }

        public Builder setSponsoringOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.sponsoringOrgRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeSponsoringOrgRef() {
            this.sponsoringOrgRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetSponsoringOrgRef(final String value) {
            Preconditions.checkState(value != null);
            this.setSponsoringOrgRef(value);
            return this;
        }

        public Builder optionalSetSponsoringOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setSponsoringOrgRef(value, comment);
            return this;
        }

        // admin-c
        public List<CommentedValue> getAdminCRef() {
            return this.adminCRef;
        }

        public Builder setAdminCRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.adminCRef = values;
            return this;
        }

        public List<String> getAdminCRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : adminCRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addAdminCRef(final String value) {
            Preconditions.checkState(value != null);
            this.adminCRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addAdminCRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.adminCRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeAdminCRefAll() {
            this.adminCRef.clear();
            return this;
        }

        public Builder removeAdminCRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < adminCRef.size(), "Invalid remove-at index for (multiple) attribute 'admin-c'");
            this.adminCRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddAdminCRef(final String value) {
            Preconditions.checkState(value != null);
            return addAdminCRef(value);
        }

        public Builder mandatoryAddAdminCRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addAdminCRef(value, comment);
        }

        // tech-c
        public List<CommentedValue> getTechCRef() {
            return this.techCRef;
        }

        public Builder setTechCRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.techCRef = values;
            return this;
        }

        public List<String> getTechCRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : techCRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addTechCRef(final String value) {
            Preconditions.checkState(value != null);
            this.techCRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addTechCRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.techCRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeTechCRefAll() {
            this.techCRef.clear();
            return this;
        }

        public Builder removeTechCRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < techCRef.size(), "Invalid remove-at index for (multiple) attribute 'tech-c'");
            this.techCRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddTechCRef(final String value) {
            Preconditions.checkState(value != null);
            return addTechCRef(value);
        }

        public Builder mandatoryAddTechCRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addTechCRef(value, comment);
        }

        // status
        public CommentedValue getStatus() {
            return this.status;
        }

        public Builder setStatus(final String value) {
            Preconditions.checkState(value != null);
            this.status = new CommentedValue(value);
            return this;
        }

        public String getStatusValue() {
            String value = null;
            if (this.status != null) {
                value = this.status.getValue();
            }
            return value;
        }

        public Builder setStatus(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.status = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeStatus() {
            this.status = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetStatus(final String value) {
            Preconditions.checkState(value != null);
            this.setStatus(value);
            return this;
        }

        public Builder mandatorySetStatus(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setStatus(value, comment);
            return this;
        }

        // assignment-size
        public boolean hasAssignmentSize() {
            return this.assignmentSize != null;
        }

        public CommentedValue getAssignmentSize() {
            return this.assignmentSize;
        }

        public Builder setAssignmentSize(final String value) {
            Preconditions.checkState(value != null);
            this.assignmentSize = new CommentedValue(value);
            return this;
        }

        public String getAssignmentSizeValue() {
            String value = null;
            if (this.assignmentSize != null) {
                value = this.assignmentSize.getValue();
            }
            return value;
        }

        public Builder setAssignmentSize(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.assignmentSize = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeAssignmentSize() {
            this.assignmentSize = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetAssignmentSize(final String value) {
            Preconditions.checkState(value != null);
            this.setAssignmentSize(value);
            return this;
        }

        public Builder optionalSetAssignmentSize(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setAssignmentSize(value, comment);
            return this;
        }

        // remarks
        public boolean hasRemarks() {
            return this.remarks.size() > 0;
        }

        public List<CommentedValue> getRemarks() {
            return this.remarks;
        }

        public Builder setRemarks(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.remarks = values;
            return this;
        }

        public List<String> getRemarksValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : remarks) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addRemarks(final String value) {
            Preconditions.checkState(value != null);
            this.remarks.add(new CommentedValue(value));
            return this;
        }

        public Builder addRemarks(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.remarks.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeRemarksAll() {
            this.remarks.clear();
            return this;
        }

        public Builder removeRemarksAt(int index) {
            Preconditions.checkState(index >= 0 && index < remarks.size(), "Invalid remove-at index for (multiple) attribute 'remarks'");
            this.remarks.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddRemarks(final String value) {
            Preconditions.checkState(value != null);
            return addRemarks(value);
        }

        public Builder optionalAddRemarks(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addRemarks(value, comment);
        }

        // notify
        public boolean hasNotify() {
            return this.notify.size() > 0;
        }

        public List<CommentedValue> getNotify() {
            return this.notify;
        }

        public Builder setNotify(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.notify = values;
            return this;
        }

        public List<String> getNotifyValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : notify) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addNotify(final String value) {
            Preconditions.checkState(value != null);
            this.notify.add(new CommentedValue(value));
            return this;
        }

        public Builder addNotify(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.notify.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeNotifyAll() {
            this.notify.clear();
            return this;
        }

        public Builder removeNotifyAt(int index) {
            Preconditions.checkState(index >= 0 && index < notify.size(), "Invalid remove-at index for (multiple) attribute 'notify'");
            this.notify.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddNotify(final String value) {
            Preconditions.checkState(value != null);
            return addNotify(value);
        }

        public Builder optionalAddNotify(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addNotify(value, comment);
        }

        // mnt-by
        public List<CommentedValue> getMntByRef() {
            return this.mntByRef;
        }

        public Builder setMntByRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntByRef = values;
            return this;
        }

        public List<String> getMntByRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntByRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntByRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntByRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntByRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntByRefAll() {
            this.mntByRef.clear();
            return this;
        }

        public Builder removeMntByRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntByRef.size(), "Invalid remove-at index for (multiple) attribute 'mnt-by'");
            this.mntByRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddMntByRef(final String value) {
            Preconditions.checkState(value != null);
            return addMntByRef(value);
        }

        public Builder mandatoryAddMntByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntByRef(value, comment);
        }

        // mnt-lower
        public boolean hasMntLowerRef() {
            return this.mntLowerRef.size() > 0;
        }

        public List<CommentedValue> getMntLowerRef() {
            return this.mntLowerRef;
        }

        public Builder setMntLowerRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntLowerRef = values;
            return this;
        }

        public List<String> getMntLowerRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntLowerRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntLowerRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntLowerRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntLowerRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntLowerRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntLowerRefAll() {
            this.mntLowerRef.clear();
            return this;
        }

        public Builder removeMntLowerRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntLowerRef.size(), "Invalid remove-at index for (multiple) attribute 'mnt-lower'");
            this.mntLowerRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMntLowerRef(final String value) {
            Preconditions.checkState(value != null);
            return addMntLowerRef(value);
        }

        public Builder optionalAddMntLowerRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntLowerRef(value, comment);
        }

        // mnt-routes
        public boolean hasMntRoutesRef() {
            return this.mntRoutesRef.size() > 0;
        }

        public List<CommentedValue> getMntRoutesRef() {
            return this.mntRoutesRef;
        }

        public Builder setMntRoutesRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntRoutesRef = values;
            return this;
        }

        public List<String> getMntRoutesRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntRoutesRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntRoutesRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntRoutesRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntRoutesRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntRoutesRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntRoutesRefAll() {
            this.mntRoutesRef.clear();
            return this;
        }

        public Builder removeMntRoutesRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntRoutesRef.size(), "Invalid remove-at index for (multiple) attribute 'mnt-routes'");
            this.mntRoutesRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMntRoutesRef(final String value) {
            Preconditions.checkState(value != null);
            return addMntRoutesRef(value);
        }

        public Builder optionalAddMntRoutesRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntRoutesRef(value, comment);
        }

        // mnt-domains
        public boolean hasMntDomainsRef() {
            return this.mntDomainsRef.size() > 0;
        }

        public List<CommentedValue> getMntDomainsRef() {
            return this.mntDomainsRef;
        }

        public Builder setMntDomainsRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntDomainsRef = values;
            return this;
        }

        public List<String> getMntDomainsRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntDomainsRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntDomainsRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntDomainsRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntDomainsRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntDomainsRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntDomainsRefAll() {
            this.mntDomainsRef.clear();
            return this;
        }

        public Builder removeMntDomainsRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntDomainsRef.size(), "Invalid remove-at index for (multiple) attribute 'mnt-domains'");
            this.mntDomainsRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMntDomainsRef(final String value) {
            Preconditions.checkState(value != null);
            return addMntDomainsRef(value);
        }

        public Builder optionalAddMntDomainsRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntDomainsRef(value, comment);
        }

        // mnt-irt
        public boolean hasMntIrtRef() {
            return this.mntIrtRef.size() > 0;
        }

        public List<CommentedValue> getMntIrtRef() {
            return this.mntIrtRef;
        }

        public Builder setMntIrtRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntIrtRef = values;
            return this;
        }

        public List<String> getMntIrtRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntIrtRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntIrtRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntIrtRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntIrtRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntIrtRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntIrtRefAll() {
            this.mntIrtRef.clear();
            return this;
        }

        public Builder removeMntIrtRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntIrtRef.size(), "Invalid remove-at index for (multiple) attribute 'mnt-irt'");
            this.mntIrtRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMntIrtRef(final String value) {
            Preconditions.checkState(value != null);
            return addMntIrtRef(value);
        }

        public Builder optionalAddMntIrtRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntIrtRef(value, comment);
        }

        // changed
        public List<CommentedValue> getChanged() {
            return this.changed;
        }

        public Builder setChanged(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.changed = values;
            return this;
        }

        public List<String> getChangedValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : changed) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addChanged(final String value) {
            Preconditions.checkState(value != null);
            this.changed.add(new CommentedValue(value));
            return this;
        }

        public Builder addChanged(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.changed.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeChangedAll() {
            this.changed.clear();
            return this;
        }

        public Builder removeChangedAt(int index) {
            Preconditions.checkState(index >= 0 && index < changed.size(), "Invalid remove-at index for (multiple) attribute 'changed'");
            this.changed.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddChanged(final String value) {
            Preconditions.checkState(value != null);
            return addChanged(value);
        }

        public Builder mandatoryAddChanged(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addChanged(value, comment);
        }

        // created
        public boolean hasCreated() {
            return this.created != null;
        }

        public CommentedValue getCreated() {
            return this.created;
        }

        public Builder setCreated(final String value) {
            Preconditions.checkState(value != null);
            this.created = new CommentedValue(value);
            return this;
        }

        public String getCreatedValue() {
            String value = null;
            if (this.created != null) {
                value = this.created.getValue();
            }
            return value;
        }

        public Builder setCreated(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.created = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeCreated() {
            this.created = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetCreated(final String value) {
            Preconditions.checkState(value != null);
            this.setCreated(value);
            return this;
        }

        public Builder optionalSetCreated(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setCreated(value, comment);
            return this;
        }

        // last-modified
        public boolean hasLastModified() {
            return this.lastModified != null;
        }

        public CommentedValue getLastModified() {
            return this.lastModified;
        }

        public Builder setLastModified(final String value) {
            Preconditions.checkState(value != null);
            this.lastModified = new CommentedValue(value);
            return this;
        }

        public String getLastModifiedValue() {
            String value = null;
            if (this.lastModified != null) {
                value = this.lastModified.getValue();
            }
            return value;
        }

        public Builder setLastModified(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.lastModified = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeLastModified() {
            this.lastModified = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetLastModified(final String value) {
            Preconditions.checkState(value != null);
            this.setLastModified(value);
            return this;
        }

        public Builder optionalSetLastModified(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setLastModified(value, comment);
            return this;
        }

        // source
        public CommentedValue getSource() {
            return this.source;
        }

        public Builder setSource(final String value) {
            Preconditions.checkState(value != null);
            this.source = new CommentedValue(value);
            return this;
        }

        public String getSourceValue() {
            String value = null;
            if (this.source != null) {
                value = this.source.getValue();
            }
            return value;
        }

        public Builder setSource(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.source = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeSource() {
            this.source = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetSource(final String value) {
            Preconditions.checkState(value != null);
            this.setSource(value);
            return this;
        }

        public Builder mandatorySetSource(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setSource(value, comment);
            return this;
        }


        public Inet6num build() {
            Inet6num obj = new Inet6num(this);
            obj.validate();
            return obj;
        }
    }

    ;

};