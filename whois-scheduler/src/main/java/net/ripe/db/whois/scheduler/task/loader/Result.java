package net.ripe.db.whois.scheduler.task.loader;

import java.util.concurrent.atomic.AtomicInteger;

public class Result {
    private AtomicInteger success = new AtomicInteger(0);
    private AtomicInteger failPass1 = new AtomicInteger(0);
    private AtomicInteger failPass2 = new AtomicInteger(0);
    private final StringBuffer text = new StringBuffer();

    public void addSuccess() {
        success.incrementAndGet();
    }

    public void addFail(final String reason, final int pass) {
        text.append(reason);

        if (pass == 1) {
            failPass1.incrementAndGet();
        }

        if (pass == 2) {
            failPass2.incrementAndGet();
        }
    }

    public int getSuccess() {
        return success.get();
    }

    public int getFailPass1() {
        return failPass1.get();
    }

    public int getFailPass2() {
        return failPass2.get();
    }

    public void addText(String text) {
        this.text.append(text);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
