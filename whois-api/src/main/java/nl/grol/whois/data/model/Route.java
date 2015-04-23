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

public class Route {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue route = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private CommentedValue originRef = null;
    private List<CommentedValue> pingable = new ArrayList<CommentedValue>();
    private List<CommentedValue> pingHdlRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> holes = new ArrayList<CommentedValue>();
    private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> inject = new ArrayList<CommentedValue>();
    private CommentedValue aggrMtd = null;
    private CommentedValue aggrBndry = null;
    private CommentedValue exportComps = null;
    private CommentedValue components = null;
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntLowerRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntRoutesRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> mntByRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public Route(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.route = builder.route;
        this.descr = builder.descr;
        this.originRef = builder.originRef;
        this.pingable = builder.pingable;
        this.pingHdlRef = builder.pingHdlRef;
        this.holes = builder.holes;
        this.orgRef = builder.orgRef;
        this.memberOfRef = builder.memberOfRef;
        this.inject = builder.inject;
        this.aggrMtd = builder.aggrMtd;
        this.aggrBndry = builder.aggrBndry;
        this.exportComps = builder.exportComps;
        this.components = builder.components;
        this.remarks = builder.remarks;
        this.notify = builder.notify;
        this.mntLowerRef = builder.mntLowerRef;
        this.mntRoutesRef = builder.mntRoutesRef;
        this.mntByRef = builder.mntByRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // route
    public CommentedValue getRoute() {
        return this.route;
    }

    public String getRouteValue() {
        String value = null;
        if (this.route != null) {
            value = this.route.getValue();
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

    // origin
    public CommentedValue getOriginRef() {
        return this.originRef;
    }

    public String getOriginRefValue() {
        String value = null;
        if (this.originRef != null) {
            value = this.originRef.getValue();
        }
        return value;
    }

    // pingable
    public boolean hasPingable() {
        return this.pingable.size() > 0;
    }

    public List<CommentedValue> getPingable() {
        return this.pingable;
    }

    public List<String> getPingableValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.pingable) {
            values.add(value.getValue());
        }
        return values;
    }

    // ping-hdl
    public boolean hasPingHdlRef() {
        return this.pingHdlRef.size() > 0;
    }

    public List<CommentedValue> getPingHdlRef() {
        return this.pingHdlRef;
    }

    public List<String> getPingHdlRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.pingHdlRef) {
            values.add(value.getValue());
        }
        return values;
    }

    // holes
    public boolean hasHoles() {
        return this.holes.size() > 0;
    }

    public List<CommentedValue> getHoles() {
        return this.holes;
    }

    public List<String> getHolesValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.holes) {
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

    // inject
    public boolean hasInject() {
        return this.inject.size() > 0;
    }

    public List<CommentedValue> getInject() {
        return this.inject;
    }

    public List<String> getInjectValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.inject) {
            values.add(value.getValue());
        }
        return values;
    }

    // aggr-mtd
    public boolean hasAggrMtd() {
        return this.aggrMtd != null;
    }

    public CommentedValue getAggrMtd() {
        return this.aggrMtd;
    }

    public String getAggrMtdValue() {
        String value = null;
        if (this.aggrMtd != null) {
            value = this.aggrMtd.getValue();
        }
        return value;
    }

    // aggr-bndry
    public boolean hasAggrBndry() {
        return this.aggrBndry != null;
    }

    public CommentedValue getAggrBndry() {
        return this.aggrBndry;
    }

    public String getAggrBndryValue() {
        String value = null;
        if (this.aggrBndry != null) {
            value = this.aggrBndry.getValue();
        }
        return value;
    }

    // export-comps
    public boolean hasExportComps() {
        return this.exportComps != null;
    }

    public CommentedValue getExportComps() {
        return this.exportComps;
    }

    public String getExportCompsValue() {
        String value = null;
        if (this.exportComps != null) {
            value = this.exportComps.getValue();
        }
        return value;
    }

    // components
    public boolean hasComponents() {
        return this.components != null;
    }

    public CommentedValue getComponents() {
        return this.components;
    }

    public String getComponentsValue() {
        String value = null;
        if (this.components != null) {
            value = this.components.getValue();
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
        Preconditions.checkState(route != null && route.getValue() != null, "Missing (single) mandatory attribute 'route'");
        Preconditions.checkState(descr != null && descr.size() > 0, "Missing (multiple) mandatory attribute 'descr'");
        Preconditions.checkState(originRef != null && originRef.getValue() != null, "Missing (single) mandatory attribute 'origin'");
        Preconditions.checkState(mntByRef != null && mntByRef.size() > 0, "Missing (multiple) mandatory attribute 'mnt-by'");
        Preconditions.checkState(changed != null && changed.size() > 0, "Missing (multiple) mandatory attribute 'changed'");
        Preconditions.checkState(source != null && source.getValue() != null, "Missing (single) mandatory attribute 'source'");

    }


    public WhoisResources toRequest() {
        WhoisResources whoisResources = new WhoisResources();

        WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(new Source(this.source.getValue()));
        whoisObject.setType("route");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (route != null) {
            if (!ATTR_NOT_IN_REQ.contains("route")) {
                attributes.add(new Attribute("route",
                        route.getValue(),
                        route.getComment(),
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
        if (originRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("origin")) {
                attributes.add(new Attribute("origin",
                        originRef.getValue(),
                        originRef.getComment(),
                        null, null));
            }
        }
        if (pingable != null) {
            if (!ATTR_NOT_IN_REQ.contains("pingable")) {
                for (CommentedValue value : pingable) {
                    attributes.add(new Attribute("pingable",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (pingHdlRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("ping-hdl")) {
                for (CommentedValue value : pingHdlRef) {
                    attributes.add(new Attribute("ping-hdl",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (holes != null) {
            if (!ATTR_NOT_IN_REQ.contains("holes")) {
                for (CommentedValue value : holes) {
                    attributes.add(new Attribute("holes",
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
        if (inject != null) {
            if (!ATTR_NOT_IN_REQ.contains("inject")) {
                for (CommentedValue value : inject) {
                    attributes.add(new Attribute("inject",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (aggrMtd != null) {
            if (!ATTR_NOT_IN_REQ.contains("aggr-mtd")) {
                attributes.add(new Attribute("aggr-mtd",
                        aggrMtd.getValue(),
                        aggrMtd.getComment(),
                        null, null));
            }
        }
        if (aggrBndry != null) {
            if (!ATTR_NOT_IN_REQ.contains("aggr-bndry")) {
                attributes.add(new Attribute("aggr-bndry",
                        aggrBndry.getValue(),
                        aggrBndry.getComment(),
                        null, null));
            }
        }
        if (exportComps != null) {
            if (!ATTR_NOT_IN_REQ.contains("export-comps")) {
                attributes.add(new Attribute("export-comps",
                        exportComps.getValue(),
                        exportComps.getComment(),
                        null, null));
            }
        }
        if (components != null) {
            if (!ATTR_NOT_IN_REQ.contains("components")) {
                attributes.add(new Attribute("components",
                        components.getValue(),
                        components.getComment(),
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
        appendRpslAttribute(sb, "route", this.route);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "origin", this.originRef);
        appendRpslAttribute(sb, "pingable", this.pingable);
        appendRpslAttribute(sb, "ping-hdl", this.pingHdlRef);
        appendRpslAttribute(sb, "holes", this.holes);
        appendRpslAttribute(sb, "org", this.orgRef);
        appendRpslAttribute(sb, "member-of", this.memberOfRef);
        appendRpslAttribute(sb, "inject", this.inject);
        appendRpslAttribute(sb, "aggr-mtd", this.aggrMtd);
        appendRpslAttribute(sb, "aggr-bndry", this.aggrBndry);
        appendRpslAttribute(sb, "export-comps", this.exportComps);
        appendRpslAttribute(sb, "components", this.components);
        appendRpslAttribute(sb, "remarks", this.remarks);
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

        private CommentedValue route = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private CommentedValue originRef = null;
        private List<CommentedValue> pingable = new ArrayList<CommentedValue>();
        private List<CommentedValue> pingHdlRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> holes = new ArrayList<CommentedValue>();
        private List<CommentedValue> orgRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> memberOfRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> inject = new ArrayList<CommentedValue>();
        private CommentedValue aggrMtd = null;
        private CommentedValue aggrBndry = null;
        private CommentedValue exportComps = null;
        private CommentedValue components = null;
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
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
                if (whoisObject.getType().equals("route")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("route")) {
                            builder.setRoute(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("origin")) {
                            builder.setOriginRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("pingable")) {
                            builder.addPingable(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("ping-hdl")) {
                            builder.addPingHdlRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("holes")) {
                            builder.addHoles(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("org")) {
                            builder.addOrgRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("member-of")) {
                            builder.addMemberOfRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("inject")) {
                            builder.addInject(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("aggr-mtd")) {
                            builder.setAggrMtd(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("aggr-bndry")) {
                            builder.setAggrBndry(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("export-comps")) {
                            builder.setExportComps(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("components")) {
                            builder.setComponents(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
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


        // route
        public CommentedValue getRoute() {
            return this.route;
        }

        public Builder setRoute(final String value) {
            Preconditions.checkState(value != null);
            this.route = new CommentedValue(value);
            return this;
        }

        public String getRouteValue() {
            String value = null;
            if (this.route != null) {
                value = this.route.getValue();
            }
            return value;
        }

        public Builder setRoute(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.route = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeRoute() {
            this.route = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetRoute(final String value) {
            Preconditions.checkState(value != null);
            this.setRoute(value);
            return this;
        }

        public Builder mandatorySetRoute(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setRoute(value, comment);
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

        // origin
        public CommentedValue getOriginRef() {
            return this.originRef;
        }

        public Builder setOriginRef(final String value) {
            Preconditions.checkState(value != null);
            this.originRef = new CommentedValue(value);
            return this;
        }

        public String getOriginRefValue() {
            String value = null;
            if (this.originRef != null) {
                value = this.originRef.getValue();
            }
            return value;
        }

        public Builder setOriginRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.originRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeOriginRef() {
            this.originRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetOriginRef(final String value) {
            Preconditions.checkState(value != null);
            this.setOriginRef(value);
            return this;
        }

        public Builder mandatorySetOriginRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setOriginRef(value, comment);
            return this;
        }

        // pingable
        public boolean hasPingable() {
            return this.pingable.size() > 0;
        }

        public List<CommentedValue> getPingable() {
            return this.pingable;
        }

        public Builder setPingable(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.pingable = values;
            return this;
        }

        public List<String> getPingableValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : pingable) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addPingable(final String value) {
            Preconditions.checkState(value != null);
            this.pingable.add(new CommentedValue(value));
            return this;
        }

        public Builder addPingable(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.pingable.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removePingableAll() {
            this.pingable.clear();
            return this;
        }

        public Builder removePingableAt(int index) {
            Preconditions.checkState(index >= 0 && index < pingable.size(), "Invalid remove-at index for (multiple) attribute 'pingable'");
            this.pingable.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddPingable(final String value) {
            Preconditions.checkState(value != null);
            return addPingable(value);
        }

        public Builder optionalAddPingable(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addPingable(value, comment);
        }

        // ping-hdl
        public boolean hasPingHdlRef() {
            return this.pingHdlRef.size() > 0;
        }

        public List<CommentedValue> getPingHdlRef() {
            return this.pingHdlRef;
        }

        public Builder setPingHdlRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.pingHdlRef = values;
            return this;
        }

        public List<String> getPingHdlRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : pingHdlRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addPingHdlRef(final String value) {
            Preconditions.checkState(value != null);
            this.pingHdlRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addPingHdlRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.pingHdlRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removePingHdlRefAll() {
            this.pingHdlRef.clear();
            return this;
        }

        public Builder removePingHdlRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < pingHdlRef.size(), "Invalid remove-at index for (multiple) attribute 'ping-hdl'");
            this.pingHdlRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddPingHdlRef(final String value) {
            Preconditions.checkState(value != null);
            return addPingHdlRef(value);
        }

        public Builder optionalAddPingHdlRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addPingHdlRef(value, comment);
        }

        // holes
        public boolean hasHoles() {
            return this.holes.size() > 0;
        }

        public List<CommentedValue> getHoles() {
            return this.holes;
        }

        public Builder setHoles(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.holes = values;
            return this;
        }

        public List<String> getHolesValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : holes) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addHoles(final String value) {
            Preconditions.checkState(value != null);
            this.holes.add(new CommentedValue(value));
            return this;
        }

        public Builder addHoles(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.holes.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeHolesAll() {
            this.holes.clear();
            return this;
        }

        public Builder removeHolesAt(int index) {
            Preconditions.checkState(index >= 0 && index < holes.size(), "Invalid remove-at index for (multiple) attribute 'holes'");
            this.holes.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddHoles(final String value) {
            Preconditions.checkState(value != null);
            return addHoles(value);
        }

        public Builder optionalAddHoles(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addHoles(value, comment);
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

        // inject
        public boolean hasInject() {
            return this.inject.size() > 0;
        }

        public List<CommentedValue> getInject() {
            return this.inject;
        }

        public Builder setInject(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.inject = values;
            return this;
        }

        public List<String> getInjectValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : inject) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addInject(final String value) {
            Preconditions.checkState(value != null);
            this.inject.add(new CommentedValue(value));
            return this;
        }

        public Builder addInject(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.inject.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeInjectAll() {
            this.inject.clear();
            return this;
        }

        public Builder removeInjectAt(int index) {
            Preconditions.checkState(index >= 0 && index < inject.size(), "Invalid remove-at index for (multiple) attribute 'inject'");
            this.inject.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddInject(final String value) {
            Preconditions.checkState(value != null);
            return addInject(value);
        }

        public Builder optionalAddInject(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addInject(value, comment);
        }

        // aggr-mtd
        public boolean hasAggrMtd() {
            return this.aggrMtd != null;
        }

        public CommentedValue getAggrMtd() {
            return this.aggrMtd;
        }

        public Builder setAggrMtd(final String value) {
            Preconditions.checkState(value != null);
            this.aggrMtd = new CommentedValue(value);
            return this;
        }

        public String getAggrMtdValue() {
            String value = null;
            if (this.aggrMtd != null) {
                value = this.aggrMtd.getValue();
            }
            return value;
        }

        public Builder setAggrMtd(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.aggrMtd = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeAggrMtd() {
            this.aggrMtd = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetAggrMtd(final String value) {
            Preconditions.checkState(value != null);
            this.setAggrMtd(value);
            return this;
        }

        public Builder optionalSetAggrMtd(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setAggrMtd(value, comment);
            return this;
        }

        // aggr-bndry
        public boolean hasAggrBndry() {
            return this.aggrBndry != null;
        }

        public CommentedValue getAggrBndry() {
            return this.aggrBndry;
        }

        public Builder setAggrBndry(final String value) {
            Preconditions.checkState(value != null);
            this.aggrBndry = new CommentedValue(value);
            return this;
        }

        public String getAggrBndryValue() {
            String value = null;
            if (this.aggrBndry != null) {
                value = this.aggrBndry.getValue();
            }
            return value;
        }

        public Builder setAggrBndry(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.aggrBndry = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeAggrBndry() {
            this.aggrBndry = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetAggrBndry(final String value) {
            Preconditions.checkState(value != null);
            this.setAggrBndry(value);
            return this;
        }

        public Builder optionalSetAggrBndry(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setAggrBndry(value, comment);
            return this;
        }

        // export-comps
        public boolean hasExportComps() {
            return this.exportComps != null;
        }

        public CommentedValue getExportComps() {
            return this.exportComps;
        }

        public Builder setExportComps(final String value) {
            Preconditions.checkState(value != null);
            this.exportComps = new CommentedValue(value);
            return this;
        }

        public String getExportCompsValue() {
            String value = null;
            if (this.exportComps != null) {
                value = this.exportComps.getValue();
            }
            return value;
        }

        public Builder setExportComps(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.exportComps = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeExportComps() {
            this.exportComps = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetExportComps(final String value) {
            Preconditions.checkState(value != null);
            this.setExportComps(value);
            return this;
        }

        public Builder optionalSetExportComps(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setExportComps(value, comment);
            return this;
        }

        // components
        public boolean hasComponents() {
            return this.components != null;
        }

        public CommentedValue getComponents() {
            return this.components;
        }

        public Builder setComponents(final String value) {
            Preconditions.checkState(value != null);
            this.components = new CommentedValue(value);
            return this;
        }

        public String getComponentsValue() {
            String value = null;
            if (this.components != null) {
                value = this.components.getValue();
            }
            return value;
        }

        public Builder setComponents(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.components = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeComponents() {
            this.components = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalSetComponents(final String value) {
            Preconditions.checkState(value != null);
            this.setComponents(value);
            return this;
        }

        public Builder optionalSetComponents(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setComponents(value, comment);
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


        public Route build() {
            Route obj = new Route(this);
            obj.validate();
            return obj;
        }
    }

    ;

};