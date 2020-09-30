package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "error", propOrder = {
    "errorCode",
    "title",
    "description"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Immutable
public class Error {

    protected int errorCode;
    protected String title;
    protected List<String> description;

    public Error(final int errorCode, final String title, final List<String> description) {
        this.errorCode = errorCode;
        this.title = title;
        this.description = Collections.unmodifiableList(description);
    }

    public Error() {
        // required no-arg constructor
    }
}
