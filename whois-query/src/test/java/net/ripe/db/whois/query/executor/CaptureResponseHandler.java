package net.ripe.db.whois.query.executor;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.domain.ResponseHandler;

import java.util.List;

public class CaptureResponseHandler implements ResponseHandler {
    final List<ResponseObject> responseObjects = Lists.newArrayList();

    @Override
    public String getApi() {
        return "CAPTURE";
    }

    @Override
    public void handle(final ResponseObject responseObject) {
        responseObjects.add(responseObject);
    }

    public List<ResponseObject> getResponseObjects() {
        return responseObjects;
    }
}
