package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.annotation.concurrent.Immutable;
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

        final WhoisVersion that = (WhoisVersion) o;

        return Objects.equals(that.date, date) &&
                Objects.equals(that.deletedDate, deletedDate) &&
                Objects.equals(that.operation, operation) &&
                Objects.equals(that.revision, revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletedDate, revision, date, operation);
    }
}
