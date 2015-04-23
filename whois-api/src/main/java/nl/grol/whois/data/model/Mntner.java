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

public class Mntner {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue mntner = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> updTo = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntNfy = new ArrayList<CommentedValue>();
    private List<CommentedValue> authRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private List<CommentedValue> abuseMailbox = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
    private CommentedValue referralByRef = null;
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public Mntner(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.mntner = builder.mntner;
        this.descr = builder.descr;
        this.orgRef = builder.orgRef;
        this.adminCRef = builder.adminCRef;
        this.techCRef = builder.techCRef;
        this.updTo = builder.updTo;
        this.mntNfy = builder.mntNfy;
        this.authRef = builder.authRef;
        this.remarks = builder.remarks;
        this.notify = builder.notify;
        this.abuseMailbox = builder.abuseMailbox;
        this.mntByRef = builder.mntByRef;
        this.referralByRef = builder.referralByRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // mntner
    public CommentedValue getMntner() {
        return this.mntner;
    }

    public String getMntnerValue() {
        String value = null;
        if (this.mntner != null) {
            value = this.mntner.getValue();
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

    // org
    public boolean hasOrgRef() {
        return this.orgRef.size() > 0;
    }

    public List<CommentedValue> getOrgRef() {
        return this.orgRef;
    }

    public List<String> getOrgRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.orgRef) {
            values.add(value.getValue());
        }
        return values;
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
    public boolean hasTechCRef() {
        return this.techCRef.size() > 0;
    }

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

    // upd-to
    public List<CommentedValue> getUpdTo() {
        return this.updTo;
    }

    public List<String> getUpdToValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.updTo) {
            values.add(value.getValue());
        }
        return values;
    }

    // mnt-nfy
    public boolean hasMntNfy() {
        return this.mntNfy.size() > 0;
    }

    public List<CommentedValue> getMntNfy() {
        return this.mntNfy;
    }

    public List<String> getMntNfyValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mntNfy) {
            values.add(value.getValue());
        }
        return values;
    }

    // auth
    public List<CommentedValue> getAuthRef() {
        return this.authRef;
    }

    public List<String> getAuthRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.authRef) {
            values.add(value.getValue());
        }
        return values;
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

    // abuse-mailbox
    public boolean hasAbuseMailbox() {
        return this.abuseMailbox.size() > 0;
    }

    public List<CommentedValue> getAbuseMailbox() {
        return this.abuseMailbox;
    }

    public List<String> getAbuseMailboxValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.abuseMailbox) {
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

    // referral-by
    public CommentedValue getReferralByRef() {
        return this.referralByRef;
    }

    public String getReferralByRefValue() {
        String value = null;
        if (this.referralByRef != null) {
            value = this.referralByRef.getValue();
        }
        return value;
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
        Preconditions.checkState(mntner != null && mntner.getValue() != null, "Missing (single) mandatory attribute 'mntner'");
        Preconditions.checkState(descr != null && descr.size() > 0, "Missing (multiple) mandatory attribute 'descr'");
        Preconditions.checkState(adminCRef != null && adminCRef.size() > 0, "Missing (multiple) mandatory attribute 'admin-c'");
        Preconditions.checkState(updTo != null && updTo.size() > 0, "Missing (multiple) mandatory attribute 'upd-to'");
        Preconditions.checkState(authRef != null && authRef.size() > 0, "Missing (multiple) mandatory attribute 'auth'");
        Preconditions.checkState(mntByRef != null && mntByRef.size() > 0, "Missing (multiple) mandatory attribute 'mnt-by'");
        Preconditions.checkState(referralByRef != null && referralByRef.getValue() != null, "Missing (single) mandatory attribute 'referral-by'");
        Preconditions.checkState(changed != null && changed.size() > 0, "Missing (multiple) mandatory attribute 'changed'");
        Preconditions.checkState(source != null && source.getValue() != null, "Missing (single) mandatory attribute 'source'");

    }


    public WhoisResources toRequest() {
        WhoisResources whoisResources = new WhoisResources();

        WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(new Source(this.source.getValue()));
        whoisObject.setType("mntner");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (mntner != null) {
            if (!ATTR_NOT_IN_REQ.contains("mntner")) {
                attributes.add(new Attribute("mntner",
                        mntner.getValue(),
                        mntner.getComment(),
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
        if (orgRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("org")) {
                for (CommentedValue value : orgRef) {
                    attributes.add(new Attribute("org",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
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
        if (updTo != null) {
            if (!ATTR_NOT_IN_REQ.contains("upd-to")) {
                for (CommentedValue value : updTo) {
                    attributes.add(new Attribute("upd-to",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mntNfy != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-nfy")) {
                for (CommentedValue value : mntNfy) {
                    attributes.add(new Attribute("mnt-nfy",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (authRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("auth")) {
                for (CommentedValue value : authRef) {
                    attributes.add(new Attribute("auth",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
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
        if (abuseMailbox != null) {
            if (!ATTR_NOT_IN_REQ.contains("abuse-mailbox")) {
                for (CommentedValue value : abuseMailbox) {
                    attributes.add(new Attribute("abuse-mailbox",
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
        if (referralByRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("referral-by")) {
                attributes.add(new Attribute("referral-by",
                        referralByRef.getValue(),
                        referralByRef.getComment(),
                        null, null));
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
        appendRpslAttribute(sb, "mntner", this.mntner);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "org", this.orgRef);
        appendRpslAttribute(sb, "admin-c", this.adminCRef);
        appendRpslAttribute(sb, "tech-c", this.techCRef);
        appendRpslAttribute(sb, "upd-to", this.updTo);
        appendRpslAttribute(sb, "mnt-nfy", this.mntNfy);
        appendRpslAttribute(sb, "auth", this.authRef);
        appendRpslAttribute(sb, "remarks", this.remarks);
        appendRpslAttribute(sb, "notify", this.notify);
        appendRpslAttribute(sb, "abuse-mailbox", this.abuseMailbox);
        appendRpslAttribute(sb, "mnt-by", this.mntByRef);
        appendRpslAttribute(sb, "referral-by", this.referralByRef);
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

        private CommentedValue mntner = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> updTo = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntNfy = new ArrayList<CommentedValue>();
        private List<CommentedValue> authRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
        private List<CommentedValue> notify = new ArrayList<CommentedValue>();
        private List<CommentedValue> abuseMailbox = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
        private CommentedValue referralByRef = null;
        private List<CommentedValue> changed = new ArrayList<CommentedValue>();
        private CommentedValue created = null;
        private CommentedValue lastModified = null;
        private CommentedValue source = null;


        public static Builder fromResponse(WhoisResources whoisResources) {
            Builder builder = new Builder();
            for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
                if (whoisObject.getType().equals("mntner")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("mntner")) {
                            builder.setMntner(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("org")) {
                            builder.addOrgRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("admin-c")) {
                            builder.addAdminCRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("tech-c")) {
                            builder.addTechCRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("upd-to")) {
                            builder.addUpdTo(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-nfy")) {
                            builder.addMntNfy(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("auth")) {
                            builder.addAuthRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("notify")) {
                            builder.addNotify(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("abuse-mailbox")) {
                            builder.addAbuseMailbox(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-by")) {
                            builder.addMntByRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("referral-by")) {
                            builder.setReferralByRef(attr.getValue(), attr.getComment());
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


        // mntner
        public CommentedValue getMntner() {
            return this.mntner;
        }

        public Builder setMntner(final String value) {
            Preconditions.checkState(value != null);
            this.mntner = new CommentedValue(value);
            return this;
        }

        public String getMntnerValue() {
            String value = null;
            if (this.mntner != null) {
                value = this.mntner.getValue();
            }
            return value;
        }

        public Builder setMntner(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntner = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeMntner() {
            this.mntner = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetMntner(final String value) {
            Preconditions.checkState(value != null);
            this.setMntner(value);
            return this;
        }

        public Builder mandatorySetMntner(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setMntner(value, comment);
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

        // org
        public boolean hasOrgRef() {
            return this.orgRef.size() > 0;
        }

        public List<CommentedValue> getOrgRef() {
            return this.orgRef;
        }

        public Builder setOrgRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.orgRef = values;
            return this;
        }

        public List<String> getOrgRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : orgRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addOrgRef(final String value) {
            Preconditions.checkState(value != null);
            this.orgRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.orgRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeOrgRefAll() {
            this.orgRef.clear();
            return this;
        }

        public Builder removeOrgRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < orgRef.size(), "Invalid remove-at index for (multiple) attribute 'org'");
            this.orgRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddOrgRef(final String value) {
            Preconditions.checkState(value != null);
            return addOrgRef(value);
        }

        public Builder optionalAddOrgRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addOrgRef(value, comment);
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
        public boolean hasTechCRef() {
            return this.techCRef.size() > 0;
        }

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
        public Builder optionalAddTechCRef(final String value) {
            Preconditions.checkState(value != null);
            return addTechCRef(value);
        }

        public Builder optionalAddTechCRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addTechCRef(value, comment);
        }

        // upd-to
        public List<CommentedValue> getUpdTo() {
            return this.updTo;
        }

        public Builder setUpdTo(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.updTo = values;
            return this;
        }

        public List<String> getUpdToValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : updTo) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addUpdTo(final String value) {
            Preconditions.checkState(value != null);
            this.updTo.add(new CommentedValue(value));
            return this;
        }

        public Builder addUpdTo(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.updTo.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeUpdToAll() {
            this.updTo.clear();
            return this;
        }

        public Builder removeUpdToAt(int index) {
            Preconditions.checkState(index >= 0 && index < updTo.size(), "Invalid remove-at index for (multiple) attribute 'upd-to'");
            this.updTo.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddUpdTo(final String value) {
            Preconditions.checkState(value != null);
            return addUpdTo(value);
        }

        public Builder mandatoryAddUpdTo(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addUpdTo(value, comment);
        }

        // mnt-nfy
        public boolean hasMntNfy() {
            return this.mntNfy.size() > 0;
        }

        public List<CommentedValue> getMntNfy() {
            return this.mntNfy;
        }

        public Builder setMntNfy(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mntNfy = values;
            return this;
        }

        public List<String> getMntNfyValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mntNfy) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMntNfy(final String value) {
            Preconditions.checkState(value != null);
            this.mntNfy.add(new CommentedValue(value));
            return this;
        }

        public Builder addMntNfy(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntNfy.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMntNfyAll() {
            this.mntNfy.clear();
            return this;
        }

        public Builder removeMntNfyAt(int index) {
            Preconditions.checkState(index >= 0 && index < mntNfy.size(), "Invalid remove-at index for (multiple) attribute 'mnt-nfy'");
            this.mntNfy.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMntNfy(final String value) {
            Preconditions.checkState(value != null);
            return addMntNfy(value);
        }

        public Builder optionalAddMntNfy(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMntNfy(value, comment);
        }

        // auth
        public List<CommentedValue> getAuthRef() {
            return this.authRef;
        }

        public Builder setAuthRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.authRef = values;
            return this;
        }

        public List<String> getAuthRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : authRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addAuthRef(final String value) {
            Preconditions.checkState(value != null);
            this.authRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addAuthRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.authRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeAuthRefAll() {
            this.authRef.clear();
            return this;
        }

        public Builder removeAuthRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < authRef.size(), "Invalid remove-at index for (multiple) attribute 'auth'");
            this.authRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddAuthRef(final String value) {
            Preconditions.checkState(value != null);
            return addAuthRef(value);
        }

        public Builder mandatoryAddAuthRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addAuthRef(value, comment);
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

        // abuse-mailbox
        public boolean hasAbuseMailbox() {
            return this.abuseMailbox.size() > 0;
        }

        public List<CommentedValue> getAbuseMailbox() {
            return this.abuseMailbox;
        }

        public Builder setAbuseMailbox(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.abuseMailbox = values;
            return this;
        }

        public List<String> getAbuseMailboxValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : abuseMailbox) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addAbuseMailbox(final String value) {
            Preconditions.checkState(value != null);
            this.abuseMailbox.add(new CommentedValue(value));
            return this;
        }

        public Builder addAbuseMailbox(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.abuseMailbox.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeAbuseMailboxAll() {
            this.abuseMailbox.clear();
            return this;
        }

        public Builder removeAbuseMailboxAt(int index) {
            Preconditions.checkState(index >= 0 && index < abuseMailbox.size(), "Invalid remove-at index for (multiple) attribute 'abuse-mailbox'");
            this.abuseMailbox.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddAbuseMailbox(final String value) {
            Preconditions.checkState(value != null);
            return addAbuseMailbox(value);
        }

        public Builder optionalAddAbuseMailbox(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addAbuseMailbox(value, comment);
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

        // referral-by
        public CommentedValue getReferralByRef() {
            return this.referralByRef;
        }

        public Builder setReferralByRef(final String value) {
            Preconditions.checkState(value != null);
            this.referralByRef = new CommentedValue(value);
            return this;
        }

        public String getReferralByRefValue() {
            String value = null;
            if (this.referralByRef != null) {
                value = this.referralByRef.getValue();
            }
            return value;
        }

        public Builder setReferralByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.referralByRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeReferralByRef() {
            this.referralByRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetReferralByRef(final String value) {
            Preconditions.checkState(value != null);
            this.setReferralByRef(value);
            return this;
        }

        public Builder mandatorySetReferralByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setReferralByRef(value, comment);
            return this;
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


        public Mntner build() {
            Mntner obj = new Mntner(this);
            obj.validate();
            return obj;
        }
    }

    ;

};