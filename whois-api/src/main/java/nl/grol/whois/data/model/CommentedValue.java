package nl.grol.whois.data.model;

import com.google.common.base.Objects;

public class CommentedValue {
    private String value;
    private String comment;

    public CommentedValue(String value) {
        this.value = value;
        this.comment = null;

    }

    public CommentedValue(String value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("value", this.value)
                .add("comment", this.comment)
                .toString();
    }

}
