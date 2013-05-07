package net.ripe.db.whois.api.freetext;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "response")
class SearchResponse {

    @XmlElement(required = true)
    private Result result;

    @XmlElements({@XmlElement(name = "lst", type = Lst.class)})
    private List<Lst> lsts;

    public void setResult(final Result result) {
        this.result = result;
    }

    public void setLsts(final List<Lst> lsts) {
        this.lsts = lsts;
    }

    static class Result {

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

        public void setDocs(final List<Doc> docs) {
            this.docs = docs;
        }

        @XmlRootElement(name = "doc")
        static class Doc {

            @XmlElements({
                    @XmlElement(name = "str", type = Str.class)
            })
            private List<Str> strs;

            public void setStrs(final List<Str> strs) {
                this.strs = strs;
            }
        }
    }

    @XmlRootElement(name = "lst")
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
    }

    @XmlRootElement(name = "arr")
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
    }

    @XmlRootElement(name = "str")
    static class Str {
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
    }

    @XmlRootElement(name = "int")
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
