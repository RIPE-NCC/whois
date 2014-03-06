package net.ripe.db.whois.api.rest.compare;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;

public class StringResponseObject implements ResponseObject{

    private String string;

    public StringResponseObject(String string) {
        this.string = string;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] bytes = string.getBytes(Charsets.UTF_8);
        for (byte b : bytes) {
            out.write(b);
        }
    }

    @Override
    public byte[] toByteArray() {
        return string.getBytes(Charsets.UTF_8);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringResponseObject)) return false;

        StringResponseObject that = (StringResponseObject) o;
        return string.equals(that.string);
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }
}
