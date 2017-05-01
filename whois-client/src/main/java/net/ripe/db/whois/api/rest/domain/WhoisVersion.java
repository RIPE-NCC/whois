package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "deletedDate",
        "revision",
        "date",
        "operation"
})

@XmlRootElement(name = "version")
public class WhoisVersion {
    @XmlAttribute(name = "deleted")
    private String deletedDate;
    @XmlElement(name = "revision", required = false)
    private Integer revision;
    @XmlElement(name = "date", required = false)
    private String date;
    @XmlElement(name = "operation", required = false)
    private String operation;

    public WhoisVersion(final String operation, final String date, final int revision) {
        this.operation = operation;
        this.date = date;
        this.revision = revision;
    }

    public WhoisVersion(final String deletedDate) {
        this.deletedDate = deletedDate;
    }

    public WhoisVersion() {
        // required no-arg constructor
    }

    public Integer getRevision() {
        return revision;
    }

    public String getDate() {
        return date;
    }

    public String getOperation() {
        return operation;
    }

    public String getDeletedDate() {
        return deletedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhoisVersion that = (WhoisVersion) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (deletedDate != null ? !deletedDate.equals(that.deletedDate) : that.deletedDate != null) return false;
        if (operation != null ? !operation.equals(that.operation) : that.operation != null) return false;
        return !(revision != null ? !revision.equals(that.revision) : that.revision != null);

    }

    @Override
    public int hashCode() {
        return Objects.hash(deletedDate, revision, date, operation);
    }
}
