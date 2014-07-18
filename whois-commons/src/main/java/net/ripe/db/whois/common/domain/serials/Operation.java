package net.ripe.db.whois.common.domain.serials;

import com.google.common.collect.Maps;

import java.util.Map;

public enum Operation {
    UPDATE(1, "ADD"),
    DELETE(2, "DEL");

    private final int code;
    private static final Map<Integer, Operation> BY_ID = Maps.newHashMap();
    private final String name;

    static {
        for (Operation operation : Operation.values()) {
            BY_ID.put(operation.getCode(), operation);
        }
    }

    Operation(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public static Operation getByCode(int code) {
        return BY_ID.get(code);
    }

    public static Operation getByName(String name) {
        for (Operation operation : Operation.values()) {
            if (operation.name.equals(name)) {
                return operation;
            }
        }
        throw new IllegalArgumentException("No operation for " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}
