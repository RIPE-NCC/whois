package net.ripe.db.whois.update.dns.zonemaster.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.List;

/**
 * Entity class for Zonemaster get_test_results API method response.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class GetTestResultsResponse extends ZonemasterResponse {

    @JsonProperty(value = "result")
    private Result result;

    public Result getResult() {
        return result;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("result", result);
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("hash_id")
        private String hashId;
        @JsonProperty("creation_time")
        private String creationTime;
        @JsonProperty
        private String id;
        @JsonProperty(value = "results")
        private List<Message> messages;

        public List<Message> getResults() {
            return messages;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("hashId", hashId)
                    .add("creationTime", creationTime)
                    .add("results", messages)
                    .toString();
        }

        public static class Message {
            private String message;
            private String level;
            private String module;
            private String ns;
            private String testcase;

            public String getMessage() {
                return message;
            }

            public String getLevel() {
                return level;
            }

            public String getModule() {
                return module;
            }

            public String getNs() {
                return ns;
            }

            public String getTestcase() {
                return testcase;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("message", message)
                        .add("level", level)
                        .add("module", module)
                        .add("ns", ns)
                        .add("testcase", testcase)
                        .toString();
            }
        }


    }

}
