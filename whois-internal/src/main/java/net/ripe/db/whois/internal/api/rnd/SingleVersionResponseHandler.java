package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;

import java.util.List;

public class SingleVersionResponseHandler implements ResponseHandler {
    private List<Message> errors = Lists.newArrayList();
    private VersionDateTime versionDateTime;
    private RpslObject rpslObject;

    @Override
    public String getApi() {
        return "INTERNAL_API";
    }

    @Override
    public void handle(final ResponseObject responseObject) {
        if (responseObject instanceof RpslObjectWithTimestamp){
            RpslObjectWithTimestamp object = (RpslObjectWithTimestamp) responseObject;
            rpslObject = object.getRpslObject();
            versionDateTime = object.getVersionDateTime();
        } else if (responseObject instanceof MessageObject) {
            final Message message = ((MessageObject) responseObject).getMessage();
            if (message != null && Messages.Type.INFO != message.getType()) {
                errors.add(message);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public VersionDateTime getVersionDateTime() {
        return versionDateTime;
    }

    public List<Message> getErrors() {
        return errors;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }
}
