package net.ripe.db.whois.api.fulltextsearch;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
@JsonInclude(NON_EMPTY)
public class SearchResponse {

    @XmlElement(required = true)
    private Result result;

    @XmlElements({@XmlElement(name = "lst", type = Lst.class)})
    private List<Lst> lsts;

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    public List<Lst> getLsts() {
        return lsts;
    }

    public void setLsts(final List<Lst> lsts) {
        this.lsts = lsts;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "result")
    @JsonInclude(NON_EMPTY)
    public static class Result {

        @XmlAttribute(required = true)
        private String name;

        @XmlAttribute(required = true)
        private int numFound = 0;

        @XmlAttribute(required = true)
        private int start = 0;

        @XmlElements({@XmlElement(name = "doc", type = Doc.class)})
        private List<Doc> docs;

        private Result() {
            // required no-arg constructor
        }

        public Result(final String name, final int numFound, final int start) {
            this.name = name;
            this.numFound = numFound;
            this.start = start;
        }

        public List<Doc> getDocs() {
            return docs;
        }

        public void setDocs(final List<Doc> docs) {
            this.docs = docs;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlRootElement(name = "doc")
        @JsonInclude(NON_EMPTY)
        public static class Doc {

            @XmlElements({@XmlElement(name = "str", type = Str.class)})
            private List<Str> strs;

            public void setStrs(final List<Str> strs) {
                this.strs = strs;
            }

            public List<Str> getStrs() {
                return strs;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "lst")
    @JsonInclude(NON_EMPTY)
    static class Lst {
        @XmlAttribute(required = true)
        private String name;

        @XmlElements({@XmlElement(name = "int", type = Int.class)})
        private List<Int> ints;

        @XmlElements({@XmlElement(name = "str", type = Str.class)})
        private List<Str> strs;

        @XmlElements({@XmlElement(name = "lst", type = Lst.class)})
        private List<Lst> lsts;

        @XmlElements({@XmlElement(name = "arr", type = Arr.class)})
        private List<Arr> arrs;

        private Lst() {
            // required no-arg constructor
        }

        public Lst(final String name) {
            this.name = name;
        }

        public void setInts(final List<Int> ints) {
            this.ints = ints;
        }

        public void setStrs(final List<Str> strs) {
            this.strs = strs;
        }

        public void setLsts(final List<Lst> lsts) {
            this.lsts = lsts;
        }

        public void setArrs(final List<Arr> arrs) {
            this.arrs = arrs;
        }

        public String getName() {
            return name;
        }

        public List<Lst> getLsts() {
            return lsts;
        }

        public List<Arr> getArrs() {
            return arrs;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "arr")
    @JsonInclude(NON_EMPTY)
    static class Arr {
        @XmlAttribute(required = true)
        private String name;

        @XmlElement(name = "str", type = Str.class)
        private Str str;

        private Arr() {
            // required no-arg constructor
        }

        public Arr(final String name) {
            this.name = name;
        }

        public void setStr(final Str str) {
            this.str = str;
        }

        public String getName() {
            return name;
        }

        public Str getStr() {
            return str;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "str")
    @JsonInclude(NON_EMPTY)
    public static class Str {
        @XmlAttribute(required = true)
        private String name;

        @XmlValue
        private String value;

        private Str() {
            // required no-arg constructor
        }

        public Str(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "int")
    @JsonInclude(NON_EMPTY)
    static class Int {
        @XmlAttribute(required = true)
        private String name;

        @XmlValue
        private String value;

        private Int() {
            // required no-arg constructor
        }

        public Int(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }
}
