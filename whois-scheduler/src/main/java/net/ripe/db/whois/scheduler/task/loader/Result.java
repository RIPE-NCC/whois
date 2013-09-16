package net.ripe.db.whois.scheduler.task.loader;

public class Result {
    private volatile int success = 0, fail = 0;
    private final StringBuffer text = new StringBuffer();

    public void addSuccess() {
        success++;
    }

    public void addFail(String reason) {
        text.append(reason);
        fail++;
    }

    public int getSuccess() {
        return success;
    }

    public int getFail() {
        return fail;
    }

    public void addText(String text) {
        this.text.append(text);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
