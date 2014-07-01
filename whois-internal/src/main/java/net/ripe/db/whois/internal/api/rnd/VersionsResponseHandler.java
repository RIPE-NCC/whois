package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.domain.VersionResponseObject;

import java.util.List;

public class VersionsResponseHandler implements ResponseHandler {
    private List<Message> errors = Lists.newArrayList();
    private List<VersionResponseObject> versions = Lists.newArrayList();
    private RpslObject rpslObject;

    @Override
    public String getApi() {
        return "INTERNAL_API";
    }

    @Override
    public void handle(final ResponseObject responseObject) {
        if (responseObject instanceof VersionResponseObject) {
            versions.add((VersionResponseObject) responseObject);
        } else if (responseObject instanceof MessageObject) {
            final Message message = ((MessageObject) responseObject).getMessage();
            if (message != null && Messages.Type.INFO != message.getType()) {
                errors.add(message);
            }
        } else if (responseObject instanceof RpslObject){
            rpslObject = (RpslObject) responseObject;
        }
    }

    public List<VersionResponseObject> getVersions() {
        return versions;
    }

    public List<Message> getErrors() {
        return errors;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }
}
