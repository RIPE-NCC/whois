package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;

import javax.annotation.Nullable;
import java.util.List;

public class VersionDateTimeResponseHandler implements ResponseHandler {
    private List<Message> errors = Lists.newArrayList();
    private RpslObjectWithTimestamp rpslObjectWithTimestamp;


    @Override
    public String getApi() {
        return "INTERNAL_API";
    }

    @Override
    public void handle(final ResponseObject responseObject) {
        if (responseObject instanceof RpslObjectWithTimestamp){
            this.rpslObjectWithTimestamp = (RpslObjectWithTimestamp) responseObject;
        } else if (responseObject instanceof MessageObject) {
            final Message message = ((MessageObject) responseObject).getMessage();
            if (message != null && Messages.Type.INFO != message.getType()) {
                errors.add(message);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public List<Message> getErrors() {
        return errors;
    }

    @Nullable
    public RpslObjectWithTimestamp getRpslObjectWithTimestamp() {
        return rpslObjectWithTimestamp;
    }
}
