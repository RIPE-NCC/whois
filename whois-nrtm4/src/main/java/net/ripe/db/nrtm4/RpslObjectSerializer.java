package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;


public class RpslObjectSerializer extends StdSerializer<RpslObject> {

    public RpslObjectSerializer() {
        this(null);
    }

    protected RpslObjectSerializer(final Class<RpslObject> objectClass) {
        super(objectClass);
    }

    @Override
    public void serialize(
        final RpslObject object,
        final JsonGenerator jsonGenerator,
        final SerializerProvider provider
    ) throws IOException {
        jsonGenerator.writeString(object.toString().replace("\\\n", "\\n"));
    }

}
