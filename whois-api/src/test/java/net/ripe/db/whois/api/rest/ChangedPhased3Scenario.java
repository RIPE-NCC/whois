package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;

import java.util.List;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Mode.OLD_MODE;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Mode.NEW_MODE;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.PreCond.OBJ_NO_EXISTS;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.PreCond.OBJ_EXISTS_WITH_CHANGED;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.PreCond.OBJ_EXISTS_NO_CHANGED;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Protocol.REST;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Protocol.TELNET;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Protocol.SYNCUPD;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Protocol.MAILUPD;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Protocol.NRTM;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.CREATE;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.MODIFY;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.DELETE;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.META;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.SEARCH;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Method.EVENT;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Req.NOT_APPLIC;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Req.WITH_CHANGED;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Req.NO_CHANGED;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Result.FAILED;
import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Result.SUCCESS;

import static net.ripe.db.whois.api.rest.ChangedPhased3Scenario.Builder.given;

public class ChangedPhased3Scenario {
    private final List<ChangedPhased3Scenario> scenarios = Lists.newArrayList(
// @formatter:off

    // old mode

            // REST
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( REST,    CREATE, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( REST,    CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    DELETE, NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    DELETE, NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),

            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_NO_EXISTS           ).when( REST,    META,   NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),

            // TELNET
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_NO_EXISTS           ).when( TELNET,  META,   NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),

            // SYNCUPD
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( SYNCUPD, CREATE, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( SYNCUPD, CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, DELETE, WITH_CHANGED  ).then( SUCCESS, OBJ_NO_EXISTS),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, DELETE, NO_CHANGED    ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),

            // MAIL UPD
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( MAILUPD, CREATE, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_NO_EXISTS           ).when( MAILUPD, CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, MODIFY, WITH_CHANGED  ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, DELETE, WITH_CHANGED  ).then( SUCCESS, OBJ_NO_EXISTS),
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, DELETE, NO_CHANGED    ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),

            // NRTM
            given( OLD_MODE, OBJ_EXISTS_WITH_CHANGED ).when( NRTM,    EVENT, NOT_APPLIC     ).then( SUCCESS, OBJ_EXISTS_WITH_CHANGED),
            given( OLD_MODE, OBJ_EXISTS_NO_CHANGED   ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

    // new mode

            // REST
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( REST,    CREATE, WITH_CHANGED  ).then( FAILED, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( REST,    CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    DELETE, NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    DELETE, NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),

            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( REST,    SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( REST,    SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_NO_EXISTS           ).when( REST,    META,   NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),

            // TELNET
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_NO_EXISTS           ).when( TELNET,  META,   NOT_APPLIC    ).then( SUCCESS, OBJ_NO_EXISTS),

            // SYNCUPD
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( SYNCUPD, CREATE, WITH_CHANGED  ).then( FAILED, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( SYNCUPD, CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( SYNCUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( SYNCUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),

            // MAIL UPD
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( MAILUPD, CREATE, WITH_CHANGED  ).then( FAILED, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_NO_EXISTS           ).when( MAILUPD, CREATE, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, MODIFY, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, MODIFY, NO_CHANGED    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),

            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( MAILUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, DELETE, WITH_CHANGED  ).then( FAILED, OBJ_EXISTS_WITH_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( MAILUPD, DELETE, NO_CHANGED    ).then( SUCCESS, OBJ_NO_EXISTS),

            // NRTM
            given( NEW_MODE, OBJ_EXISTS_WITH_CHANGED ).when( NRTM,    EVENT, NOT_APPLIC     ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED),
            given( NEW_MODE, OBJ_EXISTS_NO_CHANGED   ).when( TELNET,  SEARCH, NOT_APPLIC    ).then( SUCCESS, OBJ_EXISTS_NO_CHANGED)


// @formatter:on

            );

    public List<ChangedPhased3Scenario> getScenarios() {
        return scenarios;
    }

    public enum Mode {
        OLD_MODE,NEW_MODE
    }

    public enum PreCond {
        OBJ_EXISTS_NO_CHANGED, OBJ_EXISTS_WITH_CHANGED, OBJ_NO_EXISTS
    }

    public enum Protocol {
        REST, TELNET, SYNCUPD, MAILUPD, NRTM
    }

    public enum Method {
        CREATE, MODIFY, DELETE, SEARCH, GET, META, EVENT
    }

    public enum Req {
        WITH_CHANGED, NO_CHANGED, NOT_APPLIC
    }

    public enum Result {
        SUCCESS, FAILED
    }

    private final String description;
    private final Mode mode;
    private final PreCond preCond;
    private final Protocol protocol;
    private final Method method;
    private final Req req;
    private final Result result;
    private final PreCond postCond;

    public ChangedPhased3Scenario(Mode mode, PreCond preCond, Protocol protocol, Method method, Req req, Result result, PreCond postCond) {
        this.description = String.format("Mode:%s, Pre:%s, Protocol:%sMethod:%s, Req:%s, Post:%s",
                mode, preCond, protocol, method, req, result, postCond);
        this.mode = mode;
        this.preCond = preCond;
        this.protocol = protocol;
        this.method = method;
        this.req = req;
        this.result = result;
        this.postCond = postCond;
    }

    public String getDescription() {
        return description;
    }

    public Mode getMode() {
        return mode;
    }

    public PreCond getPreCond() {
        return preCond;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Method getMethod() {
        return method;
    }

    public Req getReq() {
        return req;
    }

    public PreCond getPostCond() {
        return postCond;
    }

    public String toString() {
        return description;
    }

    public void run() {

    }

    static class Builder {
        private  Mode mode;
        private  PreCond preCond;
        private  Protocol protocol;
        private  Method method;
        private  Req req;

        public static Builder given(final Mode mode, final PreCond preCond) {
            Builder builder = new Builder();
            builder.mode = mode;
            builder.preCond = preCond;
            return builder;
        }

        public Builder when(final Protocol protocol, final Method method, final Req req) {
            this.protocol = protocol;
            this.method = method;
            this.req = req;
            return this;
        }

        public ChangedPhased3Scenario then(final Result result, final PreCond postCond) {
            return new ChangedPhased3Scenario(
                    mode, preCond, protocol,
                    method,req,
                    result, postCond);
        }

    }
}