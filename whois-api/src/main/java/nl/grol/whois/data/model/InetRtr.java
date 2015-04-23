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

public class InetRtr {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue inetRtr = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private List<CommentedValue> alias = new ArrayList<CommentedValue>();
    private CommentedValue localAs = null;
    private List<CommentedValue> ifaddr = new ArrayList<CommentedValue>();
    private List<CommentedValue> interface_ = new ArrayList<CommentedValue>();
    private List<CommentedValue> peer = new ArrayList<CommentedValue>();
    private List<CommentedValue> mpPeer = new ArrayList<CommentedValue>();
    private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public InetRtr(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.inetRtr = builder.inetRtr;
        this.descr = builder.descr;
        this.alias = builder.alias;
        this.localAs = builder.localAs;
        this.ifaddr = builder.ifaddr;
        this.interface_ = builder.interface_;
        this.peer = builder.peer;
        this.mpPeer = builder.mpPeer;
        this.memberOfRef = builder.memberOfRef;
        this.remarks = builder.remarks;
        this.orgRef = builder.orgRef;
        this.adminCRef = builder.adminCRef;
        this.techCRef = builder.techCRef;
        this.notify = builder.notify;
        this.mntByRef = builder.mntByRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // inet-rtr
    public CommentedValue getInetRtr() {
        return this.inetRtr;
    }

    public String getInetRtrValue() {
        String value = null;
        if (this.inetRtr != null) {
            value = this.inetRtr.getValue();
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

    // alias
    public boolean hasAlias() {
        return this.alias.size() > 0;
    }

    public List<CommentedValue> getAlias() {
        return this.alias;
    }

    public List<String> getAliasValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.alias) {
            values.add(value.getValue());
        }
        return values;
    }

    // local-as
    public CommentedValue getLocalAs() {
        return this.localAs;
    }

    public String getLocalAsValue() {
        String value = null;
        if (this.localAs != null) {
            value = this.localAs.getValue();
        }
        return value;
    }

    // ifaddr
    public List<CommentedValue> getIfaddr() {
        return this.ifaddr;
    }

    public List<String> getIfaddrValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.ifaddr) {
            values.add(value.getValue());
        }
        return values;
    }

    // interface
    public boolean hasInterface() {
        return this.interface_.size() > 0;
    }

    public List<CommentedValue> getInterface() {
        return this.interface_;
    }

    public List<String> getInterfaceValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.interface_) {
            values.add(value.getValue());
        }
        return values;
    }

    // peer
    public boolean hasPeer() {
        return this.peer.size() > 0;
    }

    public List<CommentedValue> getPeer() {
        return this.peer;
    }

    public List<String> getPeerValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.peer) {
            values.add(value.getValue());
        }
        return values;
    }

    // mp-peer
    public boolean hasMpPeer() {
        return this.mpPeer.size() > 0;
    }

    public List<CommentedValue> getMpPeer() {
        return this.mpPeer;
    }

    public List<String> getMpPeerValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.mpPeer) {
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
        Preconditions.checkState(inetRtr != null && inetRtr.getValue() != null, "Missing (single) mandatory attribute 'inet-rtr'");
        Preconditions.checkState(descr != null && descr.size() > 0, "Missing (multiple) mandatory attribute 'descr'");
        Preconditions.checkState(localAs != null && localAs.getValue() != null, "Missing (single) mandatory attribute 'local-as'");
        Preconditions.checkState(ifaddr != null && ifaddr.size() > 0, "Missing (multiple) mandatory attribute 'ifaddr'");
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
        whoisObject.setType("inet-rtr");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (inetRtr != null) {
            if (!ATTR_NOT_IN_REQ.contains("inet-rtr")) {
                attributes.add(new Attribute("inet-rtr",
                        inetRtr.getValue(),
                        inetRtr.getComment(),
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
        if (alias != null) {
            if (!ATTR_NOT_IN_REQ.contains("alias")) {
                for (CommentedValue value : alias) {
                    attributes.add(new Attribute("alias",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (localAs != null) {
            if (!ATTR_NOT_IN_REQ.contains("local-as")) {
                attributes.add(new Attribute("local-as",
                        localAs.getValue(),
                        localAs.getComment(),
                        null, null));
            }
        }
        if (ifaddr != null) {
            if (!ATTR_NOT_IN_REQ.contains("ifaddr")) {
                for (CommentedValue value : ifaddr) {
                    attributes.add(new Attribute("ifaddr",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (interface_ != null) {
            if (!ATTR_NOT_IN_REQ.contains("interface")) {
                for (CommentedValue value : interface_) {
                    attributes.add(new Attribute("interface",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (peer != null) {
            if (!ATTR_NOT_IN_REQ.contains("peer")) {
                for (CommentedValue value : peer) {
                    attributes.add(new Attribute("peer",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (mpPeer != null) {
            if (!ATTR_NOT_IN_REQ.contains("mp-peer")) {
                for (CommentedValue value : mpPeer) {
                    attributes.add(new Attribute("mp-peer",
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
        appendRpslAttribute(sb, "inet-rtr", this.inetRtr);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "alias", this.alias);
        appendRpslAttribute(sb, "local-as", this.localAs);
        appendRpslAttribute(sb, "ifaddr", this.ifaddr);
        appendRpslAttribute(sb, "interface", this.interface_);
        appendRpslAttribute(sb, "peer", this.peer);
        appendRpslAttribute(sb, "mp-peer", this.mpPeer);
        appendRpslAttribute(sb, "member-of", this.memberOfRef);
        appendRpslAttribute(sb, "remarks", this.remarks);
        appendRpslAttribute(sb, "org", this.orgRef);
        appendRpslAttribute(sb, "admin-c", this.adminCRef);
        appendRpslAttribute(sb, "tech-c", this.techCRef);
        appendRpslAttribute(sb, "notify", this.notify);
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

        private CommentedValue inetRtr = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private List<CommentedValue> alias = new ArrayList<CommentedValue>();
        private CommentedValue localAs = null;
        private List<CommentedValue> ifaddr = new ArrayList<CommentedValue>();
        private List<CommentedValue> interface_ = new ArrayList<CommentedValue>();
        private List<CommentedValue> peer = new ArrayList<CommentedValue>();
        private List<CommentedValue> mpPeer = new ArrayList<CommentedValue>();
        private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
        private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> adminCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> techCRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> notify = new ArrayList<CommentedValue>();
        private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> changed = new ArrayList<CommentedValue>();
        private CommentedValue created = null;
        private CommentedValue lastModified = null;
        private CommentedValue source = null;


        public static Builder fromResponse(WhoisResources whoisResources) {
            Builder builder = new Builder();
            for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
                if (whoisObject.getType().equals("inet-rtr")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("inet-rtr")) {
                            builder.setInetRtr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("alias")) {
                            builder.addAlias(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("local-as")) {
                            builder.setLocalAs(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("ifaddr")) {
                            builder.addIfaddr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("interface")) {
                            builder.addInterface(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("peer")) {
                            builder.addPeer(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mp-peer")) {
                            builder.addMpPeer(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("member-of")) {
                            builder.addMemberOfRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
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
                        if (attr.getName().equals("notify")) {
                            builder.addNotify(attr.getValue(), attr.getComment());
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


        // inet-rtr
        public CommentedValue getInetRtr() {
            return this.inetRtr;
        }

        public Builder setInetRtr(final String value) {
            Preconditions.checkState(value != null);
            this.inetRtr = new CommentedValue(value);
            return this;
        }

        public String getInetRtrValue() {
            String value = null;
            if (this.inetRtr != null) {
                value = this.inetRtr.getValue();
            }
            return value;
        }

        public Builder setInetRtr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.inetRtr = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeInetRtr() {
            this.inetRtr = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetInetRtr(final String value) {
            Preconditions.checkState(value != null);
            this.setInetRtr(value);
            return this;
        }

        public Builder mandatorySetInetRtr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setInetRtr(value, comment);
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

        // alias
        public boolean hasAlias() {
            return this.alias.size() > 0;
        }

        public List<CommentedValue> getAlias() {
            return this.alias;
        }

        public Builder setAlias(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.alias = values;
            return this;
        }

        public List<String> getAliasValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : alias) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addAlias(final String value) {
            Preconditions.checkState(value != null);
            this.alias.add(new CommentedValue(value));
            return this;
        }

        public Builder addAlias(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.alias.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeAliasAll() {
            this.alias.clear();
            return this;
        }

        public Builder removeAliasAt(int index) {
            Preconditions.checkState(index >= 0 && index < alias.size(), "Invalid remove-at index for (multiple) attribute 'alias'");
            this.alias.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddAlias(final String value) {
            Preconditions.checkState(value != null);
            return addAlias(value);
        }

        public Builder optionalAddAlias(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addAlias(value, comment);
        }

        // local-as
        public CommentedValue getLocalAs() {
            return this.localAs;
        }

        public Builder setLocalAs(final String value) {
            Preconditions.checkState(value != null);
            this.localAs = new CommentedValue(value);
            return this;
        }

        public String getLocalAsValue() {
            String value = null;
            if (this.localAs != null) {
                value = this.localAs.getValue();
            }
            return value;
        }

        public Builder setLocalAs(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.localAs = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeLocalAs() {
            this.localAs = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetLocalAs(final String value) {
            Preconditions.checkState(value != null);
            this.setLocalAs(value);
            return this;
        }

        public Builder mandatorySetLocalAs(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setLocalAs(value, comment);
            return this;
        }

        // ifaddr
        public List<CommentedValue> getIfaddr() {
            return this.ifaddr;
        }

        public Builder setIfaddr(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.ifaddr = values;
            return this;
        }

        public List<String> getIfaddrValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : ifaddr) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addIfaddr(final String value) {
            Preconditions.checkState(value != null);
            this.ifaddr.add(new CommentedValue(value));
            return this;
        }

        public Builder addIfaddr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.ifaddr.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeIfaddrAll() {
            this.ifaddr.clear();
            return this;
        }

        public Builder removeIfaddrAt(int index) {
            Preconditions.checkState(index >= 0 && index < ifaddr.size(), "Invalid remove-at index for (multiple) attribute 'ifaddr'");
            this.ifaddr.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddIfaddr(final String value) {
            Preconditions.checkState(value != null);
            return addIfaddr(value);
        }

        public Builder mandatoryAddIfaddr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addIfaddr(value, comment);
        }

        // interface
        public boolean hasInterface() {
            return this.interface_.size() > 0;
        }

        public List<CommentedValue> getInterface() {
            return this.interface_;
        }

        public Builder setInterface(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.interface_ = values;
            return this;
        }

        public List<String> getInterfaceValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : interface_) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addInterface(final String value) {
            Preconditions.checkState(value != null);
            this.interface_.add(new CommentedValue(value));
            return this;
        }

        public Builder addInterface(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.interface_.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeInterfaceAll() {
            this.interface_.clear();
            return this;
        }

        public Builder removeInterfaceAt(int index) {
            Preconditions.checkState(index >= 0 && index < interface_.size(), "Invalid remove-at index for (multiple) attribute 'interface'");
            this.interface_.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddInterface(final String value) {
            Preconditions.checkState(value != null);
            return addInterface(value);
        }

        public Builder optionalAddInterface(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addInterface(value, comment);
        }

        // peer
        public boolean hasPeer() {
            return this.peer.size() > 0;
        }

        public List<CommentedValue> getPeer() {
            return this.peer;
        }

        public Builder setPeer(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.peer = values;
            return this;
        }

        public List<String> getPeerValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : peer) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addPeer(final String value) {
            Preconditions.checkState(value != null);
            this.peer.add(new CommentedValue(value));
            return this;
        }

        public Builder addPeer(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.peer.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removePeerAll() {
            this.peer.clear();
            return this;
        }

        public Builder removePeerAt(int index) {
            Preconditions.checkState(index >= 0 && index < peer.size(), "Invalid remove-at index for (multiple) attribute 'peer'");
            this.peer.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddPeer(final String value) {
            Preconditions.checkState(value != null);
            return addPeer(value);
        }

        public Builder optionalAddPeer(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addPeer(value, comment);
        }

        // mp-peer
        public boolean hasMpPeer() {
            return this.mpPeer.size() > 0;
        }

        public List<CommentedValue> getMpPeer() {
            return this.mpPeer;
        }

        public Builder setMpPeer(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.mpPeer = values;
            return this;
        }

        public List<String> getMpPeerValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : mpPeer) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addMpPeer(final String value) {
            Preconditions.checkState(value != null);
            this.mpPeer.add(new CommentedValue(value));
            return this;
        }

        public Builder addMpPeer(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mpPeer.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeMpPeerAll() {
            this.mpPeer.clear();
            return this;
        }

        public Builder removeMpPeerAt(int index) {
            Preconditions.checkState(index >= 0 && index < mpPeer.size(), "Invalid remove-at index for (multiple) attribute 'mp-peer'");
            this.mpPeer.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddMpPeer(final String value) {
            Preconditions.checkState(value != null);
            return addMpPeer(value);
        }

        public Builder optionalAddMpPeer(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addMpPeer(value, comment);
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


        public InetRtr build() {
            InetRtr obj = new InetRtr(this);
            obj.validate();
            return obj;
        }
    }

    ;

};