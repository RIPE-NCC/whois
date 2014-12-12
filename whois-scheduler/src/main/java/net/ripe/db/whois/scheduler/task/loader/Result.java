package net.ripe.db.whois.scheduler.task.loader;

import java.util.concurrent.atomic.AtomicInteger;

public class Result {
    private AtomicInteger success = new AtomicInteger(0);
    private AtomicInteger fail = new AtomicInteger(0);
    private final StringBuffer text = new StringBuffer();

    public void addSuccess() {
        success.incrementAndGet();
    }

    public void addFail(String reason) {
        text.append(reason);
        fail.incrementAndGet();
    }

    public int getSuccess() {
        return success.get();
    }

    public int getFail() {
        return fail.get();
    }

    public void addText(String text) {
        this.text.append(text);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
