package net.ripe.db.whois.update.dns.zonemaster.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class GetTestResultsResponse {

    @JsonProperty("jsonrpc")
    private String jsonRpc;
    private String id;
    private Result result;

    public String getJsonRpc() {
        return jsonRpc;
    }

    public void setJsonRpc(final String jsonRpc) {
        this.jsonRpc = jsonRpc;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jsonrpc", jsonRpc)
                .append("id", id)
                .append("result", result)
                .toString();
    }

    static public class Result {

        private Map params;
        @JsonProperty("hash_id")
        private String hashId;
        @JsonProperty("creation_time")
        private String creationTime;
        private String id;
        private Message[] results;

        public Map getParams() {
            return params;
        }

        public void setParams(final Map params) {
            this.params = params;
        }

        public String getHashId() {
            return hashId;
        }

        public void setHashId(final String hashId) {
            this.hashId = hashId;
        }

        public String getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(final String creationTime) {
            this.creationTime = creationTime;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public Message[] getResults() {
            return results;
        }

        public void setResults(final Message[] results) {
            this.results = results;
        }

        public String toString() {
            return new ToStringBuilder(this)
                    .append("params", params)
                    .append("hashId", hashId)
                    .append("creationTime", creationTime)
                    .append("results", results)
                    .toString();
        }

        static public class Message {

            private String message;
            private String level;
            private String module;
            private String ns;

            public String getMessage() {
                return message;
            }

            public void setMessage(final String message) {
                this.message = message;
            }

            public String getLevel() {
                return level;
            }

            public void setLevel(final String level) {
                this.level = level;
            }

            public String getModule() {
                return module;
            }

            public void setModule(final String module) {
                this.module = module;
            }

            public String getNs() {
                return ns;
            }

            public void setNs(final String ns) {
                this.ns = ns;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this)
                        .append("message", message)
                        .append("level", level)
                        .append("module", module)
                        .append("ns", ns)
                        .toString();
            }
        }
    }

}
