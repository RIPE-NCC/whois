package net.ripe.db.whois.compare.rest;

import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringResponseObject implements ResponseObject{

    private String string;

    public StringResponseObject(String string) {
        this.string = string;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            out.write(b);
        }
    }

    @Override
    public byte[] toByteArray() {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringResponseObject)) return false;

        final StringResponseObject that = (StringResponseObject) o;

        return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }
}
