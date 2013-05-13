package net.ripe.db.whois.scheduler.task.loader;

public class Result {
    private int success = 0, fail = 0;
    private final StringBuilder text = new StringBuilder();

    public void addSuccess(int pass) {
        if (pass > 1) success++;
    }

    public void addFail(int pass, String reason) {
        if (pass > 1) {
            fail++;
            text.append(reason);
        }
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
