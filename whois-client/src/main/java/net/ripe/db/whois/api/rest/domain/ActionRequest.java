package net.ripe.db.whois.api.rest.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class ActionRequest {
    private Action action;
    private RpslObject rpslObject;

    public ActionRequest(RpslObject rpslObject, Action action) {
        this.action = action;
        this.rpslObject = rpslObject;
    }

    public Action getAction() {
        return action;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }
}
