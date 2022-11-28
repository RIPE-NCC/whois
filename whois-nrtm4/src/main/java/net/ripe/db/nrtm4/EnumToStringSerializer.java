package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;


public class EnumToStringSerializer extends StdSerializer<Enum> {

    public EnumToStringSerializer() {
        super(Enum.class);
    }

    public EnumToStringSerializer(Class<Enum> t) {
        super(t);
    }

    @Override
    public void serialize(
        final Enum value,
        final JsonGenerator gen,
        final SerializerProvider provider
    ) throws IOException {
        gen.writeObject(value.toString());
    }

}
