package net.ripe.db.whois.internal.api.rnd.jmx;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "ObjectVersion", description = "Log file update operations")
public class ObjectReferenceJmx extends JmxBase {



    public ObjectReferenceJmx(Logger logger) {
        super(logger);
    }


}
