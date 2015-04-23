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

public class AutNum {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue autNum = null;
    private CommentedValue asName = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> importVia = new ArrayList<CommentedValue>();
    private List<CommentedValue> import_ = new ArrayList<CommentedValue>();
    private List<CommentedValue> mpImport = new ArrayList<CommentedValue>();
    private List<CommentedValue> exportVia = new ArrayList<CommentedValue>();
    private List<CommentedValue> export = new ArrayList<CommentedValue>();
    private List<CommentedValue> mpExport = new ArrayList<CommentedValue>();
    private List<CommentedValue> default_ = new ArrayList<CommentedValue>();
    private List<CommentedValue> mpDefault = new ArrayList<CommentedValue>();
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private CommentedValue orgRef = null;
    private CommentedValue sponsoringOrgRef = null;
    private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
    private CommentedValue status = null;
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntLowerRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntRoutesRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public AutNum(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.autNum = builder.autNum;
        this.asName = builder.asName;
        this.descr = builder.descr;
        this.memberOfRef = builder.memberOfRef;
        this.importVia = builder.importVia;
        this.import_ = builder.import_;
        this.mpImport = builder.mpImport;
        this.exportVia = builder.exportVia;
        this.export = builder.export;
        this.mpExport = builder.mpExport;
        this.default_ = builder.default_;
        this.mpDefault = builder.mpDefault;
        this.remarks = builder.remarks;
        this.orgRef = builder.orgRef;
        this.sponsoringOrgRef = builder.sponsoringOrgRef;
        this.adminCRef = builder.adminCRef;
        this.techCRef = builder.techCRef;
        this.status = builder.status;
        this.notify = builder.notify;
        this.mntLowerRef = builder.mntLowerRef;
        this.mntRoutesRef = builder.mntRoutesRef;
        this.mntByRef = builder.mntByRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // aut-num
    public CommentedValue getAutNum() {
        return this.autNum;
    }

    public String getAutNumValue() {
        String value = null;
        if (this.autNum != null) {
            value = this.autNum.getValue();
        }
        return value;
    }

    // as-name
    public CommentedValue getAsName() {
        return this.asName;
    }

    public String getAsNameValue() {
        String value = null;
        if (this.asName != null) {
            value = this.asName.getValue();
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

    // member-of
    public boolean hasMemberOfRef() {
        return this.memberOfRef.size() > 0;
    }

    public List<CommentedValue> getMemberOfRef() {
        return this.memberOfRef;
    }

    public List<String> getMemberOfRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.memberOfRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // import-via
    public boolean hasImportVia() {
        return this.importVia.size() > 0;
    }

    public List<CommentedValue> getImportVia() {
        return this.importVia;
    }

    public List<String> getImportViaValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.importVia) {
            values.add(value.getValue());
        }
        return values;
    }

    // import
    public boolean hasImport() {
        return this.import_.size() > 0;
    }

    public List<CommentedValue> getImport() {
        return this.import_;
    }

    public List<String> getImportValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.import_) {
            values.add(value.getValue());
        }
        return values;
    }

    // mp-import
    public boolean hasMpImport() {
        return this.mpImport.size() > 0;
    }

    public List<CommentedValue> getMpImport() {
        return this.mpImport;
    }

    public List<String> getMpImportValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mpImport) {
            values.add(value.getValue());
        }
        return values;
    }

    // export-via
    public boolean hasExportVia() {
        return this.exportVia.size() > 0;
    }

    public List<CommentedValue> getExportVia() {
        return this.exportVia;
    }

    public List<String> getExportViaValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.exportVia) {
            values.add(value.getValue());
        }
        return values;
    }

    // export
    public boolean hasExport() {
        return this.export.size() > 0;
    }

    public List<CommentedValue> getExport() {
        return this.export;
    }

    public List<String> getExportValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.export) {
            values.add(value.getValue());
        }
        return values;
    }

    // mp-export
    public boolean hasMpExport() {
        return this.mpExport.size() > 0;
    }

    public List<CommentedValue> getMpExport() {
        return this.mpExport;
    }

    public List<String> getMpExportValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mpExport) {
            values.add(value.getValue());
        }
        return values;
    }

    // default
    public boolean hasDefault() {
        return this.default_.size() > 0;
    }

    public List<CommentedValue> getDefault() {
        return this.default_;
    }

    public List<String> getDefaultValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.default_) {
            values.add(value.getValue());
        }
        return values;
    }

    // mp-default
    public boolean hasMpDefault() {
        return this.mpDefault.size() > 0;
    }

    public List<CommentedValue> getMpDefault() {
        return this.mpDefault;
    }

    public List<String> getMpDefaultValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mpDefault) {
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
    public boolean hasStatus() {
        return this.status != null;
    }

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
        Preconditions.checkState(autNum != null && autNum.getValue() != null, "Missing (single) mandatory attribute 'aut-num'");
        Preconditions.checkState(asName != null && asName.getValue() != null, "Missing (single) mandatory attribute 'as-name'");
        Preconditions.checkState(descr != null && descr.size() > 0, "Missing (multiple) mandatory attribute 'descr'");
        Preconditions.checkState(adminCRef != null && adminCRef.size() > 0, "Missing (multiple) mandatory attribute 'admin-c'");
        Preconditions.checkState(techCRef != null && techCRef.size() > 0, "Missing (multiple) mandatory attribute 'tech-c'");
        Preconditions.checkState(mntByRef != null && mntByRef.size() > 0, "Missing (multiple) mandatory attribute 'mnt-by'");
        Preconditions.checkState(changed != null && changed.size() > 0, "Missing (multiple) mandatory attribute 'changed'");
        Preconditions.checkState(source != null && source.getValue() != null, "Missing (single) mandatory attribute 'source'");

    }


    public WhoisResources toRequest() {
        WhoisResources whoisResources = new WhoisResources();

        WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(new Source(this.source.getValue()));
        whoisObject.setType("aut-num");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (autNum != null) {
            if (!ATTR_NOT_IN_REQ.contains("aut-num")) {
                attributes.add(new Attribute("aut-num",
                        autNum.getValue(),
                        autNum.getComment(),
                        null, null));
            }
        }
        if (asName != null) {
            if (!ATTR_NOT_IN_REQ.contains("as-name")) {
                attributes.add(new Attribute("as-name",
                        asName.getValue(),
                        asName.getComment(),
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
        if (memberOfRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("member-of")) {
                for (CommentedValue value : memberOfRef) {
                    attributes.add(new Attribute("member-of",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (importVia != null) {
            if (!ATTR_NOT_IN_REQ.contains("import-via")) {
                for (CommentedValue value : importVia) {
                    attributes.add(new Attribute("import-via",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (import_ != null) {
            if (!ATTR_NOT_IN_REQ.contains("import")) {
                for (CommentedValue value : import_) {
                    attributes.add(new Attribute("import",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mpImport != null) {
            if (!ATTR_NOT_IN_REQ.contains("mp-import")) {
                for (CommentedValue value : mpImport) {
                    attributes.add(new Attribute("mp-import",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (exportVia != null) {
            if (!ATTR_NOT_IN_REQ.contains("export-via")) {
                for (CommentedValue value : exportVia) {
                    attributes.add(new Attribute("export-via",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (export != null) {
            if (!ATTR_NOT_IN_REQ.contains("export")) {
                for (CommentedValue value : export) {
                    attributes.add(new Attribute("export",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mpExport != null) {
            if (!ATTR_NOT_IN_REQ.contains("mp-export")) {
                for (CommentedValue value : mpExport) {
                    attributes.add(new Attribute("mp-export",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (default_ != null) {
            if (!ATTR_NOT_IN_REQ.contains("default")) {
                for (CommentedValue value : default_) {
                    attributes.add(new Attribute("default",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mpDefault != null) {
            if (!ATTR_NOT_IN_REQ.contains("mp-default")) {
                for (CommentedValue value : mpDefault) {
                    attributes.add(new Attribute("mp-default",
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
        appendRpslAttribute(sb, "aut-num", this.autNum);
        appendRpslAttribute(sb, "as-name", this.asName);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "member-of", this.memberOfRef);
        appendRpslAttribute(sb, "import-via", this.importVia);
        appendRpslAttribute(sb, "import", this.import_);
        appendRpslAttribute(sb, "mp-import", this.mpImport);
        appendRpslAttribute(sb, "export-via", this.exportVia);
        appendRpslAttribute(sb, "export", this.export);
        appendRpslAttribute(sb, "mp-export", this.mpExport);
        appendRpslAttribute(sb, "default", this.default_);
        appendRpslAttribute(sb, "mp-default", this.mpDefault);
        appendRpslAttribute(sb, "remarks", this.remarks);
        appendRpslAttribute(sb, "org", this.orgRef);
        appendRpslAttribute(sb, "sponsoring-org", this.sponsoringOrgRef);
        appendRpslAttribute(sb, "admin-c", this.adminCRef);
        appendRpslAttribute(sb, "tech-c", this.techCRef);
        appendRpslAttribute(sb, "status", this.status);
        appendRpslAttribute(sb, "notify", this.notify);
        appendRpslAttribute(sb, "mnt-lower", this.mntLowerRef);
        appendRpslAttribute(sb, "mnt-routes", this.mntRoutesRef);
        appendRpslAttribute(sb, "mnt-by", this.mntByRef);
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

        private CommentedValue autNum = null;
        private CommentedValue asName = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> importVia = new ArrayList<CommentedValue>();
        private List<CommentedValue> import_ = new ArrayList<CommentedValue>();
        private List<CommentedValue> mpImport = new ArrayList<CommentedValue>();
        private List<CommentedValue> exportVia = new ArrayList<CommentedValue>();
        private List<CommentedValue> export = new ArrayList<CommentedValue>();
        private List<CommentedValue> mpExport = new ArrayList<CommentedValue>();
        private List<CommentedValue> default_ = new ArrayList<CommentedValue>();
        private List<CommentedValue> mpDefault = new ArrayList<CommentedValue>();
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
        private CommentedValue orgRef = null;
        private CommentedValue sponsoringOrgRef = null;
        private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
        private CommentedValue status = null;
        private List<CommentedValue> notify = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntLowerRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntRoutesRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> changed = new ArrayList<CommentedValue>();
        private CommentedValue created = null;
        private CommentedValue lastModified = null;
        private CommentedValue source = null;


        public static Builder fromResponse(WhoisResources whoisResources) {
            Builder builder = new Builder();
            for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
                if (whoisObject.getType().equals("aut-num")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("aut-num")) {
                            builder.setAutNum(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("as-name")) {
                            builder.setAsName(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("member-of")) {
                            builder.addMemberOfRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("import-via")) {
                            builder.addImportVia(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("import")) {
                            builder.addImport(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mp-import")) {
                            builder.addMpImport(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("export-via")) {
                            builder.addExportVia(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("export")) {
                            builder.addExport(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mp-export")) {
                            builder.addMpExport(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("default")) {
                            builder.addDefault(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mp-default")) {
                            builder.addMpDefault(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
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
                        if (attr.getName().equals("notify")) {
                            builder.addNotify(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-lower")) {
                            builder.addMntLowerRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-routes")) {
                            builder.addMntRoutesRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-by")) {
                            builder.addMntByRef(attr.getValue(), attr.getComment());
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


        // aut-num
        public CommentedValue getAutNum() {
            return this.autNum;
        }

        public Builder setAutNum(final String value) {
            Preconditions.checkState(value != null);
            this.autNum = new CommentedValue(value);
            return this;
        }

        public String getAutNumValue() {
            String value = null;
            if (this.autNum != null) {
                value = this.autNum.getValue();
            }
            return value;
        }

        public Builder setAutNum(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.autNum = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeAutNum() {
            this.autNum = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetAutNum(final String value) {
            Preconditions.checkState(value != null);
            this.setAutNum(value);
            return this;
        }

        public Builder mandatorySetAutNum(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setAutNum(value, comment);
            return this;
        }

        // as-name
        public CommentedValue getAsName() {
            return this.asName;
        }

        public Builder setAsName(final String value) {
            Preconditions.checkState(value != null);
            this.asName = new CommentedValue(value);
            return this;
        }

        public String getAsNameValue() {
            String value = null;
            if (this.asName != null) {
                value = this.asName.getValue();
            }
            return value;
        }

        public Builder setAsName(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.asName = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeAsName() {
            this.asName = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetAsName(final String value) {
            Preconditions.checkState(value != null);
            this.setAsName(value);
            return this;
        }

        public Builder mandatorySetAsName(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setAsName(value, comment);
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

        // member-of
        public boolean hasMemberOfRef() {
            return this.memberOfRef.size() > 0;
        }

        public List<CommentedValue> getMemberOfRef() {
            return this.memberOfRef;
        }

        public Builder setMemberOfRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.memberOfRef = values;
            return this;
        }

        public List<String> getMemberOfRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : memberOfRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMemberOfRef(final String value) {
            Preconditions.checkState(value != null);
            this.memberOfRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addMemberOfRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.memberOfRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMemberOfRefAll() {
            this.memberOfRef.clear();
            return this;
        }

        public Builder removeMemberOfRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < memberOfRef.size(), "Invalid remove-at index for (multiple) attribute 'member-of'");
            this.memberOfRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMemberOfRef(final String value) {
            Preconditions.checkState(value != null);
            return addMemberOfRef(value);
        }

        public Builder optionalAddMemberOfRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMemberOfRef(value, comment);
        }

        // import-via
        public boolean hasImportVia() {
            return this.importVia.size() > 0;
        }

        public List<CommentedValue> getImportVia() {
            return this.importVia;
        }

        public Builder setImportVia(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.importVia = values;
            return this;
        }

        public List<String> getImportViaValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : importVia) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addImportVia(final String value) {
            Preconditions.checkState(value != null);
            this.importVia.add(new CommentedValue(value));
            return this;
        }

        public Builder addImportVia(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.importVia.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeImportViaAll() {
            this.importVia.clear();
            return this;
        }

        public Builder removeImportViaAt(int index) {
            Preconditions.checkState(index >= 0 && index < importVia.size(), "Invalid remove-at index for (multiple) attribute 'import-via'");
            this.importVia.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddImportVia(final String value) {
            Preconditions.checkState(value != null);
            return addImportVia(value);
        }

        public Builder optionalAddImportVia(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addImportVia(value, comment);
        }

        // import
        public boolean hasImport() {
            return this.import_.size() > 0;
        }

        public List<CommentedValue> getImport() {
            return this.import_;
        }

        public Builder setImport(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.import_ = values;
            return this;
        }

        public List<String> getImportValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : import_) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addImport(final String value) {
            Preconditions.checkState(value != null);
            this.import_.add(new CommentedValue(value));
            return this;
        }

        public Builder addImport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.import_.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeImportAll() {
            this.import_.clear();
            return this;
        }

        public Builder removeImportAt(int index) {
            Preconditions.checkState(index >= 0 && index < import_.size(), "Invalid remove-at index for (multiple) attribute 'import'");
            this.import_.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddImport(final String value) {
            Preconditions.checkState(value != null);
            return addImport(value);
        }

        public Builder optionalAddImport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addImport(value, comment);
        }

        // mp-import
        public boolean hasMpImport() {
            return this.mpImport.size() > 0;
        }

        public List<CommentedValue> getMpImport() {
            return this.mpImport;
        }

        public Builder setMpImport(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mpImport = values;
            return this;
        }

        public List<String> getMpImportValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mpImport) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMpImport(final String value) {
            Preconditions.checkState(value != null);
            this.mpImport.add(new CommentedValue(value));
            return this;
        }

        public Builder addMpImport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mpImport.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMpImportAll() {
            this.mpImport.clear();
            return this;
        }

        public Builder removeMpImportAt(int index) {
            Preconditions.checkState(index >= 0 && index < mpImport.size(), "Invalid remove-at index for (multiple) attribute 'mp-import'");
            this.mpImport.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMpImport(final String value) {
            Preconditions.checkState(value != null);
            return addMpImport(value);
        }

        public Builder optionalAddMpImport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMpImport(value, comment);
        }

        // export-via
        public boolean hasExportVia() {
            return this.exportVia.size() > 0;
        }

        public List<CommentedValue> getExportVia() {
            return this.exportVia;
        }

        public Builder setExportVia(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.exportVia = values;
            return this;
        }

        public List<String> getExportViaValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : exportVia) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addExportVia(final String value) {
            Preconditions.checkState(value != null);
            this.exportVia.add(new CommentedValue(value));
            return this;
        }

        public Builder addExportVia(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.exportVia.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeExportViaAll() {
            this.exportVia.clear();
            return this;
        }

        public Builder removeExportViaAt(int index) {
            Preconditions.checkState(index >= 0 && index < exportVia.size(), "Invalid remove-at index for (multiple) attribute 'export-via'");
            this.exportVia.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddExportVia(final String value) {
            Preconditions.checkState(value != null);
            return addExportVia(value);
        }

        public Builder optionalAddExportVia(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addExportVia(value, comment);
        }

        // export
        public boolean hasExport() {
            return this.export.size() > 0;
        }

        public List<CommentedValue> getExport() {
            return this.export;
        }

        public Builder setExport(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.export = values;
            return this;
        }

        public List<String> getExportValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : export) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addExport(final String value) {
            Preconditions.checkState(value != null);
            this.export.add(new CommentedValue(value));
            return this;
        }

        public Builder addExport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.export.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeExportAll() {
            this.export.clear();
            return this;
        }

        public Builder removeExportAt(int index) {
            Preconditions.checkState(index >= 0 && index < export.size(), "Invalid remove-at index for (multiple) attribute 'export'");
            this.export.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddExport(final String value) {
            Preconditions.checkState(value != null);
            return addExport(value);
        }

        public Builder optionalAddExport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addExport(value, comment);
        }

        // mp-export
        public boolean hasMpExport() {
            return this.mpExport.size() > 0;
        }

        public List<CommentedValue> getMpExport() {
            return this.mpExport;
        }

        public Builder setMpExport(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mpExport = values;
            return this;
        }

        public List<String> getMpExportValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mpExport) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMpExport(final String value) {
            Preconditions.checkState(value != null);
            this.mpExport.add(new CommentedValue(value));
            return this;
        }

        public Builder addMpExport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mpExport.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMpExportAll() {
            this.mpExport.clear();
            return this;
        }

        public Builder removeMpExportAt(int index) {
            Preconditions.checkState(index >= 0 && index < mpExport.size(), "Invalid remove-at index for (multiple) attribute 'mp-export'");
            this.mpExport.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMpExport(final String value) {
            Preconditions.checkState(value != null);
            return addMpExport(value);
        }

        public Builder optionalAddMpExport(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMpExport(value, comment);
        }

        // default
        public boolean hasDefault() {
            return this.default_.size() > 0;
        }

        public List<CommentedValue> getDefault() {
            return this.default_;
        }

        public Builder setDefault(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.default_ = values;
            return this;
        }

        public List<String> getDefaultValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : default_) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addDefault(final String value) {
            Preconditions.checkState(value != null);
            this.default_.add(new CommentedValue(value));
            return this;
        }

        public Builder addDefault(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.default_.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeDefaultAll() {
            this.default_.clear();
            return this;
        }

        public Builder removeDefaultAt(int index) {
            Preconditions.checkState(index >= 0 && index < default_.size(), "Invalid remove-at index for (multiple) attribute 'default'");
            this.default_.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddDefault(final String value) {
            Preconditions.checkState(value != null);
            return addDefault(value);
        }

        public Builder optionalAddDefault(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addDefault(value, comment);
        }

        // mp-default
        public boolean hasMpDefault() {
            return this.mpDefault.size() > 0;
        }

        public List<CommentedValue> getMpDefault() {
            return this.mpDefault;
        }

        public Builder setMpDefault(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mpDefault = values;
            return this;
        }

        public List<String> getMpDefaultValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mpDefault) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMpDefault(final String value) {
            Preconditions.checkState(value != null);
            this.mpDefault.add(new CommentedValue(value));
            return this;
        }

        public Builder addMpDefault(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mpDefault.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMpDefaultAll() {
            this.mpDefault.clear();
            return this;
        }

        public Builder removeMpDefaultAt(int index) {
            Preconditions.checkState(index >= 0 && index < mpDefault.size(), "Invalid remove-at index for (multiple) attribute 'mp-default'");
            this.mpDefault.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMpDefault(final String value) {
            Preconditions.checkState(value != null);
            return addMpDefault(value);
        }

        public Builder optionalAddMpDefault(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMpDefault(value, comment);
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
        public boolean hasStatus() {
            return this.status != null;
        }

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
        public Builder optionalSetStatus(final String value) {
            Preconditions.checkState(value != null);
            this.setStatus(value);
            return this;
        }

        public Builder optionalSetStatus(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setStatus(value, comment);
            return this;
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


        public AutNum build() {
            AutNum obj = new AutNum(this);
            obj.validate();
            return obj;
        }
    }

    ;

};