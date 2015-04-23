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

public class Poem {
    private static final List<String> ATTR_NOT_IN_REQ = Arrays.asList("auth", "created", "last-modified");

    private CommentedValue poem = null;
    private List<CommentedValue> descr = new ArrayList<CommentedValue>();
    private CommentedValue formRef = null;
    private List<CommentedValue> text = new ArrayList<CommentedValue>();
    private List<CommentedValue> authorRef = new ArrayList<CommentedValue>();
    private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
    private List<CommentedValue> notify = new ArrayList<CommentedValue>();
    private CommentedValue mntByRef = null;
    private List<CommentedValue> changed = new ArrayList<CommentedValue>();
    private CommentedValue created = null;
    private CommentedValue lastModified = null;
    private CommentedValue source = null;


    public Poem(final Builder builder) {
        Preconditions.checkState(builder != null);
        this.poem = builder.poem;
        this.descr = builder.descr;
        this.formRef = builder.formRef;
        this.text = builder.text;
        this.authorRef = builder.authorRef;
        this.remarks = builder.remarks;
        this.notify = builder.notify;
        this.mntByRef = builder.mntByRef;
        this.changed = builder.changed;
        this.created = builder.created;
        this.lastModified = builder.lastModified;
        this.source = builder.source;
    }

    // poem
    public CommentedValue getPoem() {
        return this.poem;
    }

    public String getPoemValue() {
        String value = null;
        if (this.poem != null) {
            value = this.poem.getValue();
        }
        return value;
    }

    // descr
    public boolean hasDescr() {
        return this.descr.size() > 0;
    }

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

    // form
    public CommentedValue getFormRef() {
        return this.formRef;
    }

    public String getFormRefValue() {
        String value = null;
        if (this.formRef != null) {
            value = this.formRef.getValue();
        }
        return value;
    }

    // text
    public List<CommentedValue> getText() {
        return this.text;
    }

    public List<String> getTextValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.text) {
            values.add(value.getValue());
        }
        return values;
    }

    // author
    public boolean hasAuthorRef() {
        return this.authorRef.size() > 0;
    }

    public List<CommentedValue> getAuthorRef() {
        return this.authorRef;
    }

    public List<String> getAuthorRefValues() {
        List<String> values = new ArrayList<String>();
        for (CommentedValue value : this.authorRef) {
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

    // mnt-by
    public CommentedValue getMntByRef() {
        return this.mntByRef;
    }

    public String getMntByRefValue() {
        String value = null;
        if (this.mntByRef != null) {
            value = this.mntByRef.getValue();
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
        Preconditions.checkState(poem != null && poem.getValue() != null, "Missing (single) mandatory attribute 'poem'");
        Preconditions.checkState(formRef != null && formRef.getValue() != null, "Missing (single) mandatory attribute 'form'");
        Preconditions.checkState(text != null && text.size() > 0, "Missing (multiple) mandatory attribute 'text'");
        Preconditions.checkState(mntByRef != null && mntByRef.getValue() != null, "Missing (single) mandatory attribute 'mnt-by'");
        Preconditions.checkState(changed != null && changed.size() > 0, "Missing (multiple) mandatory attribute 'changed'");
        Preconditions.checkState(source != null && source.getValue() != null, "Missing (single) mandatory attribute 'source'");

    }


    public WhoisResources toRequest() {
        WhoisResources whoisResources = new WhoisResources();

        WhoisObject whoisObject = new WhoisObject();
        whoisObject.setSource(new Source(this.source.getValue()));
        whoisObject.setType("poem");

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (poem != null) {
            if (!ATTR_NOT_IN_REQ.contains("poem")) {
                attributes.add(new Attribute("poem",
                        poem.getValue(),
                        poem.getComment(),
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
        if (formRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("form")) {
                attributes.add(new Attribute("form",
                        formRef.getValue(),
                        formRef.getComment(),
                        null, null));
            }
        }
        if (text != null) {
            if (!ATTR_NOT_IN_REQ.contains("text")) {
                for (CommentedValue value : text) {
                    attributes.add(new Attribute("text",
                            value.getValue(),
                            value.getComment(),
                            null, null));
                }
            }
        }
        if (authorRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("author")) {
                for (CommentedValue value : authorRef) {
                    attributes.add(new Attribute("author",
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
        if (mntByRef != null) {
            if (!ATTR_NOT_IN_REQ.contains("mnt-by")) {
                attributes.add(new Attribute("mnt-by",
                        mntByRef.getValue(),
                        mntByRef.getComment(),
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
        appendRpslAttribute(sb, "poem", this.poem);
        appendRpslAttribute(sb, "descr", this.descr);
        appendRpslAttribute(sb, "form", this.formRef);
        appendRpslAttribute(sb, "text", this.text);
        appendRpslAttribute(sb, "author", this.authorRef);
        appendRpslAttribute(sb, "remarks", this.remarks);
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

        private CommentedValue poem = null;
        private List<CommentedValue> descr = new ArrayList<CommentedValue>();
        private CommentedValue formRef = null;
        private List<CommentedValue> text = new ArrayList<CommentedValue>();
        private List<CommentedValue> authorRef = new ArrayList<CommentedValue>();
        private List<CommentedValue> remarks = new ArrayList<CommentedValue>();
        private List<CommentedValue> notify = new ArrayList<CommentedValue>();
        private CommentedValue mntByRef = null;
        private List<CommentedValue> changed = new ArrayList<CommentedValue>();
        private CommentedValue created = null;
        private CommentedValue lastModified = null;
        private CommentedValue source = null;


        public static Builder fromResponse(WhoisResources whoisResources) {
            Builder builder = new Builder();
            for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
                if (whoisObject.getType().equals("poem")) {
                    for (net.ripe.db.whois.api.rest.domain.Attribute attr : whoisObject.getAttributes()) {
                        if (attr.getName().equals("poem")) {
                            builder.setPoem(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("descr")) {
                            builder.addDescr(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("form")) {
                            builder.setFormRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("text")) {
                            builder.addText(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("author")) {
                            builder.addAuthorRef(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("remarks")) {
                            builder.addRemarks(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("notify")) {
                            builder.addNotify(attr.getValue(), attr.getComment());
                        }
                        if (attr.getName().equals("mnt-by")) {
                            builder.setMntByRef(attr.getValue(), attr.getComment());
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


        // poem
        public CommentedValue getPoem() {
            return this.poem;
        }

        public Builder setPoem(final String value) {
            Preconditions.checkState(value != null);
            this.poem = new CommentedValue(value);
            return this;
        }

        public String getPoemValue() {
            String value = null;
            if (this.poem != null) {
                value = this.poem.getValue();
            }
            return value;
        }

        public Builder setPoem(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.poem = new CommentedValue(value, comment);
            return this;
        }

        public Builder removePoem() {
            this.poem = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetPoem(final String value) {
            Preconditions.checkState(value != null);
            this.setPoem(value);
            return this;
        }

        public Builder mandatorySetPoem(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setPoem(value, comment);
            return this;
        }

        // descr
        public boolean hasDescr() {
            return this.descr.size() > 0;
        }

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
        public Builder optionalAddDescr(final String value) {
            Preconditions.checkState(value != null);
            return addDescr(value);
        }

        public Builder optionalAddDescr(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addDescr(value, comment);
        }

        // form
        public CommentedValue getFormRef() {
            return this.formRef;
        }

        public Builder setFormRef(final String value) {
            Preconditions.checkState(value != null);
            this.formRef = new CommentedValue(value);
            return this;
        }

        public String getFormRefValue() {
            String value = null;
            if (this.formRef != null) {
                value = this.formRef.getValue();
            }
            return value;
        }

        public Builder setFormRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.formRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeFormRef() {
            this.formRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetFormRef(final String value) {
            Preconditions.checkState(value != null);
            this.setFormRef(value);
            return this;
        }

        public Builder mandatorySetFormRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setFormRef(value, comment);
            return this;
        }

        // text
        public List<CommentedValue> getText() {
            return this.text;
        }

        public Builder setText(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.text = values;
            return this;
        }

        public List<String> getTextValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : text) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addText(final String value) {
            Preconditions.checkState(value != null);
            this.text.add(new CommentedValue(value));
            return this;
        }

        public Builder addText(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.text.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeTextAll() {
            this.text.clear();
            return this;
        }

        public Builder removeTextAt(int index) {
            Preconditions.checkState(index >= 0 && index < text.size(), "Invalid remove-at index for (multiple) attribute 'text'");
            this.text.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatoryAddText(final String value) {
            Preconditions.checkState(value != null);
            return addText(value);
        }

        public Builder mandatoryAddText(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addText(value, comment);
        }

        // author
        public boolean hasAuthorRef() {
            return this.authorRef.size() > 0;
        }

        public List<CommentedValue> getAuthorRef() {
            return this.authorRef;
        }

        public Builder setAuthorRef(final List<CommentedValue> values) {
            Preconditions.checkState(values != null);
            this.authorRef = values;
            return this;
        }

        public List<String> getAuthorRefValues() {
            List<String> values = new ArrayList<String>();
            for (CommentedValue value : authorRef) {
                values.add(value.getValue());
            }
            return values;
        }

        public Builder addAuthorRef(final String value) {
            Preconditions.checkState(value != null);
            this.authorRef.add(new CommentedValue(value));
            return this;
        }

        public Builder addAuthorRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.authorRef.add(new CommentedValue(value, comment));
            return this;
        }

        public Builder removeAuthorRefAll() {
            this.authorRef.clear();
            return this;
        }

        public Builder removeAuthorRefAt(int index) {
            Preconditions.checkState(index >= 0 && index < authorRef.size(), "Invalid remove-at index for (multiple) attribute 'author'");
            this.authorRef.remove(index);
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder optionalAddAuthorRef(final String value) {
            Preconditions.checkState(value != null);
            return addAuthorRef(value);
        }

        public Builder optionalAddAuthorRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            return addAuthorRef(value, comment);
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
        public CommentedValue getMntByRef() {
            return this.mntByRef;
        }

        public Builder setMntByRef(final String value) {
            Preconditions.checkState(value != null);
            this.mntByRef = new CommentedValue(value);
            return this;
        }

        public String getMntByRefValue() {
            String value = null;
            if (this.mntByRef != null) {
                value = this.mntByRef.getValue();
            }
            return value;
        }

        public Builder setMntByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.mntByRef = new CommentedValue(value, comment);
            return this;
        }

        public Builder removeMntByRef() {
            this.mntByRef = null;
            return this;
        }

        // nice to have indication of multiplicity in settter name
        public Builder mandatorySetMntByRef(final String value) {
            Preconditions.checkState(value != null);
            this.setMntByRef(value);
            return this;
        }

        public Builder mandatorySetMntByRef(final String value, final String comment) {
            Preconditions.checkState(value != null);
            this.setMntByRef(value, comment);
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


        public Poem build() {
            Poem obj = new Poem(this);
            obj.validate();
            return obj;
        }
    }

    ;

};