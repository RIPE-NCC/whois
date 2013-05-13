package net.ripe.db.whois.common.domain.serials;

public class SerialRange {
    private final int begin;
    private final int end;

    public SerialRange(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", begin, end);
    }
}
